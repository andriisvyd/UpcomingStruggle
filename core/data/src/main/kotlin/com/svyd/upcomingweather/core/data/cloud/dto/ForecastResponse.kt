package com.svyd.upcomingweather.core.data.cloud.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Open-Meteo's forecast, as it arrives.
 *
 * Hours and days come back as parallel arrays rather than lists of objects: `time[3]`,
 * `temperature_2m[3]` and `weather_code[3]` are three facts about the same hour. Zipping them by
 * index is the mapper's first job, and a response where those arrays disagree in length is a
 * response that cannot be read.
 *
 * Times are local to [timezone] and carry no offset of their own — "2026-08-09T15:15".
 */
@Serializable
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("utc_offset_seconds") val utcOffsetSeconds: Int,
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("apparent_temperature") val feelsLike: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("surface_pressure") val surfacePressure: Double,
    @SerialName("pressure_msl") val seaLevelPressure: Double,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("wind_gusts_10m") val windGusts: Double?,
    @SerialName("cloud_cover") val cloudCover: Int,
)

@Serializable
data class HourlyDto(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("is_day") val isDay: List<Int>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?>,
)

@Serializable
data class DailyDto(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val maximum: List<Double>,
    @SerialName("temperature_2m_min") val minimum: List<Double>,
    @SerialName("precipitation_probability_max") val precipitationProbability: List<Int?>,
    @SerialName("precipitation_sum") val precipitationSum: List<Double?>,
    val sunrise: List<String>,
    val sunset: List<String>,
)
