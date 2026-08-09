package com.svyd.upcomingweather.core.domain.model

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * The questions asked of a [Forecast] often enough that every caller would otherwise answer them
 * again.
 *
 * All three convert into the forecast's own zone first. That conversion is the invariant worth
 * keeping in one place: a caller doing it itself is a caller that can get it wrong, and reading a
 * foreign city near midnight is where it shows.
 */

/** The day [date] falls on, or null when the forecast does not reach that far. */
fun Forecast.day(date: LocalDate): DayForecast? = days.firstOrNull { it.date == date }

/**
 * The day [moment] falls on where the weather is, which is not always the day it is where the
 * reader is: a forecast for Tokyo read from Budapest is most of a day ahead.
 */
fun Forecast.today(moment: ZonedDateTime): DayForecast? =
    day(moment.withZoneSameInstant(timeZone).toLocalDate())

/**
 * Up to [count] hours starting with the one [moment] falls in, running past midnight into the days
 * that follow.
 *
 * The hour in progress counts as the first: at 14:20 the run starts at 14:00, because that is the
 * hour the reader is standing in. Returns fewer than asked for when the forecast runs out.
 */
fun Forecast.hoursFrom(moment: ZonedDateTime, count: Int): List<HourConditions> {
    if (count <= 0) return emptyList()
    val from = moment.withZoneSameInstant(timeZone).truncatedTo(ChronoUnit.HOURS)
    return days.asSequence()
        .flatMap { it.hours }
        .dropWhile { it.time.isBefore(from) }
        .take(count)
        .toList()
}
