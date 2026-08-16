package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * The places already looked at, most recent first, reported again whenever one is added.
 *
 * A store that cannot be read is an empty list rather than a failure: the recents are a convenience
 * offered beside a search field that works without them, and there is nothing for a reader to do
 * about a disk that will not answer.
 */
class ObserveRecentPlaces(private val recents: RecentPlacesRepository) {
    operator fun invoke(): Flow<List<Place>> = recents.recentPlaces.catch { emit(emptyList()) }
}
