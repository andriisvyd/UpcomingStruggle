package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import kotlinx.coroutines.flow.Flow

/**
 * Where forecasts come from.
 *
 * Takes the whole place rather than its coordinates: providers report on a grid cell and cannot say
 * what it is called, so a name already in hand is the best one there is.
 *
 * Emits what is stored for that place, if anything, and then what the provider returns — so a
 * reader sees something immediately and the fresh answer replaces it.
 */
interface ForecastRepository {

    /**
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.NoConnection
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.ServiceUnavailable
     */
    fun forecast(at: SelectedPlace): Flow<ForecastRead>
}
