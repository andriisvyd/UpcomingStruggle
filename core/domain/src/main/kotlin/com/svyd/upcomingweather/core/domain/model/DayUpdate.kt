package com.svyd.upcomingweather.core.domain.model

import java.time.Instant

/**
 * What is known about one day of the forecast, as it changes.
 *
 * Fewer cases than [ForecastUpdate], because a single day is only ever read and never refreshed on
 * its own. [Failed] is kept apart from [Unavailable] all the same: a fetch that did not land says
 * nothing about whether the day is in the forecast, and a stored day is very often already drawn
 * by the time it fails.
 */
sealed interface DayUpdate {

    /** Nothing to show yet. */
    data object Fetching : DayUpdate

    /** A stored day, older than asked for, with a fetch under way behind it. */
    data class Stale(val day: DayForecast, val retrievedAt: Instant) : DayUpdate

    /** A day, settled. */
    data class Ready(val day: DayForecast, val retrievedAt: Instant) : DayUpdate

    /** The forecast could not be fetched. Whatever was stored is still what it was. */
    data object Failed : DayUpdate

    /** No such day in the forecast, or no forecast to look in. */
    data object Unavailable : DayUpdate
}
