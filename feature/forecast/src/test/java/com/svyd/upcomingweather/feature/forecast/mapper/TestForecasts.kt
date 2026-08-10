package com.svyd.upcomingweather.feature.forecast.mapper

import com.svyd.upcomingweather.core.domain.model.AirPressure
import com.svyd.upcomingweather.core.domain.model.Condition
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
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Forecasts that are only as specific as a test needs; everything unnamed is filled with something
 * plausible and ignored.
 *
 * `:core:domain` has builders of its own, but they sit in that module's test source set and do not
 * travel. These are trimmed to what the mapper is asked about — spans, zones, and the shape of a
 * day — rather than copied.
 */
internal fun forecast(
    zone: ZoneId,
    days: List<DayForecast>,
    current: Conditions = conditions(days.first().date.atStartOfDay(zone)),
    retrievedAt: Instant = RETRIEVED_AT,
    label: PlaceLabel = PlaceLabel.Named(PLACE_NAME),
): Forecast = Forecast(
    label = label,
    timeZone = zone,
    retrievedAt = retrievedAt,
    current = current,
    days = days,
)

/** Consecutive days from [from], all with the same span unless a test says otherwise. */
internal fun week(
    zone: ZoneId,
    from: LocalDate,
    count: Int = 7,
    minimum: Double = DEFAULT_MINIMUM,
    maximum: Double = DEFAULT_MAXIMUM,
): List<DayForecast> = (0 until count).map { offset ->
    dayForecast(zone, from.plusDays(offset.toLong()), minimum = minimum, maximum = maximum)
}

internal fun dayForecast(
    zone: ZoneId,
    date: LocalDate,
    minimum: Double = DEFAULT_MINIMUM,
    maximum: Double = DEFAULT_MAXIMUM,
    condition: Condition = Condition.Clear,
    precipitationChance: Int = 0,
    precipitation: Double = 0.0,
    hours: List<HourConditions> = hoursOf(zone, date),
): DayForecast = DayForecast(
    date = date,
    condition = condition,
    minimum = Temperature(minimum),
    maximum = Temperature(maximum),
    precipitationChance = Percentage(precipitationChance),
    precipitation = Millimetres(precipitation),
    sunrise = date.atStartOfDay(zone).plusHours(SUNRISE_HOUR.toLong()),
    sunset = date.atStartOfDay(zone).plusHours(SUNSET_HOUR.toLong()),
    hours = hours,
)

/** One entry per hour of [date], each a function of the hour so a test can point at a known one. */
internal fun hoursOf(
    zone: ZoneId,
    date: LocalDate,
    count: Int = 24,
    temperature: (Int) -> Double = { 20.0 + it },
    condition: (Int) -> Condition = { Condition.Clear },
    precipitationChance: (Int) -> Int = { 0 },
): List<HourConditions> = (0 until count).map { hour ->
    HourConditions(
        time = date.atStartOfDay(zone).plusHours(hour.toLong()),
        condition = condition(hour),
        partOfDay = if (hour in SUNRISE_HOUR until SUNSET_HOUR) PartOfDay.Day else PartOfDay.Night,
        temperature = Temperature(temperature(hour)),
        precipitationChance = Percentage(precipitationChance(hour)),
    )
}

internal fun conditions(
    at: ZonedDateTime,
    condition: Condition = Condition.Clear,
    partOfDay: PartOfDay = PartOfDay.Day,
    temperature: Double = 26.6,
    feelsLike: Double = 25.4,
    gust: Double? = 30.6,
): Conditions = Conditions(
    observedAt = at,
    condition = condition,
    partOfDay = partOfDay,
    remark = remarkFor(condition, partOfDay),
    temperature = Temperature(temperature),
    feelsLike = Temperature(feelsLike),
    humidity = Percentage(40),
    cloudCover = Percentage(10),
    wind = Wind(speed = Speed(11.9), gust = gust?.let(::Speed)),
    pressure = AirPressure(seaLevel = Pressure(1019.8), surface = Pressure(1006.8)),
)

internal const val PLACE_NAME = "Budapest"
internal val RETRIEVED_AT: Instant = Instant.parse("2026-08-10T03:00:00Z")

private const val DEFAULT_MINIMUM = 18.0
private const val DEFAULT_MAXIMUM = 28.0
private const val SUNRISE_HOUR = 5
private const val SUNSET_HOUR = 20
