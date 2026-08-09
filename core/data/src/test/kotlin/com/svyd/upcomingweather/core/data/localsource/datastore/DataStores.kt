package com.svyd.upcomingweather.core.data.localsource.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.TestScope
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * A store on disk that lasts as long as the test does.
 *
 * The scope is the test's own, so the writer coroutine ends with it. DataStore's default scope never
 * stops, and a suite that made one per test would leave every one of them running.
 */
internal fun TemporaryFolder.preferences(scope: TestScope): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
        File(newFolder(), "test.preferences_pb")
    }
