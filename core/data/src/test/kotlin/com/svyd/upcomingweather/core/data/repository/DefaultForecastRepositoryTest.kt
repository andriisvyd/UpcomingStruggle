package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.cloud.ForecastApi
import com.svyd.upcomingweather.core.data.cloud.dto.ForecastResponse
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.data.mapper.Fixtures
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DefaultForecastRepositoryTest {

    private val response: ForecastResponse =
        Fixtures.json.decodeFromString(Fixtures.read("forecast-budapest.json"))

    @Test
    fun `with nothing kept, only the fetched forecast is reported`() = runTest {
        val reads = repository(ApiReturning(response)).forecast(budapest).toList()

        assertEquals(1, reads.size)
        assertTrue(reads.single() is ForecastRead.Fresh)
    }

    @Test
    fun `what was fetched is kept for next time`() = runTest {
        val kept = InMemoryForecasts()

        repository(ApiReturning(response), kept).forecast(budapest).toList()

        assertNotNull(kept.forecast(budapest.coordinates))
    }

    @Test
    fun `what was kept answers first, and the fetched one settles it`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).forecast(budapest).toList()

        val reads = repository(ApiReturning(response), kept).forecast(budapest).toList()

        assertEquals(2, reads.size)
        assertTrue("kept first", reads[0] is ForecastRead.Cached)
        assertTrue("fetched second", reads[1] is ForecastRead.Fresh)
    }

    /** The point of keeping anything: something to draw when the network is down. */
    @Test
    fun `what was kept is still reported when the fetch then fails`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).forecast(budapest).toList()

        val reads = mutableListOf<ForecastRead>()
        var thrown: Throwable? = null
        try {
            repository(ApiFailing(IOException("connection refused")), kept)
                .forecast(budapest)
                .collect { reads += it }
        } catch (failure: Throwable) {
            thrown = failure
        }

        assertTrue("the failure is translated", thrown is WeatherFailure.NoConnection)
        assertEquals(1, reads.size)
        assertTrue(reads.single() is ForecastRead.Cached)
    }

    /** Lisbon's forecast must never be drawn under Budapest's name. */
    @Test
    fun `what was kept for one place is not offered for another`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).forecast(budapest).toList()

        val reads = repository(ApiReturning(response), kept).forecast(lisbon).toList()

        assertEquals(1, reads.size)
        assertTrue(reads.single() is ForecastRead.Fresh)
    }

    @Test
    fun `what was kept keeps the label it was saved with`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).forecast(budapest).toList()

        val first = repository(ApiReturning(response), kept).forecast(budapest).toList().first()

        assertEquals(PlaceLabel.Named("Budapest"), (first as ForecastRead.Cached).forecast.label)
    }

    private fun repository(api: ForecastApi, kept: ForecastLocalSource = InMemoryForecasts()) =
        DefaultForecastRepository(api = api, forecasts = kept)

    private class InMemoryForecasts : ForecastLocalSource {
        private val kept = mutableMapOf<Coordinates, StoredForecast>()

        override suspend fun forecast(at: Coordinates): StoredForecast? = kept[at]

        override suspend fun save(at: Coordinates, forecast: StoredForecast) {
            kept[at] = forecast
        }
    }

    private class ApiReturning(private val response: ForecastResponse) : ForecastApi {
        override suspend fun forecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            timezone: String,
            days: Int,
            windSpeedUnit: String,
        ): ForecastResponse = response
    }

    private class ApiFailing(private val failure: Throwable) : ForecastApi {
        override suspend fun forecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            timezone: String,
            days: Int,
            windSpeedUnit: String,
        ): ForecastResponse = throw failure
    }

    private companion object {
        val budapest = SelectedPlace(PlaceLabel.Named("Budapest"), Coordinates(47.5, 19.04))
        val lisbon = SelectedPlace(PlaceLabel.Named("Lisbon"), Coordinates(38.72, -9.13))
    }
}
