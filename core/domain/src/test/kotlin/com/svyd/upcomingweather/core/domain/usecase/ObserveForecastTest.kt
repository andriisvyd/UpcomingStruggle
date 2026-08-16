package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.ForecastUpdate
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.model.forecast
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

class ObserveForecastTest {

    @Test
    fun `with nothing chosen there is nothing to fetch`() = runTest {
        val world = World(this)

        val updates = world.observe()

        assertEquals(listOf(ForecastUpdate.NoPlace), updates)
        assertTrue("no fetch should have been attempted", world.forecasts.asked.isEmpty())
    }

    @Test
    fun `with nothing stored, the fetched forecast settles it`() = runTest {
        val world = World(this)
        val updates = world.observe()

        world.selection.value = budapest

        assertEquals(
            listOf(
                ForecastUpdate.NoPlace,
                ForecastUpdate.Fetching,
                ForecastUpdate.Ready(world.forecasts.fresh),
            ),
            updates,
        )
        assertEquals(listOf(budapest), world.forecasts.asked)
    }

    /** The point of storing anything: something to draw before the provider answers. */
    @Test
    fun `a stored forecast stands until the fetched one replaces it`() = runTest {
        val world = World(this)
        world.forecasts.cached = world.forecasts.fresh
        val updates = world.observe()

        world.selection.value = budapest

        assertEquals(
            listOf(
                ForecastUpdate.NoPlace,
                ForecastUpdate.Fetching,
                ForecastUpdate.Stale(world.forecasts.fresh),
                ForecastUpdate.Ready(world.forecasts.fresh),
            ),
            updates,
        )
    }

    /** Something stored, then a failed request: what was stored stays, and the failure follows it. */
    @Test
    fun `a failed fetch after a stored one leaves the stored one standing`() = runTest {
        val boom = IOException("connection refused")
        val world = World(this)
        world.forecasts.cached = world.forecasts.fresh
        world.forecasts.failWith = boom
        val updates = world.observe()

        world.selection.value = budapest

        assertEquals(
            listOf(
                ForecastUpdate.NoPlace,
                ForecastUpdate.Fetching,
                ForecastUpdate.Stale(world.forecasts.fresh),
                ForecastUpdate.Failed(boom),
            ),
            updates,
        )
    }

    @Test
    fun `a failed fetch with nothing stored is reported without ending the stream`() = runTest {
        val boom = IOException("connection refused")
        val world = World(this)
        world.forecasts.failWith = boom
        val updates = world.observe()

        world.selection.value = budapest

        assertEquals(ForecastUpdate.Fetching, updates[1])
        assertEquals(ForecastUpdate.Failed(boom), updates[2])

        world.forecasts.failWith = null
        world.refresh.emit(Unit)

        assertEquals(ForecastUpdate.Ready(world.forecasts.fresh), updates.last())
    }

    @Test
    fun `a refresh fetches again for the same place`() = runTest {
        val world = World(this)
        world.selection.value = budapest
        val updates = world.observe()

        world.refresh.emit(Unit)

        assertEquals(2, world.forecasts.asked.size)
        assertEquals(
            listOf(
                ForecastUpdate.Fetching,
                ForecastUpdate.Ready(world.forecasts.fresh),
                ForecastUpdate.Fetching,
                ForecastUpdate.Ready(world.forecasts.fresh),
            ),
            updates,
        )
    }

    /** Arriving at a place accepts what is stored if it is young enough; pulling never does. */
    @Test
    fun `a new place honours the age, a refresh overrides it`() = runTest {
        val world = World(this)
        world.selection.value = budapest
        world.observe()

        world.refresh.emit(Unit)

        assertEquals(listOf(false, true), world.forecasts.forced)
    }

    @Test
    fun `the age the caller considers good is passed down`() = runTest {
        val world = World(this)
        world.observe()

        world.selection.value = budapest

        assertEquals(listOf(ObserveForecast.MAX_AGE), world.forecasts.ages)
    }

