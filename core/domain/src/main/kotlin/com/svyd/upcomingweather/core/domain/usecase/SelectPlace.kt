package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.failure.catching
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository

/**
 * Settles on a place the user named: it becomes the one being reported on, and it joins the ones
 * already looked at. The two happen together, so no caller can do one and forget the other.
 *
 * Only [RECENTS_LIMIT] places are kept — a list of everywhere ever searched is not a shortcut. How
 * that list is kept in order and free of duplicates is the store's business; how long it is, is
 * this one's.
 */
class SelectPlace(
    private val selection: SelectedPlaceRepository,
    private val recents: RecentPlacesRepository,
) {

    suspend operator fun invoke(place: Place): Result<Unit> = catching {
        selection.select(
            place = SelectedPlace(
                label = PlaceLabel.Named(name = place.name),
                coordinates = place.coordinates,
            )
        )

        recents.remember(
            place = place,
            limit = RECENTS_LIMIT,
        )
    }

    companion object {
        const val RECENTS_LIMIT = 15
    }
}
