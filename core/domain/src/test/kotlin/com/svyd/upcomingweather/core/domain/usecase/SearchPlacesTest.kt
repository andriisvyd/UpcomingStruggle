package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.SearchOutcome
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class SearchPlacesTest {

    @Test
    fun `a single character is not a search`() = runTest {
        val places = RecordingPlaces(results = listOf(budapest))

        val outcome = SearchPlaces(places)("b").getOrThrow()

        assertEquals(SearchOutcome.TooShort, outcome)
        assertNull("the geocoder should not have been asked", places.lastQuery)
    }

    @Test
    fun `padding does not make a query long enough`() = runTest {
        val places = RecordingPlaces(results = listOf(budapest))

        val outcome = SearchPlaces(places)("  b  ").getOrThrow()

        assertEquals(SearchOutcome.TooShort, outcome)
        assertNull(places.lastQuery)
    }

    /**
     * The reason [SearchOutcome] exists: both answers hold no places, and the screen says something
     * different about each.
     */
    @Test
    fun `finding nothing is not the same as not looking`() = runTest {
        val foundNothing = SearchPlaces(RecordingPlaces(results = emptyList()))("qqqq").getOrThrow()
        val didNotLook = SearchPlaces(RecordingPlaces(results = emptyList()))("q").getOrThrow()

        assertEquals(SearchOutcome.NoMatch, foundNothing)
        assertEquals(SearchOutcome.TooShort, didNotLook)
    }

    @Test
    fun `the query is trimmed before it is asked`() = runTest {
        val places = RecordingPlaces(results = listOf(budapest))

        SearchPlaces(places)("  buda  ")

        assertEquals("buda", places.lastQuery)
    }

    @Test
    fun `matches come back as they were found`() = runTest {
        val places = RecordingPlaces(results = listOf(budapest))

        val outcome = SearchPlaces(places)("buda").getOrThrow()

        assertEquals(SearchOutcome.Found(listOf(budapest)), outcome)
    }

    @Test
    fun `a failing geocoder becomes a failed result`() = runTest {
        val boom = IllegalStateException("no wire")

        val result = SearchPlaces(RecordingPlaces(failure = boom))("buda")

        assertTrue(result.isFailure)
        assertEquals(boom, result.exceptionOrNull())
    }

    /**
     * The reason [com.svyd.upcomingweather.core.domain.failure.catching] exists: a canceled search must stay cancelled, not arrive as a
     * failure that the caller then reports to a user who has already moved on.
     */
    @Test
    fun `cancellation is not swallowed into a failed result`() = runTest {
        val started = CompletableDeferred<Unit>()
        val places = object : PlaceRepository {
            override suspend fun search(query: String): List<Place> {
                started.complete(Unit)
                throw CancellationException("typed another letter")
            }

            override suspend fun currentPlace(): SelectedPlace = error("not used")
        }

        var outcome: Result<SearchOutcome>? = null
        val job = launch { outcome = SearchPlaces(places)("buda") }
        started.await()
        job.join()

        assertTrue("the coroutine should have been cancelled", job.isCancelled)
        assertNull("no result should have been produced", outcome)
    }

    private class RecordingPlaces(
        private val results: List<Place> = emptyList(),
        private val failure: Throwable? = null,
    ) : PlaceRepository {
        var lastQuery: String? = null
            private set

        override suspend fun search(query: String): List<Place> {
            lastQuery = query
            failure?.let { throw it }
            return results
        }

        override suspend fun currentPlace(): SelectedPlace = error("not used")
    }

    private companion object {
        val budapest = Place(
            id = "3054643",
            name = "Budapest",
            region = "Budapest",
            country = "Hungary",
            coordinates = Coordinates(latitude = 47.49835, longitude = 19.04045),
        )
    }
}
