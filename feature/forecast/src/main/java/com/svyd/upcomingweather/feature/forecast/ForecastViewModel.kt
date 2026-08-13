package com.svyd.upcomingweather.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastUpdate
import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.core.domain.usecase.SelectCurrentPlace
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastUiMapper
import com.svyd.upcomingweather.feature.forecast.model.Busy
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    private val mapper: ForecastUiMapper,
    private val clock: Clock,
) : ViewModel() {

    private val refresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val locationOutcome = MutableStateFlow<ForecastUiState?>(null)
    private val locating = MutableStateFlow(false)

    /**
     * What the screen last showed.
     *
     * Held here rather than folded through the stream, because the stream does not outlive the
     * screen: leaving for the day details drops the last subscriber and the collection stops. A
     * fold would restart from its seed on the way back and draw an empty page over a forecast that
     * is already known, so what a fetch in flight looks like has to be decided against the last
     * thing drawn rather than against the start of a stream.
     */
    private var rendered: ForecastUiState = ForecastUiState.Loading()

    val state: StateFlow<ForecastUiState> =
        combine(
            observeForecast(refresh),
            ticks(),
            locationOutcome,
            locating,
        ) { update, _, outcome, looking ->
            Triple(update, outcome, looking)
        }
            .map { (update, outcome, looking) ->
                when {
                    // A failed attempt only takes the screen when there is nothing else on it:
                    // someone who has already chosen a city keeps seeing its weather.
                    outcome != null && !looking && update == ForecastUpdate.NoPlace -> outcome

                    // Finding the device takes seconds and says nothing while it does, so it
                    // outranks a fetch: it is the slower of the two and the one the reader asked
                    // for.
                    looking -> reduce(rendered, update).locating()

                    else -> reduce(rendered, update)
                }.also { rendered = it }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE),
                initialValue = ForecastUiState.Loading(),
            )

    fun refresh() {
        refresh.tryEmit(Unit)
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
                is ForecastUiState.Content -> previous.copy(busy = Busy.Updating)
                else -> ForecastUiState.Loading(Busy.Updating)
            }

            is ForecastUpdate.Stale -> content(update.forecast, busy = Busy.Updating)
            is ForecastUpdate.Ready -> content(update.forecast, busy = null)

            // A failure over something already drawn is a notice, not a blank page.
            is ForecastUpdate.Failed -> when (previous) {
                is ForecastUiState.Content ->
                    previous.copy(busy = null, offline = mapper.offline())

                else -> ForecastUiState.Error
            }
        }

    /** Says the device is being asked where it is, without disturbing what is drawn. */
    private fun ForecastUiState.locating(): ForecastUiState = when (this) {
        is ForecastUiState.Content -> copy(busy = Busy.Locating)
        else -> ForecastUiState.Loading(Busy.Locating)
    }

    private fun content(forecast: Forecast, busy: Busy?): ForecastUiState.Content {
        val now = clock.instant()
        return ForecastUiState.Content(
            city = mapper.city(forecast.label) ?: mapper.currentLocationName(),
            hero = mapper.hero(forecast, now),
            hours = mapper.hours(forecast, now, HOURS_ON_STRIP),
            readings = mapper.readings(forecast.current, forecast.days.firstOrNull()),
            days = mapper.days(forecast, now),
            busy = busy,
        )
    }

    private companion object {
        const val HOURS_ON_STRIP = 8
        const val SUBSCRIPTION_GRACE = 5_000L
    }
}
