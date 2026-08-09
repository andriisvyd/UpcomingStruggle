package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.ForecastUpdate
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.cancellation.CancellationException

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

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(refresh: Flow<Unit>): Flow<ForecastUpdate> =
        combine(
            flow = selection.selected,
            flow2 = refresh.onStart { emit(Unit) },
        ) { place, _ -> place }
            .flatMapLatest { place ->
                if (place == null) flowOf(ForecastUpdate.NoPlace) else fetch(place)
            }

    /**
     * A repository answering from storage emits twice, so a stored forecast is reported as standing
     * for now and the fetched one settles it.
     */
    private fun fetch(place: SelectedPlace): Flow<ForecastUpdate> =
        forecasts.forecast(at = place)
            .map { read ->
                when (read) {
                    is ForecastRead.Cached -> ForecastUpdate.Stale(read.forecast)
                    is ForecastRead.Fresh -> ForecastUpdate.Ready(read.forecast)
                }
            }
            .onStart { emit(ForecastUpdate.Fetching) }
            // A canceled fetch is not a failure to report: the reader has already moved on.
            .catch { cause ->
                if (cause is CancellationException) throw cause
                emit(ForecastUpdate.Failed(cause))
            }
}
