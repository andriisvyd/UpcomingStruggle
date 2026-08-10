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
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class DefaultForecastRepositoryTest {

    private val response: ForecastResponse =
        Fixtures.json.decodeFromString(Fixtures.read("forecast-budapest.json"))

    private val clock = MovableClock(START)

    @Test
    fun `with nothing kept, only the fetched forecast is reported`() = runTest {
        val reads = repository(ApiReturning(response)).read(budapest)

        assertEquals(1, reads.size)
        assertTrue(reads.single() is ForecastRead.Fresh)
    }

    @Test
    fun `what was fetched is kept for next time`() = runTest {
        val kept = InMemoryForecasts()

        repository(ApiReturning(response), kept).read(budapest)

        assertNotNull(kept.forecast(budapest.coordinates))
    }

    /** Inside the age asked for, what is stored is the whole answer. */
    @Test
    fun `a young enough forecast is reported alone and nothing is fetched`() = runTest {
        val kept = InMemoryForecasts()
        val api = ApiReturning(response)
        repository(api, kept).read(budapest)
        clock.advance(Duration.ofSeconds(30))

        val reads = repository(api, kept).read(budapest)

        assertTrue(reads.single() is ForecastRead.Cached)
        assertEquals("nothing should have been fetched twice", 1, api.calls)
    }

    @Test
    fun `once past that age it is reported as stale and then replaced`() = runTest {
        val kept = InMemoryForecasts()
        val api = ApiReturning(response)
        repository(api, kept).read(budapest)
        clock.advance(Duration.ofMinutes(2))

        val reads = repository(api, kept).read(budapest)

        assertTrue(reads.first() is ForecastRead.Stale)
        assertTrue(reads.last() is ForecastRead.Fresh)
        assertEquals(2, api.calls)
    }

    /** Pulling to refresh asks again however young the stored answer is. */
    @Test
    fun `forcing ignores the age`() = runTest {
        val kept = InMemoryForecasts()
        val api = ApiReturning(response)
        repository(api, kept).read(budapest)
        clock.advance(Duration.ofSeconds(5))

        val reads = repository(api, kept).read(budapest, force = true)

        assertTrue(reads.first() is ForecastRead.Stale)
        assertTrue(reads.last() is ForecastRead.Fresh)
        assertEquals(2, api.calls)
    }

    /** The point of keeping anything: something to draw when the network is down. */
    @Test
    fun `what was kept is still reported when the fetch then fails`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).read(budapest)
        clock.advance(Duration.ofMinutes(2))

        val reads = mutableListOf<ForecastRead>()
        var thrown: Throwable? = null
        try {
            repository(ApiFailing(IOException("connection refused")), kept)
                .forecast(budapest, MAX_AGE, force = false)
                .collect { reads += it }
        } catch (failure: Throwable) {
            thrown = failure
        }

        assertTrue("the failure is translated", thrown is WeatherFailure.NoConnection)
        assertEquals(1, reads.size)
        assertTrue(reads.single() is ForecastRead.Stale)
    }

    /** Lisbon's forecast must never be drawn under Budapest's name. */
    @Test
    fun `what was kept for one place is not offered for another`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).read(budapest)

        val reads = repository(ApiReturning(response), kept).read(lisbon)

        assertEquals(1, reads.size)
        assertTrue(reads.single() is ForecastRead.Fresh)
    }

    @Test
    fun `what was kept keeps the label it was saved with`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).read(budapest)
        clock.advance(Duration.ofMinutes(2))

        val first = repository(ApiReturning(response), kept).read(budapest).first()

        assertEquals(PlaceLabel.Named("Budapest"), (first as ForecastRead.Stale).forecast.label)
    }

    @Test
    fun `a fetched forecast is stamped with the moment it arrived`() = runTest {
        val reads = repository(ApiReturning(response)).read(budapest)

        assertEquals(START, (reads.single() as ForecastRead.Fresh).forecast.retrievedAt)
    }

    @Test
    fun `a stored forecast keeps the moment it was fetched, not the moment it was read`() = runTest {
        val kept = InMemoryForecasts()
        repository(ApiReturning(response), kept).read(budapest)
        clock.advance(Duration.ofMinutes(5))

        val first = repository(ApiReturning(response), kept).read(budapest).first()

        assertEquals(START, (first as ForecastRead.Stale).forecast.retrievedAt)
    }

    private suspend fun ForecastRepository.read(
        place: SelectedPlace,
        force: Boolean = false,
    ): List<ForecastRead> = forecast(place, MAX_AGE, force).toList()

    private fun repository(api: ForecastApi, kept: ForecastLocalSource = InMemoryForecasts()) =
        DefaultForecastRepository(api = api, forecasts = kept, clock = clock)

    private class InMemoryForecasts : ForecastLocalSource {
        private val kept = mutableMapOf<Coordinates, StoredForecast>()

        override suspend fun forecast(at: Coordinates): StoredForecast? = kept[at]

        override suspend fun save(at: Coordinates, forecast: StoredForecast) {
            kept[at] = forecast
        }
    }

    private class ApiReturning(private val response: ForecastResponse) : ForecastApi {
        var calls = 0
            private set

        override suspend fun forecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            timezone: String,
            days: Int,
            windSpeedUnit: String,
        ): ForecastResponse {
            calls++
            return response
        }
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

    /** A clock the test moves by hand, so ageing takes no time to happen. */
    private class MovableClock(private var now: Instant) : Clock() {
        fun advance(by: Duration) {
            now += by
        }

        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
    }

    private companion object {
        val START: Instant = Instant.parse("2026-08-09T13:20:00Z")
        val MAX_AGE: Duration = Duration.ofMinutes(1)

        val budapest = SelectedPlace(PlaceLabel.Named("Budapest"), Coordinates(47.5, 19.04))
        val lisbon = SelectedPlace(PlaceLabel.Named("Lisbon"), Coordinates(38.72, -9.13))
    }
}
