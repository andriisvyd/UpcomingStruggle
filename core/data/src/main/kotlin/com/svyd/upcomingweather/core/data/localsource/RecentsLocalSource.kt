package com.svyd.upcomingweather.core.data.localsource

import com.svyd.upcomingweather.core.data.localsource.dto.StoredPlace
import kotlinx.coroutines.flow.Flow

/** The places already looked at, most recent first. */
internal interface RecentsLocalSource {

    /** Reports again on every change, so a screen listing them never lists a stale set. */
    val recentPlaces: Flow<List<StoredPlace>>

    /** Files [place] at the front, once, keeping at most [limit]. */
    suspend fun remember(place: StoredPlace, limit: Int)
}
