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
        val recents = RecordingRecents()

        SelectPlace(selection, recents)(budapest).getOrThrow()

        assertEquals(
            SelectedPlace(PlaceLabel.Named("Budapest"), budapest.coordinates),
            selection.selected.first(),
        )
        assertEquals(listOf(budapest), recents.remembered)
    }

    /** Only what a forecast needs survives the selection: somewhere to ask, something to call it. */
    @Test
    fun `the id, region and country are left behind`() = runTest {
        val selection = InMemorySelection()

        SelectPlace(selection, RecordingRecents())(londonOhio).getOrThrow()

        assertEquals(
            SelectedPlace(PlaceLabel.Named("London"), londonOhio.coordinates),
            selection.selected.first(),
        )
    }

    /** How long the list is, is this use case's rule; keeping it in order is the store's. */
    @Test
    fun `the store is told how many to keep`() = runTest {
        val recents = RecordingRecents()

        SelectPlace(InMemorySelection(), recents)(budapest).getOrThrow()

        assertEquals(listOf(SelectPlace.RECENTS_LIMIT), recents.limits)
    }

    @Test
    fun `a failing store leaves a failed result`() = runTest {
        val boom = IllegalStateException("disk full")
        val recents = object : RecentPlacesRepository {
            override suspend fun recentPlaces(): List<Place> = emptyList()
            override suspend fun remember(place: Place, limit: Int) = throw boom
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

    private class RecordingRecents : RecentPlacesRepository {
        val remembered = mutableListOf<Place>()
        val limits = mutableListOf<Int>()

        override suspend fun recentPlaces(): List<Place> = remembered

        override suspend fun remember(place: Place, limit: Int) {
            remembered += place
            limits += limit
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

        val budapest =
            place("3054643", "Budapest", "Budapest", latitude = 47.49835, longitude = 19.04045)
        val londonOhio = place("4517009", "London", "Ohio", "United States", 39.8865, -83.4483)
    }
}
