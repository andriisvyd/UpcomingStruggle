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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

/**
 * A day is read from storage and nothing else.
 *
 * Which is why there is no fetching to test here, and no failing: a day is opened from a list built
 * out of the very forecast this looks in. What refreshes that forecast is the list's business, and
 * it reaches this screen by being stored.
 */
class ObserveDayTest {

    @Test
    fun `the stored forecast's day is reported at once`() = runTest {
        val world = World(this)
        world.selection.value = budapest

        val updates = world.observe(FIRST_DAY)

        assertEquals(
            listOf(DayUpdate.Ready(world.stored.days.first(), world.stored.retrievedAt)),
            updates,
        )
    }

    /** The whole point: the page has its day before it has drawn anything. */
    @Test
    fun `nothing is fetched to answer`() = runTest {
        val world = World(this)
        world.selection.value = budapest

        world.observe(FIRST_DAY)

        assertEquals(0, world.forecasts.fetches)
    }

    @Test
    fun `a day the stored forecast does not reach has nothing to show`() = runTest {
        val world = World(this)
        world.selection.value = budapest

        val updates = world.observe(LocalDate.of(2026, 12, 25))

        assertEquals(listOf(DayUpdate.Unavailable), updates)
    }

    @Test
    fun `with no place chosen there is no day`() = runTest {
        val world = World(this)

        val updates = world.observe(FIRST_DAY)

        assertEquals(listOf(DayUpdate.Unavailable), updates)
    }

    @Test
    fun `with nothing stored there is no day`() = runTest {
        val world = World(this)
        world.forecasts.kept.value = null
        world.selection.value = budapest

        val updates = world.observe(FIRST_DAY)

        assertEquals(listOf(DayUpdate.Unavailable), updates)
    }

    /** A refresh the list asked for arrives here too, because both are reading the one copy. */
    @Test
    fun `a forecast stored while the day is open replaces it`() = runTest {
        val world = World(this)
        world.forecasts.kept.value = null
        world.selection.value = budapest
        val updates = world.observe(FIRST_DAY)

        world.forecasts.kept.value = world.stored

        assertEquals(
            listOf(
                DayUpdate.Unavailable,
                DayUpdate.Ready(world.stored.days.first(), world.stored.retrievedAt),
            ),
            updates,
        )
    }

    private class World(private val scope: TestScope) {
        val selection = MutableStateFlow<SelectedPlace?>(null)
        val forecasts = RecordingForecasts()
        val stored: Forecast get() = forecasts.fresh

        private val selectedPlaces = object : SelectedPlaceRepository {
            override val selected: Flow<SelectedPlace?> = selection
            override suspend fun select(place: SelectedPlace) {
                selection.value = place
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        fun observe(date: LocalDate): List<DayUpdate> {
            val updates = mutableListOf<DayUpdate>()
            val observeDay = ObserveDay(selectedPlaces, forecasts)
            scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
                observeDay(date).toList(updates)
            }
            return updates
        }
    }

    private class RecordingForecasts : ForecastRepository {
        var fetches = 0
            private set

        val fresh: Forecast =
            forecast(zone = ZoneId.of("Europe/Budapest"), from = FIRST_DAY, days = 2)

        val kept = MutableStateFlow<Forecast?>(fresh)

        override fun stored(at: SelectedPlace): Flow<Forecast?> = kept

        /** Counted, not answered: reaching this at all is the failure the test is looking for. */
        override fun forecast(
            at: SelectedPlace,
            maxAge: Duration,
            force: Boolean,
        ): Flow<ForecastRead> {
            fetches++
            return emptyFlow()
        }
    }

    private companion object {
        val FIRST_DAY: LocalDate = LocalDate.of(2026, 8, 9)

        val budapest = SelectedPlace(PlaceLabel.Named("Budapest"), Coordinates(47.5, 19.04))
    }
}
