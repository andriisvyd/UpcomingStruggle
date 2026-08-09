package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.failure.catching
import com.svyd.upcomingweather.core.domain.model.SearchOutcome
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository

/**
 * Places matching what the user typed.
 *
 * A single character matches most of the world, so anything shorter than [MINIMUM_QUERY_LENGTH] is
 * not a search and never reaches the geocoder. How long to wait before asking is a separate
 * question, and belongs to whoever watches the keystrokes.
 */
class SearchPlaces(private val places: PlaceRepository) {

    suspend operator fun invoke(query: String): Result<SearchOutcome> {
        val trimmed = query.trim()
        if (trimmed.length < MINIMUM_QUERY_LENGTH) return Result.success(SearchOutcome.TooShort)
        return catching {
            val found = places.search(query = trimmed)
            if (found.isEmpty()) SearchOutcome.NoMatch else SearchOutcome.Found(places = found)
        }
    }

    companion object {
        const val MINIMUM_QUERY_LENGTH = 2
    }
}
