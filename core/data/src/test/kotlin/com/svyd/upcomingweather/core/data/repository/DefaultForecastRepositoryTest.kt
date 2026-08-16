package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.cloud.ForecastApi
import com.svyd.upcomingweather.core.data.cloud.dto.ForecastResponse
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.data.localsource.dto.StoredLabel
import com.svyd.upcomingweather.core.data.mapper.Fixtures
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Reading is a stream of what is stored, and fetching is a write to it.
 *
 * Which is why nothing here waits for the flow to finish: it does not. A reader is subscribed for
 * as long as the screen is, and the fetched forecast arrives as another thing storage says.
 */
class DefaultForecastRepositoryTest {

    private val response: ForecastResponse =
        Fixtures.json.decodeFromString(Fixtures.read("forecast-budapest.json"))

    private val clock = MovableClock(START)

    @Test
    fun `with nothing kept, the fetched forecast is what is reported`() = runTest {
        val reads = read(repository(ApiReturning(response)), budapest)

        assertEquals(listOf(ForecastRead.Cached::class), reads.map { it::class })
    }

    @Test
    fun `what was fetched is kept for next time`() = runTest {
        val kept = InMemoryForecasts()

        read(repository(ApiReturning(response), kept), budapest)

        assertNotNull(kept.forecast(budapest.coordinates).first())
    }

    /** Inside the age asked for, what is stored is the whole answer. */
    @Test
    fun `a young enough forecast is reported alone and nothing is fetched`() = runTest {
        val kept = InMemoryForecasts()
        val api = ApiReturning(response)
        read(repository(api, kept), budapest)
        clock.advance(Duration.ofSeconds(30))

        val reads = read(repository(api, kept), budapest)

        assertEquals(listOf(ForecastRead.Cached::class), reads.map { it::class })
        assertEquals("nothing should have been fetched twice", 1, api.calls)
    }

    @Test
    fun `once past that age it is reported as stale and then replaced`() = runTest {
        val kept = InMemoryForecasts()
        val api = ApiReturning(response)
        read(repository(api, kept), budapest)
        clock.advance(Duration.ofMinutes(2))

        val reads = read(repository(api, kept), budapest)

        assertTrue(reads.first() is ForecastRead.Stale)
        assertTrue(reads.last() is ForecastRead.Cached)
        assertEquals(2, api.calls)
    }

    /** Pulling to refresh asks again however young the stored answer is. */
    @Test
    fun `forcing ignores the age`() = runTest {
        val kept = InMemoryForecasts()
        val api = ApiReturning(response)
        read(repository(api, kept), budapest)
        clock.advance(Duration.ofSeconds(5))

        val reads = read(repository(api, kept), budapest, force = true)

        assertTrue(reads.first() is ForecastRead.Stale)
        assertTrue(reads.last() is ForecastRead.Cached)
        assertEquals(2, api.calls)
    }

    /** The point of keeping anything: something to draw when the network is down. */
    @Test
    fun `what was kept is still reported when the fetch then fails`() = runTest {
        val kept = InMemoryForecasts()
        read(repository(ApiReturning(response), kept), budapest)
        clock.advance(Duration.ofMinutes(2))

        val reads = read(repository(ApiFailing(IOException("connection refused")), kept), budapest)

        assertTrue(reads.first() is ForecastRead.Stale)
        assertTrue("the failure is reported, not thrown", reads.last() is ForecastRead.Failed)
        assertTrue(
            "and it is translated",
            (reads.last() as ForecastRead.Failed).cause is WeatherFailure.NoConnection,
        )
    }

    /**
     * A failure ends the fetch, not the reading.
     *
     * Throwing would close the stream and leave the screen deaf to everything stored afterwards,
     * for as long as it stayed open.
     */
    @Test
    fun `a forecast saved after a failure still reaches the reader`() = runTest {
        val kept = InMemoryForecasts()
        val reads = mutableListOf<ForecastRead>()
        val reading = collect(repository(ApiFailing(IOException("down")), kept), budapest, reads)

        kept.save(budapest.coordinates, stored())

        reading.cancel()
        assertTrue(reads.any { it is ForecastRead.Failed })
        assertTrue("nothing arrived after the failure: $reads", reads.last() is ForecastRead.Cached)
    }

    /** Lisbon's forecast must never be drawn under Budapest's name. */
    @Test
    fun `what was kept for one place is not offered for another`() = runTest {
        val kept = InMemoryForecasts()
        read(repository(ApiReturning(response), kept), budapest)

        val reads = read(repository(ApiReturning(response), kept), lisbon)

        assertEquals(listOf(ForecastRead.Cached::class), reads.map { it::class })
    }

    @Test
    fun `nothing is reported for a place with nothing kept and nothing fetched`() = runTest {
        val kept = InMemoryForecasts()
        val reads = mutableListOf<ForecastRead>()

        collect(repository(ApiFailing(IOException("down")), kept), budapest, reads).cancel()

        assertNull(reads.firstOrNull { it is ForecastRead.Cached })
    }

    @Test
    fun `what was kept keeps the label it was saved with`() = runTest {
        val kept = InMemoryForecasts()
        read(repository(ApiReturning(response), kept), budapest)
        clock.advance(Duration.ofMinutes(2))

        val first = read(repository(ApiReturning(response), kept), budapest).first()

        assertEquals(PlaceLabel.Named("Budapest"), (first as ForecastRead.Stale).forecast.label)
    }

    @Test
    fun `a fetched forecast is stamped with the moment it arrived`() = runTest {
        val reads = read(repository(ApiReturning(response)), budapest)

        assertEquals(START, (reads.single() as ForecastRead.Cached).forecast.retrievedAt)
    }

    @Test
    fun `a stored forecast keeps the moment it was fetched, not the moment it was read`() = runTest {
        val kept = InMemoryForecasts()
        read(repository(ApiReturning(response), kept), budapest)
        clock.advance(Duration.ofMinutes(5))

        val first = read(repository(ApiReturning(response), kept), budapest).first()

        assertEquals(START, (first as ForecastRead.Stale).forecast.retrievedAt)
    }

    /** Everything the stream had to say by the time the work it started had settled. */
    private fun TestScope.read(
        repository: ForecastRepository,
        place: SelectedPlace,
        force: Boolean = false,
    ): List<ForecastRead> {
        val reads = mutableListOf<ForecastRead>()
        collect(repository, place, reads, force).cancel()
        return reads
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.collect(
        repository: ForecastRepository,
        place: SelectedPlace,
        into: MutableList<ForecastRead>,
        force: Boolean = false,
    ) = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        repository.forecast(place, MAX_AGE, force).toList(into)
    }

    /**
     * The scope the kept readings live in is the test's own, so sharing settles when the test says
     * so rather than on a dispatcher nothing here controls.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.repository(
        api: ForecastApi,
        kept: ForecastLocalSource = InMemoryForecasts(),
    ) = DefaultForecastRepository(
        api = api,
        forecasts = kept,
        clock = clock,
        scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
    )

    private fun stored() = StoredForecast(
        label = StoredLabel(name = "Budapest"),
        response = response,
        savedAt = clock.instant().toEpochMilli(),
    )

    private class InMemoryForecasts : ForecastLocalSource {
        private val kept = MutableStateFlow<Map<Coordinates, StoredForecast>>(emptyMap())

        override fun forecast(at: Coordinates): Flow<StoredForecast?> = kept.map { it[at] }

        override suspend fun save(at: Coordinates, forecast: StoredForecast) {
            kept.value += (at to forecast)
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
