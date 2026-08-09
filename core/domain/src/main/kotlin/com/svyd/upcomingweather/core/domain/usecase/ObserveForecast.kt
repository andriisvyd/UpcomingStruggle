package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.failure.catching
import com.svyd.upcomingweather.core.domain.model.ForecastUpdate
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart

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
            selection.selected,
            refresh.onStart { emit(Unit) },
        ) { place, _ -> place }
            .flatMapLatest { place ->
                if (place == null) flowOf(ForecastUpdate.NoPlace) else fetch(place)
            }

    private fun fetch(place: SelectedPlace): Flow<ForecastUpdate> = flow {
        emit(ForecastUpdate.Fetching)
        emit(
            catching { forecasts.forecast(place) }.fold(
                onSuccess = { ForecastUpdate.Ready(it) },
                onFailure = { ForecastUpdate.Failed(it) },
            ),
        )
    }
}
