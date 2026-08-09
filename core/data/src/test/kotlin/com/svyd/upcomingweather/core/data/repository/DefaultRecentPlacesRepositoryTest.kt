package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.RecentsLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredPlace
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultRecentPlacesRepositoryTest {

    @Test
    fun `what the store holds comes back as places`() = runTest {
        val recents = FakeRecents(listOf(budapest.stored()))

        assertEquals(listOf(budapest), DefaultRecentPlacesRepository(recents).recentPlaces())
    }

    /** How many to keep is the use case's rule, so it has to reach the store unchanged. */
    @Test
    fun `the limit is passed through to the store`() = runTest {
        val recents = FakeRecents()

        DefaultRecentPlacesRepository(recents).remember(budapest, limit = 3)

        assertEquals(listOf(budapest.stored() to 3), recents.remembered)
    }

    private class FakeRecents(private val held: List<StoredPlace> = emptyList()) :
        RecentsLocalSource {
        val remembered = mutableListOf<Pair<StoredPlace, Int>>()

        override suspend fun recentPlaces(): List<StoredPlace> = held

        override suspend fun remember(place: StoredPlace, limit: Int) {
            remembered += place to limit
        }
    }

    private companion object {
        val budapest = Place(
            id = "3054643",
            name = "Budapest",
            region = "Budapest",
            country = "Hungary",
            coordinates = Coordinates(latitude = 47.5, longitude = 19.04),
        )

        fun Place.stored() = StoredPlace(
            id = id,
            name = name,
            region = region,
            country = country,
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
        )
    }
}
