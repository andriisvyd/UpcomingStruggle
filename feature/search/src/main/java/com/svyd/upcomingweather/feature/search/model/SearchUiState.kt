package com.svyd.upcomingweather.feature.search.model

import androidx.compose.runtime.Immutable

/**
 * The search screen's whole world: what is in the field, and what came back.
 *
 * Debouncing, the two-character minimum and request cancellation all happen above this — by the
 * time a state lands here the answer is settled.
 */
@Immutable
data class SearchUiState(
    val query: String = "",
    val results: SearchResultsUi = SearchResultsUi.Recents(emptyList()),
    /** Set once the device has been asked where it is and could not answer. */
    val location: LocationNoticeUi? = null,
)

/**
 * Why letting the phone name the city did not work.
 *
 * Only reasons a reader can act on. Anything else is a defect rather than something to explain, and
 * a row that reported it would offer no way forward.
 */
@Immutable
sealed interface LocationNoticeUi {

    /**
     * Location was refused. [canAskAgain] is what the platform will still do: while it is true the
     * system prompt appears on another attempt, and once it is false only settings can reverse it.
     */
    data class Refused(val canAskAgain: Boolean) : LocationNoticeUi

    /** Location was granted, and the device still could not say where it is. */
    data object Unavailable : LocationNoticeUi
}

@Immutable
sealed interface SearchResultsUi {

    /** Empty query: the cases that went cold, most recent first. */
    data class Recents(val cities: List<CityUi>) : SearchResultsUi

    /** Live geocoding results. */
    data class Cities(val cities: List<CityUi>) : SearchResultsUi

    /**
     * Geocoding came back empty.
     *
     * Carries the query rather than a finished sentence: the message names it, and only the
     * screen can reach the string resource that gets the word order right in every language.
     */
    data class NoResults(val query: String) : SearchResultsUi

    /** Geocoding failed; the row offers to run it again. */
    data object Error : SearchResultsUi
}

@Immutable
data class CityUi(
    /** Stable key for the row. Opaque to the screen, which never reads it. */
    val id: String,
    val name: String,
    /** "California, United States" — the state is dropped when the API returns none. */
    val subtitle: String? = null,
)
