package com.svyd.upcomingweather.core.data.localsource

import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.domain.model.Coordinates

/** Forecasts already fetched, kept against the place they were fetched for. */
internal interface ForecastLocalSource {

    /** What was kept for [at], or null when nothing was. */
    suspend fun forecast(at: Coordinates): StoredForecast?

    suspend fun save(at: Coordinates, forecast: StoredForecast)
}
