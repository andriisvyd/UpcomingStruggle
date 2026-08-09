package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.Place

/** The places already looked at. */
interface RecentPlacesRepository {

    /** Most recent first. */
    suspend fun recentPlaces(): List<Place>

    /**
     * Files [place] at the front, once, keeping at most [limit].
     */
    suspend fun remember(place: Place, limit: Int)
}
