package com.svyd.upcomingweather.core.domain.model

/**
 * Somewhere a forecast can be asked for.
 *
 * [id] is whatever the geocoder calls this place — opaque above the data layer, and there so that
 * two places sharing a name are still two places.
 */
data class Place(
    val id: String,
    val name: String,
    /** The first-level division a country is cut into — a state, a county, a region. */
    val region: String?,
    val country: String,
    val coordinates: Coordinates,
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)
