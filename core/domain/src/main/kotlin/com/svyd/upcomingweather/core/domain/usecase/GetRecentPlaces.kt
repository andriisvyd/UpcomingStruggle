package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.failure.catching
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository

/** The places already looked at, most recent first. */
class GetRecentPlaces(private val recents: RecentPlacesRepository) {

    suspend operator fun invoke(): Result<List<Place>> = catching { recents.recentPlaces() }
}
