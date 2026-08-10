package com.svyd.upcomingweather.core.domain.model

/**
 * What came of asking about a query.
 *
 * Nothing typed, nothing found, and something found are three different answers that a list cannot
 * keep apart — the first two would both be empty. Only the rule about what counts as a search knows
 * which is which, so it says, rather than leaving the caller to infer it from a length and a size.
 */
sealed interface SearchOutcome {

    /** Too little typed to search on. Nothing was asked of the Geocoder. */
    data object TooShort : SearchOutcome

    /** The Geocoder was asked, and knows no such place. */
    data object NoMatch : SearchOutcome

    /** Places that match, the best first. Never empty — that case is [NoMatch]. */
    data class Found(val places: List<Place>) : SearchOutcome
}
