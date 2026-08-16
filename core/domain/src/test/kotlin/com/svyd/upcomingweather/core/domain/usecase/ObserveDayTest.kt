package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.DayUpdate
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.model.forecast
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

class ObserveDayTest {

    @Test
    fun `a day of the forecast is reported once the forecast arrives`() = runTest {
        val world = World(this)
        val updates = world.observe(FIRST_DAY)

        world.selection.value = budapest

        assertEquals(
            listOf(
                DayUpdate.Unavailable,
                DayUpdate.Fetching,
                DayUpdate.Ready(world.forecasts.fresh.days.first(), world.forecasts.fresh.retrievedAt),
            ),
            updates,
        )
    }

    /** A day opened from the list is drawn from what is stored, then replaced. */
    @Test
    fun `a stored day stands until the fetched one replaces it`() = runTest {
        val world = World(this)
        world.forecasts.stored = world.forecasts.fresh
        val updates = world.observe(FIRST_DAY)

        world.selection.value = budapest

        assertEquals(
            listOf(
                DayUpdate.Unavailable,
                DayUpdate.Fetching,
                DayUpdate.Stale(world.forecasts.fresh.days.first(), world.forecasts.fresh.retrievedAt),
                DayUpdate.Ready(world.forecasts.fresh.days.first(), world.forecasts.fresh.retrievedAt),
            ),
            updates,
        )
    }

    @Test
    fun `a date the forecast does not reach has nothing to show`() = runTest {
        val world = World(this)
        val updates = world.observe(LocalDate.of(2026, 12, 25))

        world.selection.value = budapest

        assertEquals(DayUpdate.Unavailable, updates.last())
    }

    @Test
    fun `with no place chosen there is no day`() = runTest {
        val world = World(this)

        val updates = world.observe(FIRST_DAY)

        assertEquals(listOf(DayUpdate.Unavailable), updates)
    }

    @Test
    fun `a failed fetch with nothing stored is reported as a failure`() = runTest {
        val world = World(this)
        world.forecasts.failWith = IOException("connection refused")
        val updates = world.observe(FIRST_DAY)

        world.selection.value = budapest

        assertEquals(DayUpdate.Failed, updates.last())
    }

    /**
     * The failure a stored day is read out ahead of says nothing about that day.
     *
     * Storage answers first and the fetch goes out behind it, so this is the ordinary shape of
     * opening a day past its age with no connection — and reporting the day absent would take one
     * that is in hand off the screen.
     */
    @Test
    fun `a stored day is not made unavailable by the fetch behind it failing`() = runTest {
        val world = World(this)
        world.forecasts.stored = world.forecasts.fresh
        world.forecasts.failWith = IOException("connection refused")
        val updates = world.observe(FIRST_DAY)

        world.selection.value = budapest

        assertEquals(
            listOf(
                DayUpdate.Unavailable,
                DayUpdate.Fetching,
                DayUpdate.Stale(world.forecasts.fresh.days.first(), world.forecasts.fresh.retrievedAt),
                DayUpdate.Failed,
            ),
            updates,
        )
    }

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

        @OptIn(ExperimentalCoroutinesApi::class)
        fun observe(date: LocalDate): List<DayUpdate> {
            val updates = mutableListOf<DayUpdate>()
            val observeDay = ObserveDay(ObserveForecast(selectedPlaces, forecasts))
            scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
                observeDay(date, refresh).toList(updates)
            }
            return updates
        }
    }

    private class RecordingForecasts : ForecastRepository {
        var stored: Forecast? = null
        var failWith: Throwable? = null

        val fresh: Forecast =
            forecast(zone = ZoneId.of("Europe/Budapest"), from = FIRST_DAY, days = 2)

        override fun forecast(
            at: SelectedPlace,
            maxAge: Duration,
            force: Boolean,
        ): Flow<ForecastRead> = flow {
            stored?.let { emit(ForecastRead.Stale(it)) }
            failWith?.let { throw it }
            emit(ForecastRead.Fresh(fresh))
        }
    }

    private companion object {
        val FIRST_DAY: LocalDate = LocalDate.of(2026, 8, 9)

        val budapest = SelectedPlace(PlaceLabel.Named("Budapest"), Coordinates(47.5, 19.04))
    }
}
