package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.Place
import kotlinx.coroutines.flow.Flow

/** The places already looked at. */
interface RecentPlacesRepository {

    /** Most recent first, and reported again whenever the list changes. */
    val recentPlaces: Flow<List<Place>>

    /**
     * Files [place] at the front, once, keeping at most [limit].
     */
    suspend fun remember(place: Place, limit: Int)
}
