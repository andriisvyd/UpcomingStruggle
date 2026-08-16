package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.svyd.upcomingweather.core.data.cloud.dto.ForecastResponse
import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.data.localsource.dto.StoredLabel
import com.svyd.upcomingweather.core.data.mapper.Fixtures
import com.svyd.upcomingweather.core.domain.model.Coordinates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreForecastSourceTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val response: ForecastResponse =
        Fixtures.json.decodeFromString(Fixtures.read("forecast-budapest.json"))

    @Test
    fun `nothing is kept for a place never fetched`() = runTest {
        assertNull(source().forecast(budapest).first())
    }

    @Test
    fun `what was saved comes back`() = runTest {
        val source = source()

        source.save(budapest, forecast())

        assertEquals(forecast(), source.forecast(budapest).first())
    }

    @Test
    fun `a different place does not find it`() = runTest {
        val source = source()
        source.save(budapest, forecast())

        assertNull(source.forecast(lisbon).first())
    }

    @Test
    fun `several places are kept side by side`() = runTest {
        val source = source()
        source.save(budapest, forecast())
        source.save(lisbon, forecast())

        assertNotNull(source.forecast(budapest).first())
        assertNotNull(source.forecast(lisbon).first())
    }

    /** Saving it again counts as recent, so it is not the one dropped next. */
    @Test
    fun `saving a place again moves it to the back of the queue`() = runTest {
        val source = source(limit = 2)
        source.save(Coordinates(1.0, 1.0), forecast())
        source.save(Coordinates(2.0, 2.0), forecast())
        source.save(Coordinates(1.0, 1.0), forecast())
        source.save(Coordinates(3.0, 3.0), forecast())

        assertNull("the one not touched again is gone", source.forecast(Coordinates(2.0, 2.0)).first())
        assertNotNull("the one saved again survived", source.forecast(Coordinates(1.0, 1.0)).first())
        assertNotNull(source.forecast(Coordinates(3.0, 3.0)).first())
    }

    /** The whole object is rewritten on every save, so it cannot be allowed to grow forever. */
    @Test
    fun `the least recently saved falls off the end`() = runTest {
        val source = source(limit = 2)
        source.save(Coordinates(1.0, 1.0), forecast())
        source.save(Coordinates(2.0, 2.0), forecast())
        source.save(Coordinates(3.0, 3.0), forecast())

        assertNull("the oldest is gone", source.forecast(Coordinates(1.0, 1.0)).first())
        assertNotNull(source.forecast(Coordinates(2.0, 2.0)).first())
        assertNotNull(source.forecast(Coordinates(3.0, 3.0)).first())
    }

    @Test
    fun `a value that no longer decodes reads as absent`() = runTest {
        val store = folder.preferences(this)
        store.edit { it[stringPreferencesKey("forecasts")] = "not json at all" }

        assertNull(DataStoreForecastSource(store, Fixtures.json).forecast(budapest).first())
    }

    private fun TestScope.source(limit: Int? = null) = limit
        ?.let { DataStoreForecastSource(folder.preferences(this), Fixtures.json, limit = it) }
        ?: DataStoreForecastSource(folder.preferences(this), Fixtures.json)

    private fun forecast() =
        StoredForecast(label = StoredLabel("Budapest"), response = response, savedAt = 0L)

    private companion object {
        val budapest = Coordinates(47.5, 19.04)
        val lisbon = Coordinates(38.72, -9.13)
    }
}
