package com.svyd.upcomingweather.feature.forecast

import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import com.svyd.upcomingweather.feature.forecast.mapper.forecast
import com.svyd.upcomingweather.feature.forecast.mapper.week
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

/**
 * A place and a forecast for it, and the two repositories a view model reaches through.
 *
 * Shared by the view model tests, which both ask the same question of their own layer: what the
 * screen shows when the stream behind it stops and starts again.
 */
internal val testZone: ZoneId = ZoneId.of("Europe/Budapest")

internal val testDate: LocalDate = LocalDate.of(2026, 8, 10)

internal val budapest = SelectedPlace(
    label = PlaceLabel.Named("Budapest"),
    coordinates = Coordinates(47.5, 19.04),
)

internal val storedForecast: Forecast = forecast(testZone, week(testZone, testDate))

internal class FakeSelection(initial: SelectedPlace? = budapest) : SelectedPlaceRepository {
    override val selected = MutableStateFlow(initial)

    override suspend fun select(place: SelectedPlace) {
        selected.value = place
    }
}

/**
 * Answers from the store every time, as the repository does inside the age.
 *
 * [failing] is the shape of opening a day past that age with no connection: storage answers first
 * and the fetch behind it comes back with nothing.
 */
internal class FakeForecasts(
    private val stored: Forecast = storedForecast,
    private val failing: Boolean = false,
) : ForecastRepository {
    override fun forecast(
        at: SelectedPlace,
        maxAge: Duration,
        force: Boolean,
    ): Flow<ForecastRead> = flow {
        if (failing) {
            emit(ForecastRead.Stale(stored))
            throw IOException("connection refused")
        }
        emit(ForecastRead.Cached(stored))
    }
}

internal class FakePlaces : PlaceRepository {
    override suspend fun search(query: String): List<Place> = emptyList()

    override suspend fun currentPlace(): SelectedPlace = budapest
}
