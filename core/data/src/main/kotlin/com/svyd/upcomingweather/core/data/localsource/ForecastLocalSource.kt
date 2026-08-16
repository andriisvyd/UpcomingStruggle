package com.svyd.upcomingweather.core.data.localsource

import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.domain.model.Coordinates
import kotlinx.coroutines.flow.Flow

/** Forecasts already fetched, kept against the place they were fetched for. */
internal interface ForecastLocalSource {

    /**
     * What is kept for [at], as it changes, and null while nothing is.
     *
     * A stream rather than a reading, because this is the one copy of a forecast the app has: two
     * screens can read it at once, a save is what tells both of them, and neither has to know the
     * other is there.
     */
    fun forecast(at: Coordinates): Flow<StoredForecast?>

    suspend fun save(at: Coordinates, forecast: StoredForecast)
}
