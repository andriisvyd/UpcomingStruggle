package com.svyd.upcomingweather.core.domain.model

/**
 * A forecast, and where it came from.
 *
 * A repository that answers from storage first emits twice: what it had, then what it fetched. The
 * two are the same shape and mean different things to a reader — the first is worth drawing
 * immediately and worth marking as not the last word.
 */
sealed interface ForecastRead {

    /** What was already stored. A [Fresh] may still be on its way. */
    data class Cached(val forecast: Forecast) : ForecastRead

    /** Straight off the cloud. */
    data class Fresh(val forecast: Forecast) : ForecastRead
}
