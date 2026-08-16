package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.DayUpdate
import com.svyd.upcomingweather.core.domain.model.day
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * One day of the forecast that is stored for the selected place.
 *
 * Reads storage and never fetches. A day is opened from a list that was built from the very
 * forecast this looks in, so there is nothing to ask the provider for — and asking would put a
 * second request behind a page with nothing new to say. What the list does about refreshing that
 * forecast reaches here anyway, because both are reading the one copy.
 *
 * Two answers, and no third: either the stored forecast reaches this day or it does not. A fetch in
 * flight is not this screen's business and a fetch that failed cannot take a stored day away.
 */
class ObserveDay(
    private val selection: SelectedPlaceRepository,
    private val forecasts: ForecastRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(date: LocalDate): Flow<DayUpdate> =
        selection.selected.flatMapLatest { place ->
            if (place == null) {
                flowOf(DayUpdate.Unavailable)
            } else {
                forecasts.stored(place).map { forecast ->
                    val day = forecast?.day(date)
                    if (forecast != null && day != null) {
                        DayUpdate.Ready(day, forecast.retrievedAt)
                    } else {
                        DayUpdate.Unavailable
                    }
                }
            }
        }
}
