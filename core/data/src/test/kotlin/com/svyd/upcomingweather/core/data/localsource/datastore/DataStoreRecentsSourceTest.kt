package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.svyd.upcomingweather.core.data.localsource.dto.StoredPlace
import com.svyd.upcomingweather.core.data.mapper.Fixtures
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreRecentsSourceTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `nothing has been looked at to begin with`() = runTest {
        assertTrue(source().recentPlaces.first().isEmpty())
    }

    @Test
    fun `the newest place goes to the front`() = runTest {
        val source = source()
        source.remember(lisbon, LIMIT)
        source.remember(tokyo, LIMIT)

        source.remember(budapest, LIMIT)

        assertEquals(listOf(budapest, tokyo, lisbon), source.recentPlaces.first())
    }

    @Test
    fun `looking at a place again moves it up rather than repeating it`() = runTest {
        val source = source()
        source.remember(lisbon, LIMIT)
        source.remember(budapest, LIMIT)
        source.remember(tokyo, LIMIT)

        source.remember(budapest, LIMIT)

        assertEquals(listOf(budapest, tokyo, lisbon), source.recentPlaces.first())
    }

    /** Two towns of the same name are two places, told apart by id rather than by name. */
    @Test
    fun `places sharing a name are kept apart`() = runTest {
        val source = source()
        source.remember(londonEngland, LIMIT)

        source.remember(londonOhio, LIMIT)

        assertEquals(listOf(londonOhio, londonEngland), source.recentPlaces.first())
    }

    @Test
    fun `the oldest place falls off the end`() = runTest {
        val source = source()
        source.remember(lisbon, limit = 2)
        source.remember(tokyo, limit = 2)

        source.remember(budapest, limit = 2)

        assertEquals(listOf(budapest, tokyo), source.recentPlaces.first())
    }

    @Test
    fun `a value that no longer decodes reads as empty`() = runTest {
        val store = folder.preferences(this)
        store.edit { it[stringPreferencesKey("recents")] = "not json at all" }

        assertTrue(DataStoreRecentsSource(store, Fixtures.json).recentPlaces.first().isEmpty())
    }

    private fun TestScope.source() = DataStoreRecentsSource(folder.preferences(this), Fixtures.json)

    private companion object {
        const val LIMIT = 5

        fun place(id: String, name: String, region: String? = null, country: String = "Hungary") =
            StoredPlace(
                id = id,
                name = name,
                region = region,
                country = country,
                latitude = 0.0,
                longitude = 0.0,
            )

        val budapest = place("3054643", "Budapest", "Budapest")
        val lisbon = place("2267057", "Lisbon", country = "Portugal")
        val tokyo = place("1850147", "Tokyo", country = "Japan")
        val londonEngland = place("2643743", "London", "England", "United Kingdom")
        val londonOhio = place("4517009", "London", "Ohio", "United States")
    }
}
