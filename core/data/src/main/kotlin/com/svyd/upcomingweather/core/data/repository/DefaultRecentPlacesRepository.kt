package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.RecentsLocalSource
import com.svyd.upcomingweather.core.data.localsource.mapper.toStored
import com.svyd.upcomingweather.core.data.localsource.mapper.toPlace
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository

internal class DefaultRecentPlacesRepository(
    private val recents: RecentsLocalSource,
) : RecentPlacesRepository {

    override suspend fun recentPlaces(): List<Place> =
        recents.recentPlaces().map { it.toPlace() }

    override suspend fun remember(place: Place, limit: Int) {
        recents.remember(place = place.toStored(), limit = limit)
    }
}
