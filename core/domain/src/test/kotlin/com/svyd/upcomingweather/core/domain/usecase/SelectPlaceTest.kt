package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectPlaceTest {

    @Test
    fun `choosing a place selects it and files it in one go`() = runTest {
        val selection = InMemorySelection()
        val recents = InMemoryRecents()

        SelectPlace(selection, recents)(budapest).getOrThrow()

        assertEquals(
            SelectedPlace(PlaceLabel.Named("Budapest"), budapest.coordinates),
            selection.selected.first(),
        )
        assertEquals(listOf(budapest), recents.recentPlaces())
    }

    /** Only what a forecast needs survives the selection: somewhere to ask, something to call it. */
    @Test
    fun `the id, region and country are left behind`() = runTest {
        val selection = InMemorySelection()

        SelectPlace(selection, InMemoryRecents())(londonOhio).getOrThrow()

        assertEquals(
            SelectedPlace(PlaceLabel.Named("London"), londonOhio.coordinates),
            selection.selected.first(),
        )
    }

    @Test
    fun `the newest place goes to the front`() = runTest {
        val recents = InMemoryRecents(listOf(lisbon, tokyo))

        SelectPlace(InMemorySelection(), recents)(budapest).getOrThrow()

        assertEquals(listOf(budapest, lisbon, tokyo), recents.recentPlaces())
    }

    @Test
    fun `choosing a place again moves it up rather than repeating it`() = runTest {
        val recents = InMemoryRecents(listOf(lisbon, budapest, tokyo))

        SelectPlace(InMemorySelection(), recents)(budapest).getOrThrow()

        assertEquals(listOf(budapest, lisbon, tokyo), recents.recentPlaces())
    }

    @Test
    fun `two places that share a name are still two places`() = runTest {
        val recents = InMemoryRecents(listOf(londonEngland))

        SelectPlace(InMemorySelection(), recents)(londonOhio).getOrThrow()

        assertEquals(listOf(londonOhio, londonEngland), recents.recentPlaces())
    }

    @Test
    fun `the oldest place falls off the end`() = runTest {
        val full = List(SelectPlace.RECENTS_LIMIT) { index -> place("old-$index", "Place $index") }
        val recents = InMemoryRecents(full)

        SelectPlace(InMemorySelection(), recents)(budapest).getOrThrow()

        val remembered = recents.recentPlaces()
        assertEquals(SelectPlace.RECENTS_LIMIT, remembered.size)
        assertEquals(budapest, remembered.first())
        assertTrue("the oldest place should have been dropped", full.last() !in remembered)
    }

    @Test
    fun `a failing store leaves a failed result`() = runTest {
        val boom = IllegalStateException("disk full")
        val recents = object : RecentPlacesRepository {
            override suspend fun recentPlaces(): List<Place> = emptyList()
            override suspend fun save(places: List<Place>) = throw boom
        }

        val result = SelectPlace(InMemorySelection(), recents)(budapest)

        assertTrue(result.isFailure)
        assertEquals(boom, result.exceptionOrNull())
    }

    private class InMemorySelection : SelectedPlaceRepository {
        private val state = MutableStateFlow<SelectedPlace?>(null)
        override val selected: Flow<SelectedPlace?> = state

        override suspend fun select(place: SelectedPlace) {
            state.value = place
        }
    }

    private class InMemoryRecents(initial: List<Place> = emptyList()) : RecentPlacesRepository {
        private var places = initial

        override suspend fun recentPlaces(): List<Place> = places

        override suspend fun save(places: List<Place>) {
            this.places = places
        }
    }

    private companion object {
        fun place(
            id: String,
            name: String,
            region: String? = null,
            country: String = "Hungary",
            latitude: Double = 0.0,
            longitude: Double = 0.0,
        ) = Place(
            id = id,
            name = name,
            region = region,
            country = country,
            coordinates = Coordinates(latitude, longitude),
        )

        val budapest = place("3054643", "Budapest", "Budapest", latitude = 47.49835, longitude = 19.04045)
        val lisbon = place("2267057", "Lisbon", country = "Portugal")
        val tokyo = place("1850147", "Tokyo", country = "Japan")
        val londonEngland = place("2643743", "London", "England", "United Kingdom")
        val londonOhio = place("4517009", "London", "Ohio", "United States", 39.8865, -83.4483)
    }
}
