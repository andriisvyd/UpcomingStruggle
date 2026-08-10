package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.data.cloud.dto.CurrentDto
import com.svyd.upcomingweather.core.data.cloud.dto.DailyDto
import com.svyd.upcomingweather.core.data.cloud.dto.ForecastResponse
import com.svyd.upcomingweather.core.data.cloud.dto.HourlyDto
import com.svyd.upcomingweather.core.domain.model.AirPressure
import com.svyd.upcomingweather.core.domain.model.Conditions
import com.svyd.upcomingweather.core.domain.model.DayForecast
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.HourConditions
import com.svyd.upcomingweather.core.domain.model.Millimetres
import com.svyd.upcomingweather.core.domain.model.PartOfDay
import com.svyd.upcomingweather.core.domain.model.Percentage
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.Pressure
import com.svyd.upcomingweather.core.domain.model.Speed
import com.svyd.upcomingweather.core.domain.model.Temperature
import com.svyd.upcomingweather.core.domain.model.Wind
import com.svyd.upcomingweather.core.domain.model.remarkFor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Turns a forecast response into the domain's [Forecast].
 *
 * The label and [retrievedAt] are carried in rather than taken from the response: the provider
 * reports on a grid cell, so it knows neither what town sits under it nor when anyone asked.
 */
internal fun ForecastResponse.toForecast(label: PlaceLabel, retrievedAt: Instant): Forecast {
    val zone = ZoneId.of(timezone)
    val hoursByDate = hourly.toHours(zone).groupBy { it.time.toLocalDate() }

    return Forecast(
        label = label,
        timeZone = zone,
        retrievedAt = retrievedAt,
        current = current.toConditions(zone),
        days = daily.toDays(zone, hoursByDate),
    )
}

private fun CurrentDto.toConditions(zone: ZoneId): Conditions {
    val condition = conditionOf(weatherCode)
    val partOfDay = partOfDay(isDay)

    return Conditions(
        observedAt = time.atZone(zone),
        condition = condition,
        partOfDay = partOfDay,
        remark = remarkFor(condition, partOfDay),
        temperature = Temperature(temperature),
        feelsLike = Temperature(feelsLike),
        humidity = Percentage(humidity),
        cloudCover = Percentage(cloudCover),
        wind = Wind(speed = Speed(windSpeed), gust = windGusts?.let(::Speed)),
        pressure = AirPressure(
            seaLevel = Pressure(seaLevelPressure),
            surface = Pressure(surfacePressure),
        ),
    )
}

/**
 * Zips the parallel hourly arrays into one list.
 *
 * The arrays are read to the length of the shortest: a response whose columns disagree is broken,
 * and dropping the ragged tail loses less than an index out of bounds does.
 */
private fun HourlyDto.toHours(zone: ZoneId): List<HourConditions> {
    val size = minOf(
        time.size,
        temperature.size,
        weatherCode.size,
        isDay.size,
        precipitationProbability.size,
    )

    return (0 until size).map { index ->
        val partOfDay = partOfDay(isDay[index])
        HourConditions(
            time = time[index].atZone(zone),
            condition = conditionOf(weatherCode[index]),
            partOfDay = partOfDay,
            temperature = Temperature(temperature[index]),
            precipitationChance = Percentage(precipitationProbability[index] ?: 0),
        )
    }
}

private fun DailyDto.toDays(
    zone: ZoneId,
    hoursByDate: Map<LocalDate, List<HourConditions>>,
): List<DayForecast> {
    val size = minOf(
        time.size,
        weatherCode.size,
        maximum.size,
        minimum.size,
        sunrise.size,
        sunset.size,
    )

    return (0 until size).map { index ->
        val date = LocalDate.parse(time[index])
        DayForecast(
            date = date,
            condition = conditionOf(weatherCode[index]),
            minimum = Temperature(minimum[index]),
            maximum = Temperature(maximum[index]),
            precipitationChance = Percentage(precipitationProbability.getOrNull(index) ?: 0),
            precipitation = Millimetres(precipitationSum.getOrNull(index) ?: 0.0),
            sunrise = sunrise[index].atZone(zone),
            sunset = sunset[index].atZone(zone),
            hours = hoursByDate[date].orEmpty(),
        )
    }
}

/** The provider reports day and night as one and zero. */
private fun partOfDay(isDay: Int): PartOfDay = if (isDay == 1) PartOfDay.Day else PartOfDay.Night

/**
 * Times arrive local to the place and without an offset — "2026-08-09T15:15". The zone comes from
 * the response's own `timezone` field, so the forecast keeps the place's clock rather than the
 * reader's.
 */
private fun String.atZone(zone: ZoneId): ZonedDateTime =
    LocalDateTime.parse(this).atZone(zone)
