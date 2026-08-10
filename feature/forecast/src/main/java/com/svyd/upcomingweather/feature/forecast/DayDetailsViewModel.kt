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

    val state: StateFlow<DayDetailsUiState> =
        combine(observeDay(date, refresh), ticks()) { update, _ -> update }
            .map(::toUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE),
                initialValue = DayDetailsUiState.Loading,
            )

    private fun toUiState(update: DayUpdate): DayDetailsUiState = when (update) {
        DayUpdate.Fetching -> DayDetailsUiState.Loading
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
