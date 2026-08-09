package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.svyd.upcomingweather.core.data.localsource.SelectionLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

internal class DataStoreSelectionSource(
    private val store: DataStore<Preferences>,
    private val json: Json,
) : SelectionLocalSource {

    override val selection: Flow<StoredSelection?> = store.data.map { preferences ->
        preferences[SELECTION]?.let { json.decodeOrNull<StoredSelection>(it) }
    }

    override suspend fun save(selection: StoredSelection) {
        store.edit { it[SELECTION] = json.encodeToString(selection) }
    }

    private companion object {
        val SELECTION = stringPreferencesKey("selection")
    }
}
