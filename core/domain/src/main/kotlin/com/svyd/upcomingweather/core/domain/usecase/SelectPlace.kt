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
 * Recents run most recent first, each place once, and only [RECENTS_LIMIT] are kept — a list of
 * everywhere ever searched is not a shortcut. Choosing a place again moves it to the front rather
 * than adding it twice.
 */
class SelectPlace(
    private val selection: SelectedPlaceRepository,
    private val recents: RecentPlacesRepository,
) {

    suspend operator fun invoke(place: Place): Result<Unit> = catching {
        selection.select(
            place = SelectedPlace(
                label = PlaceLabel.Named(place.name),
                coordinates = place.coordinates
            )
        )

        val remembered = recents.recentPlaces()
            .filterNot { it.id == place.id }
            .let { listOf(place) + it }
            .take(RECENTS_LIMIT)

        recents.save(remembered)
    }

    companion object {
        const val RECENTS_LIMIT = 5
    }
}
