package com.svyd.upcomingweather.feature.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.upcomingweather.core.domain.model.DayForecast
import com.svyd.upcomingweather.core.domain.model.DayUpdate
import com.svyd.upcomingweather.core.domain.usecase.ObserveDay
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastUiMapper
import com.svyd.upcomingweather.feature.forecast.model.DayDetailsUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

/**
 * One day of the forecast, opened from a row of the list.
 *
 * Nothing here asks for a refresh: the stream underneath fetches when it is first collected, so
 * arriving at this screen is itself the request.
 */
internal class DayDetailsViewModel(
    observeDay: ObserveDay,
    date: LocalDate,
    private val mapper: ForecastUiMapper,
    private val clock: Clock,
) : ViewModel() {

    private val refresh = MutableSharedFlow<Unit>()

    /**
     * What the screen last showed.
     *
     * Held here rather than carried through the stream, because the stream does not outlive the
     * screen: it stops with the last subscriber and starts again from the beginning. A fetch
     * announces itself on every restart, and without something that remembers the day already
     * drawn it would empty the page each time.
     */
    private var rendered: DayDetailsUiState = DayDetailsUiState.Loading

    val state: StateFlow<DayDetailsUiState> =
        combine(observeDay(date, refresh), ticks()) { update, _ -> update }
            .map { update -> toUiState(update).also { rendered = it } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE),
                initialValue = DayDetailsUiState.Loading,
            )

    private fun toUiState(update: DayUpdate): DayDetailsUiState = when (update) {
        // A fetch behind a day already drawn changes nothing on screen; only an empty page waits.
        DayUpdate.Fetching -> rendered as? DayDetailsUiState.Content ?: DayDetailsUiState.Loading

        // The same for a fetch that failed behind one. The day on screen came from storage and is
        // no less true for the next fetch having gone nowhere; only an empty page reports it.
        DayUpdate.Failed -> rendered as? DayDetailsUiState.Content ?: DayDetailsUiState.Unavailable

        DayUpdate.Unavailable -> DayDetailsUiState.Unavailable
        is DayUpdate.Stale -> content(update.day, update.retrievedAt)
        is DayUpdate.Ready -> content(update.day, update.retrievedAt)
    }

    private fun content(day: DayForecast, retrievedAt: Instant): DayDetailsUiState.Content {
        val now = clock.instant()
        // The day's own zone: these slots divide that place's day, not the reader's.
        val zone = day.sunrise.zone
        return DayDetailsUiState.Content(
            title = mapper.dayTitle(day, now, zone),
            hero = mapper.dayHero(day, retrievedAt, now),
            logHeader = mapper.dayLogHeader(day, now, zone),
            slots = mapper.slots(day),
            readings = mapper.dayReadings(day),
        )
    }

    private companion object {
        const val SUBSCRIPTION_GRACE = 5_000L
    }
}
