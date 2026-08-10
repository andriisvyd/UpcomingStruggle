package com.svyd.upcomingweather.core.domain.model

import java.time.Instant

/**
 * What is known about one day of the forecast, as it changes.
 *
 * Fewer cases than [ForecastUpdate], because a single day is only ever read and never refreshed on
 * its own: a forecast that is missing, still arriving or failed all leave nothing of that day to
 * show, and [Unavailable] covers the three.
 */
sealed interface DayUpdate {

    /** Nothing to show yet. */
    data object Fetching : DayUpdate

    /** A stored day, older than asked for, with a fetch under way behind it. */
    data class Stale(val day: DayForecast, val retrievedAt: Instant) : DayUpdate

    /** A day, settled. */
    data class Ready(val day: DayForecast, val retrievedAt: Instant) : DayUpdate

    /** No such day in the forecast, or no forecast to look in. */
    data object Unavailable : DayUpdate
}
