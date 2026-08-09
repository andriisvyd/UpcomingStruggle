package com.svyd.upcomingweather.core.data.cloud.dto

import kotlinx.serialization.Serializable

/**
 * Open-Meteo's place search.
 *
 * [results] is absent rather than empty when nothing matches, which is why it is nullable here.
 */
@Serializable
data class SearchResponse(
    val results: List<PlaceDto>? = null,
)

@Serializable
data class PlaceDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    /** The first-level division: a state, a county, a region. Absent for some places. */
    val admin1: String? = null,
)
