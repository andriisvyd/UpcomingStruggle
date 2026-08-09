package com.svyd.upcomingweather.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.android.gms.location.FusedLocationProviderClient
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.check.checkModules
import java.io.File

/**
 * Walks every definition in [dataModule].
 *
 * Koin binds at runtime, so a missing or miswired dependency is a crash on first use rather than a
 * compile error. This is where that trade is paid back: the graph is resolved here, and a hole in it
 * fails the build instead of the app.
 *
 * Two definitions run platform code as they are built — the store's file needs a real context to
 * place it, and the location client needs Play Services — so both are overridden here rather than
 * being kept out of the module the app actually loads.
 */
class DataModuleTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `every definition in the data module can be resolved`() = runTest {
        val writer = backgroundScope
        val overrides = module {
            single<DataStore<Preferences>> {
                PreferenceDataStoreFactory.create(scope = writer) {
                    File(folder.newFolder(), STORE_FILE)
                }
            }
            single<FusedLocationProviderClient> { mockk(relaxed = true) }
        }

        koinApplication {
            androidContext(mockk<Context>(relaxed = true))
            modules(dataModule, overrides)
        }.checkModules()
    }

    private companion object {
        const val STORE_FILE = "test.preferences_pb"
    }
}
