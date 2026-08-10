package com.svyd.upcomingweather.feature.forecast

import com.svyd.upcomingweather.core.domain.usecase.ObserveDay
import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.feature.forecast.mapper.FakeForecastStrings
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastUiMapper
import com.svyd.upcomingweather.feature.forecast.mapper.RETRIEVED_AT
import com.svyd.upcomingweather.feature.forecast.model.DayDetailsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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

/**
 * The day already drawn survives the stream behind it stopping.
 *
 * A fetch announces itself whenever the stream restarts — backgrounding the app is enough — and
 * the page must not empty itself over a day it is already showing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DayDetailsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a fetch behind a day already drawn does not empty the page`() = runTest {
        val viewModel = viewModel()

        val first = mutableListOf<DayDetailsUiState>()
        val watching = backgroundScope.launch { viewModel.state.collect(first::add) }
        advanceTimeBy(1_000)
        assertTrue("expected the day before leaving", first.last() is DayDetailsUiState.Content)

        watching.cancel()
        advanceTimeBy(10_000)

        val second = mutableListOf<DayDetailsUiState>()
        val again = backgroundScope.launch { viewModel.state.collect(second::add) }
        advanceTimeBy(1_000)
        again.cancel()

        assertTrue("nothing was drawn on return", second.isNotEmpty())
        assertTrue(
            "the page emptied over a day already in hand: $second",
            second.none { it is DayDetailsUiState.Loading },
        )
    }

    private fun viewModel() = DayDetailsViewModel(
        observeDay = ObserveDay(ObserveForecast(FakeSelection(), FakeForecasts())),
        date = testDate,
        mapper = ForecastUiMapper(
            strings = FakeForecastStrings(),
            maxAge = Duration.ofMinutes(1),
            deviceZone = { testZone },
        ),
        clock = Clock.fixed(RETRIEVED_AT, testZone),
    )
}
