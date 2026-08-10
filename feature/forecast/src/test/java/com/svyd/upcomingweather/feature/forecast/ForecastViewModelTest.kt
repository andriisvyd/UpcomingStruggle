package com.svyd.upcomingweather.feature.forecast

import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.core.domain.usecase.SelectCurrentPlace
import com.svyd.upcomingweather.feature.forecast.mapper.FakeForecastStrings
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastUiMapper
import com.svyd.upcomingweather.feature.forecast.mapper.RETRIEVED_AT
import com.svyd.upcomingweather.feature.forecast.mapper.forecast
import com.svyd.upcomingweather.feature.forecast.mapper.week
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

/**
 * The dashboard's state survives leaving the screen.
 *
 * The stream behind it stops once the last subscriber goes, which is what happens on the way to
 * the day details. What the reader sees on the way back is decided by whether anything remembers
 * the page that was already drawn.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `coming back after the stream stops does not redraw the skeleton`() = runTest {
        val viewModel = viewModel()

        // Arrive, and let the forecast land.
        val first = mutableListOf<ForecastUiState>()
        val watching = backgroundScope.launch { viewModel.state.collect(first::add) }
        advanceTimeBy(1_000)
        assertTrue("expected content before leaving", first.last() is ForecastUiState.Content)

        // Leave for the day details: the last subscriber goes and the stream is cancelled.
        watching.cancel()
        advanceTimeBy(10_000)

        // Come back.
        val second = mutableListOf<ForecastUiState>()
        val again = backgroundScope.launch { viewModel.state.collect(second::add) }
        advanceTimeBy(1_000)
        again.cancel()

        assertTrue("nothing was drawn on return", second.isNotEmpty())
        assertTrue(
            "the skeleton came back over a forecast already in hand: $second",
            second.none { it is ForecastUiState.Loading },
        )
    }

    private fun TestScope.viewModel(): ForecastViewModel {
        val selection = FakeSelection(budapest)
        return ForecastViewModel(
            observeForecast = ObserveForecast(selection, FakeForecasts(sofa)),
            selectCurrentPlace = SelectCurrentPlace(FakePlaces(), selection),
            mapper = ForecastUiMapper(
                strings = FakeForecastStrings(),
                maxAge = Duration.ofMinutes(1),
                deviceZone = { zone },
            ),
            clock = Clock.fixed(RETRIEVED_AT, zone),
        )
    }

    private class FakeSelection(initial: SelectedPlace?) : SelectedPlaceRepository {
        override val selected = MutableStateFlow(initial)

        override suspend fun select(place: SelectedPlace) {
            selected.value = place
        }
    }

    /** Answers from the store every time, as the repository does inside the age. */
    private class FakeForecasts(private val stored: Forecast) : ForecastRepository {
        override fun forecast(
            at: SelectedPlace,
            maxAge: Duration,
            force: Boolean,
        ): Flow<ForecastRead> = flow { emit(ForecastRead.Cached(stored)) }
    }

    private class FakePlaces : PlaceRepository {
        override suspend fun search(query: String): List<Place> = emptyList()

        override suspend fun currentPlace(): SelectedPlace = budapest
    }

    private companion object {
        val zone: ZoneId = ZoneId.of("Europe/Budapest")
        val budapest = SelectedPlace(
            label = PlaceLabel.Named("Budapest"),
            coordinates = com.svyd.upcomingweather.core.domain.model.Coordinates(47.5, 19.04),
        )
        val sofa: Forecast = forecast(zone, week(zone, LocalDate.of(2026, 8, 10)))
    }
}