    @Test
    fun `a stored forecast still within its age settles without waiting`() = runTest {
        val world = World(this)
        world.forecasts.withinAge = world.forecasts.fresh
        val updates = world.observe()

        world.selection.value = budapest

        assertEquals(
            listOf(
                ForecastUpdate.NoPlace,
                ForecastUpdate.Fetching,
                ForecastUpdate.Ready(world.forecasts.fresh),
            ),
            updates,
        )
    }

    @Test
    fun `the place is handed over whole, name and all`() = runTest {
        val world = World(this)
        world.observe()

        world.selection.value = budapest

        assertEquals(PlaceLabel.Named("Budapest"), world.forecasts.asked.single().label)
    }

    @Test
    fun `changing place abandons the fetch already in flight`() = runTest {
        val world = World(this)
        val budapestFetch = CompletableDeferred<Unit>()
        world.forecasts.gate = { place -> if (place == budapest) budapestFetch.await() }
        val updates = world.observe()

        world.selection.value = budapest
        world.selection.value = lisbon
        budapestFetch.complete(Unit)

        assertEquals(listOf(budapest, lisbon), world.forecasts.asked)
        assertEquals(
            "only the current place's answer may arrive",
            listOf(
                ForecastUpdate.NoPlace,
                ForecastUpdate.Fetching,
                ForecastUpdate.Fetching,
                ForecastUpdate.Ready(world.forecasts.fresh),
            ),
            updates,
        )
    }

    /** The inputs a test drives, and the collector that records what comes back out. */
    private class World(private val scope: TestScope) {
        val selection = MutableStateFlow<SelectedPlace?>(null)
        val refresh = MutableSharedFlow<Unit>()
        val forecasts = RecordingForecasts()

        private val selectedPlaces = object : SelectedPlaceRepository {
            override val selected: Flow<SelectedPlace?> = selection

            override suspend fun select(place: SelectedPlace) {
                selection.value = place
            }
        }

        /** Starts collecting, and hands back the list that fills as the test drives the inputs. */
        @OptIn(ExperimentalCoroutinesApi::class)
        fun observe(): List<ForecastUpdate> {
            val updates = mutableListOf<ForecastUpdate>()
            scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
                ObserveForecast(selectedPlaces, forecasts)(refresh).toList(updates)
            }
            return updates
        }
    }

    private class RecordingForecasts : ForecastRepository {
        val asked = mutableListOf<SelectedPlace>()
        var failWith: Throwable? = null
        var cached: Forecast? = null
        var withinAge: Forecast? = null
        var gate: (suspend (SelectedPlace) -> Unit)? = null

        val fresh: Forecast =
            forecast(zone = ZoneId.of("Europe/Budapest"), from = LocalDate.of(2026, 8, 9), days = 2)

        val ages = mutableListOf<Duration>()
        val forced = mutableListOf<Boolean>()

        override fun forecast(
            at: SelectedPlace,
            maxAge: Duration,
            force: Boolean,
        ): Flow<ForecastRead> = flow {
            asked += at
            ages += maxAge
            forced += force
            withinAge?.let {
                emit(ForecastRead.Cached(it))
                return@flow
            }
            cached?.let { emit(ForecastRead.Stale(it)) }
            gate?.invoke(at)
            // Reported rather than thrown, as the repository now does: a lost connection ends the
            // fetch, not the reading.
            failWith?.let {
                emit(ForecastRead.Failed(it))
                return@flow
            }
            emit(ForecastRead.Cached(fresh))
        }

        /** Read-only, and never a fetch. What the day details screen sees. */
        override fun stored(at: SelectedPlace): Flow<Forecast?> =
            flowOf(withinAge ?: cached ?: fresh)
    }

    private companion object {
        val budapest = SelectedPlace(PlaceLabel.Named("Budapest"), Coordinates(47.49835, 19.04045))
        val lisbon = SelectedPlace(PlaceLabel.Named("Lisbon"), Coordinates(38.71667, -9.13333))
    }
}
