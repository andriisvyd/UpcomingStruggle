package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.svyd.upcomingweather.core.data.localsource.RecentsLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredPlace
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

internal class DataStoreRecentsSource(
    private val store: DataStore<Preferences>,
    private val json: Json,
) : RecentsLocalSource {

    override suspend fun recentPlaces(): List<StoredPlace> = store.data.first().read()

    /**
     * Read, reorder and write in one edit.
     *
     * Doing this from a caller would mean reading and writing either side of a suspension, where two
     * selections in quick succession can interleave and one of them is lost.
     */
    override suspend fun remember(place: StoredPlace, limit: Int) {
        store.edit { preferences ->
            val kept = preferences.read()
                .filterNot { it.id == place.id }
                .let { listOf(place) + it }
                .take(limit)

            preferences[RECENTS] = json.encodeToString(kept)
        }
    }

    private fun Preferences.read(): List<StoredPlace> =
        this[RECENTS]?.let { json.decodeOrNull<List<StoredPlace>>(it) }.orEmpty()

    private companion object {
        val RECENTS = stringPreferencesKey("recents")
    }
}
