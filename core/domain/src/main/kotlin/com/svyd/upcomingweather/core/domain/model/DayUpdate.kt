package com.svyd.upcomingweather.core.domain.model

import java.time.Instant

/**
 * What is known about one day of the forecast, as it changes.
 *
 * Two cases against [ForecastUpdate]'s five, because a day is read from storage and never fetched
 * on its own: there is no request of its own to be in flight and none to fail. A stored day cannot
 * be taken off the screen by anything except a newer forecast that does not reach it.
 */
sealed interface DayUpdate {

    /** A day, as it is stored. [retrievedAt] is when the forecast it came from was fetched. */
    data class Ready(val day: DayForecast, val retrievedAt: Instant) : DayUpdate

    /** No such day in the stored forecast, nothing stored, or no place chosen. */
    data object Unavailable : DayUpdate
}
