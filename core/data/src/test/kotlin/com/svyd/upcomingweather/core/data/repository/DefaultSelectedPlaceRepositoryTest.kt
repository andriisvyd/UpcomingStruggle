package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.SelectionLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredSelection
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultSelectedPlaceRepositoryTest {

    @Test
    fun `nothing is selected until something is`() = runTest {
        assertNull(DefaultSelectedPlaceRepository(FakeSelections()).selected.first())
    }

    @Test
    fun `what was written comes back as a place`() = runTest {
        val selections = FakeSelections()
        val repository = DefaultSelectedPlaceRepository(selections)

        repository.select(budapest)

        assertEquals(budapest, repository.selected.first())
    }

    /**
     * Two device readings from the same area round to the same coordinates, so the same selection gets
     * written twice. [ObserveForecast][com.svyd.upcomingweather.core.domain.usecase.ObserveForecast]
     * refetches on every emission, so standing still must not look like moving.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `writing the same selection twice is reported once`() = runTest {
        val selections = FakeSelections()
        val repository = DefaultSelectedPlaceRepository(selections)
        val seen = mutableListOf<SelectedPlace?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.selected.toList(seen)
        }

        repository.select(budapest)
        repository.select(budapest)

        assertEquals(listOf(null, budapest), seen)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a different selection is reported`() = runTest {
        val selections = FakeSelections()
        val repository = DefaultSelectedPlaceRepository(selections)
        val seen = mutableListOf<SelectedPlace?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.selected.toList(seen)
        }

        repository.select(budapest)
        repository.select(lisbon)

        assertEquals(listOf(null, budapest, lisbon), seen)
    }

    /**
     * Emits on every write, including one that changes nothing — which is what a real store does,
     * and what makes [DefaultSelectedPlaceRepository]'s own conflation worth having.
     */
    private class FakeSelections : SelectionLocalSource {
        private val state = MutableSharedFlow<StoredSelection?>(replay = 1)

        init {
            state.tryEmit(null)
        }

        override val selection: Flow<StoredSelection?> = state

        override suspend fun save(selection: StoredSelection) {
            state.emit(selection)
        }
    }

    private companion object {
        val budapest = SelectedPlace(PlaceLabel.Named("Budapest"), Coordinates(47.5, 19.04))
        val lisbon = SelectedPlace(PlaceLabel.Named("Lisbon"), Coordinates(38.72, -9.13))
    }
}
