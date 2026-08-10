package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.DayForecast
import com.svyd.upcomingweather.core.domain.model.DayUpdate
import com.svyd.upcomingweather.core.domain.model.ForecastUpdate
import com.svyd.upcomingweather.core.domain.model.day
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * One day of the forecast for the selected place.
 *
 * Reads the same stream the whole forecast does, so a day opened from the list is drawn from what is
 * already stored and replaced when the fetch behind it lands.
 */
class ObserveDay(private val forecasts: ObserveForecast) {

    operator fun invoke(date: LocalDate, refresh: Flow<Unit>): Flow<DayUpdate> =
        forecasts(refresh).map { update ->
            when (update) {
                ForecastUpdate.Fetching -> DayUpdate.Fetching
                is ForecastUpdate.Stale -> update.forecast.day(date)
                    .orUnavailable { DayUpdate.Stale(it, update.forecast.retrievedAt) }

                is ForecastUpdate.Ready -> update.forecast.day(date)
                    .orUnavailable { DayUpdate.Ready(it, update.forecast.retrievedAt) }
                // A day cannot be looked up in a forecast that is missing or never arrived, and a
                // screen showing one day has nothing else to say about either.
                ForecastUpdate.NoPlace -> DayUpdate.Unavailable
                is ForecastUpdate.Failed -> DayUpdate.Unavailable
            }
        }

    private fun DayForecast?.orUnavailable(found: (DayForecast) -> DayUpdate): DayUpdate =
        this?.let(found) ?: DayUpdate.Unavailable
}
