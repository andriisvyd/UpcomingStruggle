package com.svyd.upcomingweather.core.domain.model

/**
 * What is known about the forecast right now, as it changes.
 *
 * Not a screen state — it says what the app is doing, not what it draws. Whether [Fetching] looks
 * like an empty page typing itself in or a spinner over yesterday's numbers depends on what the
 * reader can already see, and only the caller knows that.
 */
sealed interface ForecastUpdate {

    /** No place has been chosen yet. First launch, and nothing to fetch. */
    data object NoPlace : ForecastUpdate

    /** A fetch is in flight. */
    data object Fetching : ForecastUpdate

    /**
     * A stored forecast, drawn while a fresh one is still being fetched. Worth showing at once, and
     * worth marking as not the last word.
     */
    data class Stale(val forecast: Forecast) : ForecastUpdate

    /** A forecast, settled. Nothing further is on its way. */
    data class Ready(val forecast: Forecast) : ForecastUpdate

    /** The fetch failed. */
    data class Failed(val cause: Throwable) : ForecastUpdate
}
