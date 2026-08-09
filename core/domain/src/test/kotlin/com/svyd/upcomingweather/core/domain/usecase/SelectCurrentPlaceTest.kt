package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectCurrentPlaceTest {

    @Test
    fun `where the device is becomes the place being reported on`() = runTest {
        val selection = InMemorySelection()

        SelectCurrentPlace(locating(here), selection)().getOrThrow()

        assertEquals(here, selection.selected.first())
    }

    /** A position nothing could name still gets a forecast; only its title is vaguer. */
    @Test
    fun `an unnamed position is selected all the same`() = runTest {
        val unnamed = SelectedPlace(PlaceLabel.NamelessCurrentLocation, Coordinates(47.49835, 19.04045))
        val selection = InMemorySelection()

        SelectCurrentPlace(locating(unnamed), selection)().getOrThrow()

        assertEquals(unnamed, selection.selected.first())
    }

    @Test
    fun `a refused location selects nothing`() = runTest {
        val refused = SecurityException("location denied")
        val selection = InMemorySelection()
        val places = object : PlaceRepository {
            override suspend fun search(query: String): List<Place> = error("not used")
            override suspend fun currentPlace(): SelectedPlace = throw refused
        }

        val result = SelectCurrentPlace(places, selection)()

        assertTrue(result.isFailure)
        assertEquals(refused, result.exceptionOrNull())
        assertNull(selection.selected.first())
    }

    private fun locating(place: SelectedPlace) = object : PlaceRepository {
        override suspend fun search(query: String): List<Place> = error("not used")
        override suspend fun currentPlace(): SelectedPlace = place
    }

    private class InMemorySelection : SelectedPlaceRepository {
        private val state = MutableStateFlow<SelectedPlace?>(null)
        override val selected: Flow<SelectedPlace?> = state

        override suspend fun select(place: SelectedPlace) {
            state.value = place
        }
    }

    private companion object {
        val here = SelectedPlace(PlaceLabel.Named("Budapest"), Coordinates(47.49835, 19.04045))
    }
}
