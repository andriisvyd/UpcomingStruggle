package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.RecentsLocalSource
import com.svyd.upcomingweather.core.data.localsource.mapper.toStored
import com.svyd.upcomingweather.core.data.localsource.mapper.toPlace
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * The places already looked at, kept across launches.
 *
 * Held alive in [scope] with its last value, like the selection is: the search screen lists these
 * the instant it opens, and a list that has to be read off disk first is a list that arrives after
 * the screen it belongs to. It is read once and then written to rarely — choosing a city is the
 * only thing that changes it — so keeping it costs one small list and saves the wait every time.
 */
internal class DefaultRecentPlacesRepository(
    private val recents: RecentsLocalSource,
    scope: CoroutineScope,
) : RecentPlacesRepository {

    override val recentPlaces: Flow<List<Place>> = recents.recentPlaces
        .map { stored -> stored.map { it.toPlace() } }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = STOP_TIMEOUT,
                replayExpirationMillis = Long.MAX_VALUE,
            ),
            replay = 1,
        )

    override suspend fun remember(place: Place, limit: Int) {
        recents.remember(place = place.toStored(), limit = limit)
    }

    private companion object {
        /** Long enough to cover a screen change, short enough not to outlive an app in the background. */
        const val STOP_TIMEOUT = 5_000L
    }
}
