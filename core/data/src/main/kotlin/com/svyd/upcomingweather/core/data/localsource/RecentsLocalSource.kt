package com.svyd.upcomingweather.core.data.localsource

import com.svyd.upcomingweather.core.data.localsource.dto.StoredPlace

/** The places already looked at, most recent first. */
internal interface RecentsLocalSource {

    suspend fun recentPlaces(): List<StoredPlace>

    /** Files [place] at the front, once, keeping at most [limit]. */
    suspend fun remember(place: StoredPlace, limit: Int)
}
