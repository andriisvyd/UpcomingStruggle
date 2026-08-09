package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.SelectedPlace

/**
 * Where forecasts come from.
 *
 * Takes the whole place rather than its coordinates: providers report on a grid cell and cannot say
 * what it is called, so a name already in hand is the best one there is, and looking one up is the
 * fallback for a place that arrived without.
 */
interface ForecastRepository {

    /**
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.NoConnection
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.ServiceUnavailable
     */
    suspend fun forecast(at: SelectedPlace): Forecast
}
