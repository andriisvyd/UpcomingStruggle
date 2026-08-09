package com.svyd.upcomingweather.core.domain.model

/**
 * What to call the place a forecast is for.
 *
 * There is exactly one way to
 * end up without name — standing somewhere no Geocoder can put a name to — and saying so here is
 * what lets the screen fall back to "current location" as a fact rather than a guess.
 */
sealed interface PlaceLabel {

    /** Named, because the user searched for it or a Geocoder recognized the position. */
    data class Named(val placeName: String) : PlaceLabel

    /** The device's own position, which nothing could name. */
    data object NamelessCurrentLocation : PlaceLabel
}
