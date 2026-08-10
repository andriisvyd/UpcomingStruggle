package com.svyd.upcomingweather.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastUpdate
import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.core.domain.usecase.RecordLocationPrompt
import com.svyd.upcomingweather.core.domain.usecase.SelectCurrentPlace
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastUiMapper
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock

/**
 * Turns what the domain reports into what the dashboard draws.
 *
 * Three things are decided here rather than below: whether a fetch in flight looks like an empty
 * page or a spinner over what is already drawn, when a forecast stops counting as current, and what
 * a refused location does to a screen with nothing on it.
 */
internal class ForecastViewModel(
    observeForecast: ObserveForecast,
    private val selectCurrentPlace: SelectCurrentPlace,
    private val recordLocationPrompt: RecordLocationPrompt,
    private val mapper: ForecastUiMapper,
    private val clock: Clock,
) : ViewModel() {

    private val refresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val locationOutcome = MutableStateFlow<ForecastUiState?>(null)
    private val locating = MutableStateFlow(false)

    val state: StateFlow<ForecastUiState> =
        combine(
            observeForecast(refresh),
            ticks(),
            locationOutcome,
            locating,
        ) { update, _, outcome, looking ->
            Triple(update, outcome, looking)
        }
            .runningFold(ForecastUiState.Loading as ForecastUiState) { previous, next ->
                val (update, outcome, looking) = next
                when {
                    // Finding the device takes seconds and says nothing while it does; without this
                    // the button looks dead.
                    looking && update == ForecastUpdate.NoPlace -> ForecastUiState.Loading

                    // A failed attempt only takes the screen when there is nothing else on it:
                    // someone who has already chosen a city keeps seeing its weather.
                    outcome != null && update == ForecastUpdate.NoPlace -> outcome

                    else -> reduce(previous, update)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE),
                initialValue = ForecastUiState.Loading,
            )

    fun refresh() {
        refresh.tryEmit(Unit)
    }

    /**
     * Notes that the system prompt is about to be shown.
     *
     * Nothing can work this out later: a refusal looks the same whether or not the reader was ever
     * asked, so the one component that knows has to write it down.
     */
    fun locationPromptShown() {
        viewModelScope.launch { recordLocationPrompt() }
    }

    /**
     * Called once the platform has been asked; the refusal comes back as a failure.
     *
     * [canAskAgain] is the half of the permission picture only a caller holding an Activity can
     * read, so it is passed in rather than looked up here.
     */
    fun useCurrentLocation(canAskAgain: Boolean) {
        viewModelScope.launch {
            locating.value = true
            try {
                selectCurrentPlace()
                    .onSuccess { locationOutcome.value = null }
                    .onFailure { cause -> locationOutcome.value = cause.asState(canAskAgain) }
            } finally {
                locating.value = false
            }
        }
    }

    /** Anything other than these two, is a defect rather than something to explain to a reader. */
    private fun Throwable.asState(canAskAgain: Boolean): ForecastUiState? = when (this) {
        is WeatherFailure.LocationPermissionMissing -> ForecastUiState.LocationRefused(canAskAgain)
        is WeatherFailure.LocationUnavailable -> ForecastUiState.LocationUnavailable
        else -> null
    }

    private fun reduce(previous: ForecastUiState, update: ForecastUpdate): ForecastUiState =
        when (update) {
            ForecastUpdate.NoPlace -> ForecastUiState.Empty

            // What a fetch looks like depends on what is already drawn: nothing to show means the
            // page types itself in, something to show means a spinner over it.
            ForecastUpdate.Fetching -> when (previous) {
                is ForecastUiState.Content -> previous.copy(isRefreshing = true)
                else -> ForecastUiState.Loading
            }

            is ForecastUpdate.Stale -> content(update.forecast, refreshing = true)
            is ForecastUpdate.Ready -> content(update.forecast, refreshing = false)

            // A failure over something already drawn is a notice, not a blank page.
            is ForecastUpdate.Failed -> when (previous) {
                is ForecastUiState.Content ->
                    previous.copy(isRefreshing = false, offline = mapper.offline())

                else -> ForecastUiState.Error
            }
        }

    private fun content(forecast: Forecast, refreshing: Boolean): ForecastUiState.Content {
        val now = clock.instant()
        return ForecastUiState.Content(
            city = mapper.city(forecast.label) ?: mapper.currentLocationName(),
            hero = mapper.hero(forecast, now),
            hours = mapper.hours(forecast, now, HOURS_ON_STRIP),
            readings = mapper.readings(forecast.current, forecast.days.firstOrNull()),
            days = mapper.days(forecast, now),
            isRefreshing = refreshing,
        )
    }

    private companion object {
        const val HOURS_ON_STRIP = 8
        const val SUBSCRIPTION_GRACE = 5_000L
    }
}
