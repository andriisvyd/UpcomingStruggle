package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.data.cloud.dto.PlaceDto
import com.svyd.upcomingweather.core.data.cloud.dto.SearchResponse
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place

/**
 * Search results as places.
 *
 * A result without a country is dropped: the row would read as a bare name with nothing to tell it
 * apart from the four other towns of the same name above it.
 */
internal fun SearchResponse.toPlaces(): List<Place> =
    results.orEmpty().mapNotNull { it.toPlaceOrNull() }

private fun PlaceDto.toPlaceOrNull(): Place? {
    val country = country ?: return null
    return Place(
        id = id.toString(),
        name = name,
        region = admin1,
        country = country,
        coordinates = Coordinates(latitude = latitude, longitude = longitude),
    )
}
