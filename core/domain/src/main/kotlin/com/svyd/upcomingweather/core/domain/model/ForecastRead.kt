package com.svyd.upcomingweather.core.domain.model

/**
 * A forecast, and how it was come by.
 *
 * [Cached] and [Stale] both come from storage and differ only in age: one is inside the window the
 * caller asked for, the other is past it with a fetch under way behind it.
 */
sealed interface ForecastRead {

    /** Stored, and young enough that nothing was fetched. Nothing follows it. */
    data class Cached(val forecast: Forecast) : ForecastRead

    /** Stored, but older than asked for. A [Fresh] follows unless the fetch fails. */
    data class Stale(val forecast: Forecast) : ForecastRead

    /** Straight from the provider. */
    data class Fresh(val forecast: Forecast) : ForecastRead
}
