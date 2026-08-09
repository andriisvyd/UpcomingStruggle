package com.svyd.upcomingweather.core.domain.model

/**
 * What the app is currently reporting on.
 *
 * Less than a [Place]: an id, a region and a country matter to a list of search results, and mean
 * nothing to "where I am standing". All a forecast needs is somewhere to ask about and something to
 * call it.
 */
data class SelectedPlace(
    val label: PlaceLabel,
    val coordinates: Coordinates,
)
