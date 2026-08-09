package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.failure.catching
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository

/**
 * Settles on wherever the device is.
 *
 * Unlike a place picked out of a search, this one is not filed among the recents: "where I was on
 * Tuesday" is not a shortcut anybody wants to tap.
 *
 * Fails when location is refused or unavailable; the caller decides what to say about that.
 */
class SelectCurrentPlace(
    private val places: PlaceRepository,
    private val selection: SelectedPlaceRepository,
) {

    suspend operator fun invoke(): Result<Unit> = catching {
        selection.select(places.currentPlace())
    }
}
