package com.svyd.upcomingweather.feature.search.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.svyd.upcomingweather.core.data.di.dataModule
import com.svyd.upcomingweather.feature.search.SearchViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.io.File

/**
 * Builds everything [searchModule] exists to provide.
 *
 * One definition, and five use cases behind it that come from [dataModule] — so the module is
 * loaded alongside and the graph under test is the one the app assembles.
 */
class SearchModuleTest {

    @get:Rule
    val folder = TemporaryFolder()

    /**
     * The view model builds its state in the constructor, which needs a main dispatcher to launch
     * into. Nothing is collected — the state is shared only while subscribed — but the scope has to
     * exist for the constructor to return.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the view model can be built`() = runTest {
        val koin = koin()

        koin.get<SearchViewModel>()
    }

    /**
     * The two definitions that run platform code as they are built — the store's file, whose
     * location comes from a real context, and Google's location client — are replaced here. Every
     * other definition is the one the app uses.
     */
    private fun TestScope.koin(): Koin {
        val writer = backgroundScope
        val onDevice = module {
            single<DataStore<Preferences>> {
                PreferenceDataStoreFactory.create(scope = writer) {
                    File(folder.newFolder(), STORE_FILE)
                }
            }
            single<FusedLocationProviderClient> { mockk(relaxed = true) }
        }

        return koinApplication {
            androidContext(mockk<Context>(relaxed = true))
            modules(dataModule, searchModule, onDevice)
        }.koin
    }

    private companion object {
        const val STORE_FILE = "test.preferences_pb"
    }
}
