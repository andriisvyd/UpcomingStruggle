package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.svyd.upcomingweather.core.data.localsource.LocationPromptLocalSource
import kotlinx.coroutines.flow.first

internal class DataStoreLocationPromptSource(
    private val store: DataStore<Preferences>,
) : LocationPromptLocalSource {

    override suspend fun everAsked(): Boolean = store.data.first()[ASKED] == true

    override suspend fun recordAsked() {
        store.edit { it[ASKED] = true }
    }

    private companion object {
        val ASKED = booleanPreferencesKey("location_asked")
    }
}
