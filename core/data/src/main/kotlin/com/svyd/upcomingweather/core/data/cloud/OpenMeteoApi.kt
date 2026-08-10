package com.svyd.upcomingweather.core.data.cloud

import com.svyd.upcomingweather.core.data.cloud.dto.ForecastResponse
import com.svyd.upcomingweather.core.data.cloud.dto.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * The forecast endpoint.
 *
 * The requested fields are fixed rather than passed in: they are exactly what the domain model
 * needs, and asking for less would leave a field unfillable while asking for more wastes bytes on
 * every call.
 */
interface ForecastApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = FORECAST_DAYS,
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
    ): ForecastResponse

    companion object {
        const val FORECAST_DAYS = 7

        private const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,weather_code,is_day,relative_humidity_2m," +
                "surface_pressure,pressure_msl,wind_speed_10m,wind_gusts_10m,cloud_cover"

        private const val HOURLY_FIELDS =
            "temperature_2m,weather_code,is_day,precipitation_probability"

        private const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max," +
                "precipitation_sum,sunrise,sunset"
    }
}

/** Place search. A separate host from the forecast, hence a separate service. */
interface SearchApi {

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = RESULT_LIMIT,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json",
    ): SearchResponse

    companion object {
        const val RESULT_LIMIT = 10
    }
}
