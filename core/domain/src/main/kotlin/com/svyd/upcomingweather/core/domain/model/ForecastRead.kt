package com.svyd.upcomingweather.core.domain.model

/**
 * A forecast as storage reports it, and what the fetch behind it is doing.
 *
 * [Cached] and [Stale] both come from storage and differ only in age: one is inside the window the
 * caller asked for, the other is past it with a fetch under way behind it. A fetched forecast is
 * not a case of its own — it is written to storage, and storage says so.
 */
sealed interface ForecastRead {

    /** Stored, and young enough that nothing is on its way. */
    data class Cached(val forecast: Forecast) : ForecastRead

    /** Stored, but older than asked for. A fetch is under way behind it. */
    data class Stale(val forecast: Forecast) : ForecastRead

    /**
     * The fetch failed. Whatever is stored is unchanged and still stands.
     *
     * Reported rather than thrown, because throwing ends the stream: storage would go on changing
     * with nobody left listening, and one lost connection would stop a screen updating for as long
     * as it stayed open.
     */
    data class Failed(val cause: Throwable) : ForecastRead
}
