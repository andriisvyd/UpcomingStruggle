package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.RecentsLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredPlace
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultRecentPlacesRepositoryTest {

    /**
     * The recents are kept alive in the test's own scope, so the sharing settles when the test
     * scheduler says rather than on a dispatcher nothing here controls.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.repository(recents: RecentsLocalSource) =
        DefaultRecentPlacesRepository(
            recents = recents,
            scope = CoroutineScope(
                backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler),
            ),
        )

    @Test
    fun `what the store holds comes back as places`() = runTest {
        val recents = FakeRecents(listOf(budapest.stored()))

        assertEquals(listOf(budapest), repository(recents).recentPlaces.first())
    }

    /** The list is followed, not read once: filing a place reaches whoever is already listing them. */
    @Test
    fun `a place filed after the list was read is reported`() = runTest {
        val recents = FakeRecents()
        val repository = repository(recents)
        assertEquals(emptyList<Place>(), repository.recentPlaces.first())

        recents.hold(listOf(budapest.stored()))

        assertEquals(listOf(budapest), repository.recentPlaces.first())
    }

    /** How many to keep is the use case's rule, so it has to reach the store unchanged. */
    @Test
    fun `the limit is passed through to the store`() = runTest {
        val recents = FakeRecents()

        repository(recents).remember(budapest, limit = 3)

        assertEquals(listOf(budapest.stored() to 3), recents.remembered)
    }

    private class FakeRecents(held: List<StoredPlace> = emptyList()) : RecentsLocalSource {
        private val state = MutableStateFlow(held)
        val remembered = mutableListOf<Pair<StoredPlace, Int>>()

        override val recentPlaces: Flow<List<StoredPlace>> = state

        override suspend fun remember(place: StoredPlace, limit: Int) {
            remembered += place to limit
        }

        fun hold(places: List<StoredPlace>) {
            state.value = places
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
