package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.domain.model.Coordinates
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

/**
 * Forecasts held as one JSON object, keyed by the coordinates they were fetched for.
 *
 * [limit] is a property of storing them this way rather than a rule about the app: the whole object
 * is rewritten on every save, so an unbounded one makes each write more expensive than the last. A
 * store with room to spare would be right to keep everything.
 *
 */
internal class DataStoreForecastSource(
    private val store: DataStore<Preferences>,
    private val json: Json,
    private val limit: Int = DEFAULT_LIMIT,
) : ForecastLocalSource {

    override suspend fun forecast(at: Coordinates): StoredForecast? = read()[at.key()]

    /** Removing the key before adding it is what makes saving the same place again count as recent. */
    override suspend fun save(at: Coordinates, forecast: StoredForecast) {
        val kept = ((read() - at.key()) + (at.key() to forecast))
            .toList()
            .takeLast(limit)
            .toMap()

        store.edit { it[FORECASTS] = json.encodeToString(kept) }
    }

    private suspend fun read(): Map<String, StoredForecast> {
        val raw = store.data.first()[FORECASTS] ?: return emptyMap()
        return json.decodeOrNull<Map<String, StoredForecast>>(raw).orEmpty()
    }

    private fun Coordinates.key(): String = "$latitude,$longitude"

    private companion object {
        const val DEFAULT_LIMIT = 15

        val FORECASTS = stringPreferencesKey("forecasts")
    }
}
