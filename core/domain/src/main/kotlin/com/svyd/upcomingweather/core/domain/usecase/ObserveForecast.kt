package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.ForecastUpdate
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.Duration

/**
 * The forecast for whatever place is selected, refetched whenever the selection changes or
 * [refresh] asks for it again.
 *
 * Changing city while a fetch is in flight abandons that fetch: the answer to a question nobody is
 * asking anymore is not worth waiting for, and it must never land after the answer to the current
 * one.
 */
class ObserveForecast(
    private val selection: SelectedPlaceRepository,
    private val forecasts: ForecastRepository,
) {

    /**
     * The two reasons to fetch sit at different levels rather than in one stream, so each keeps its
     * own meaning: arriving at a new place honours [MAX_AGE], while every [refresh] overrides it.
     * Which level restarted is what says whether the age still counts.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(refresh: Flow<Unit>): Flow<ForecastUpdate> =
        selection.selected.flatMapLatest { place ->
            if (place == null) {
                flowOf(ForecastUpdate.NoPlace)
            } else {
                refresh.map { FORCED }
                    .onStart { emit(WITHIN_AGE) }
                    .flatMapLatest { force -> read(place, force) }
            }
        }

    private fun read(place: SelectedPlace, force: Boolean): Flow<ForecastUpdate> =
        forecasts.forecast(place, MAX_AGE, force)
            .map { read ->
                when (read) {
                    is ForecastRead.Cached -> ForecastUpdate.Ready(read.forecast)
                    is ForecastRead.Stale -> ForecastUpdate.Stale(read.forecast)
                    // Not caught, and not the end of anything: storage goes on reporting after it,
                    // so a forecast saved later still reaches whoever is reading.
                    is ForecastRead.Failed -> ForecastUpdate.Failed(read.cause)
                }
            }
            .onStart { emit(ForecastUpdate.Fetching) }

    companion object {
        /**
         * How long a forecast stays worth showing without asking again.
         *
         * Weather changes slowly enough that a minute-old answer is the same answer, and quickly
         * enough that anyone returning to the screen after that deserves a new one.
         */
        val MAX_AGE: Duration = Duration.ofMinutes(1)

        private const val FORCED = true
        private const val WITHIN_AGE = false
    }
}
