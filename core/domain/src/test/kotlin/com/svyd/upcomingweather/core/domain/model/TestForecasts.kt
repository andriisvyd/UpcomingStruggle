package com.svyd.upcomingweather.core.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Builders for forecasts that are only as specific as a test needs.
 *
 * Everything not named by a test is filled with something plausible and ignored.
 */
internal fun forecast(
    zone: ZoneId,
    from: LocalDate,
    days: Int,
    hoursPerDay: Int = 24,
    label: PlaceLabel = PlaceLabel.Named("Budapest"),
    retrievedAt: Instant = Instant.parse("2026-08-09T12:00:00Z"),
): Forecast = Forecast(
    label = label,
    timeZone = zone,
    retrievedAt = retrievedAt,
    current = conditions(from.atStartOfDay(zone)),
    days = (0 until days).map { offset ->
        dayForecast(zone, from.plusDays(offset.toLong()), hoursPerDay)
    },
)

internal fun dayForecast(
    zone: ZoneId,
    date: LocalDate,
    hoursPerDay: Int = 24,
): DayForecast = DayForecast(
    date = date,
    condition = Condition.Clear,
    minimum = Temperature(18.0),
    maximum = Temperature(28.0),
    precipitationChance = Percentage(0),
    precipitation = Millimetres(0.0),
    sunrise = date.atStartOfDay(zone).plusHours(5),
    sunset = date.atStartOfDay(zone).plusHours(20),
    hours = (0 until hoursPerDay).map { hour ->
        HourConditions(
            time = date.atStartOfDay(zone).plusHours(hour.toLong()),
            condition = Condition.Clear,
            partOfDay = if (hour in 6..19) PartOfDay.Day else PartOfDay.Night,
            temperature = Temperature(20.0 + hour),
            precipitationChance = Percentage(0),
        )
    },
)

internal fun conditions(at: ZonedDateTime): Conditions = Conditions(
    observedAt = at,
    condition = Condition.Clear,
    partOfDay = PartOfDay.Day,
    remark = Remark.ClearAndBright,
    temperature = Temperature(26.6),
    feelsLike = Temperature(25.4),
    humidity = Percentage(40),
    cloudCover = Percentage(0),
    wind = Wind(speed = Speed(11.9), gust = Speed(30.6)),
    pressure = AirPressure(seaLevel = Pressure(1019.8), surface = Pressure(1006.8)),
)
