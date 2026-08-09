package com.svyd.upcomingweather.core.domain.model

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Everything known about the weather at one place.
 *
 * Times are carried in the place's own zone rather than the device's, so a forecast for Tokyo read
 * from Budapest still says when the sun comes up in Tokyo.
 */
data class Forecast(
    /**
     * What to call the place this is for. The provider reports on a grid cell and has no idea what
     * town sits under it, so the label is put here on the way through rather than read from the
     * response.
     */
    val label: PlaceLabel,
    val timeZone: ZoneId,
    val current: Conditions,
    /** Today first, one entry per day. */
    val days: List<DayForecast>,
)

/** The weather at a single moment, as fully as a provider reports it. */
data class Conditions(
    val observedAt: ZonedDateTime,
    val condition: Condition,
    val partOfDay: PartOfDay,
    val remark: Remark,
    val temperature: Temperature,
    val feelsLike: Temperature,
    val humidity: Percentage,
    val cloudCover: Percentage,
    val wind: Wind,
    val pressure: AirPressure,
)

data class Wind(
    val speed: Speed,
    /** Absent where the provider reports no gust separate from the steady wind. */
    val gust: Speed?,
)

/**
 * Two pressures for one sky: [seaLevel] is the figure a forecast is normally quoted in, [surface]
 * the one actually pressing on the place, which differs with altitude.
 */
data class AirPressure(
    val seaLevel: Pressure,
    val surface: Pressure,
)

/** One day, with the hours that make it up. */
data class DayForecast(
    val date: LocalDate,
    val condition: Condition,
    val minimum: Temperature,
    val maximum: Temperature,
    val precipitationChance: Percentage,
    val precipitation: Millimetres,
    val sunrise: ZonedDateTime,
    val sunset: ZonedDateTime,
    /** In order, from the first hour of the day to the last. */
    val hours: List<HourConditions>,
)

data class HourConditions(
    val time: ZonedDateTime,
    val condition: Condition,
    val partOfDay: PartOfDay,
    val temperature: Temperature,
    val precipitationChance: Percentage,
)
