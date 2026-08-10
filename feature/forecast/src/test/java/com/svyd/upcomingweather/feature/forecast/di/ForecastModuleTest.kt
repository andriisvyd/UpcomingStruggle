package com.svyd.upcomingweather.feature.forecast.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.svyd.upcomingweather.core.data.di.dataModule
import com.svyd.upcomingweather.feature.forecast.DayDetailsViewModel
import com.svyd.upcomingweather.feature.forecast.ForecastViewModel
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastStrings
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastUiMapper
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
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.io.File
import java.time.LocalDate

/**
 * Builds everything [forecastModule] exists to provide.
 *
 * Koin binds at runtime, so a missing or miswired dependency is a crash on first use rather than a
 * compile error. Resolving each type here moves that crash into the build. [DayDetailsViewModel] is
 * the one worth the trouble: its date arrives as a runtime parameter, which nothing checks
 * otherwise.
 *
 * [dataModule] is loaded alongside, because half of what these definitions need comes from it and a
 * stand-in would only prove the stand-in was wired correctly.
 */
class ForecastModuleTest {

    @get:Rule
    val folder = TemporaryFolder()

    /**
     * Both ViewModels build their state in the constructor, which needs a main dispatcher to launch
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
    fun `the wording and the mapper can be built`() = runTest {
        val koin = koin()

        koin.get<ForecastStrings>()
        koin.get<ForecastUiMapper>()
    }

    @Test
    fun `both view models can be built`() = runTest {
        val koin = koin()

        koin.get<ForecastViewModel>()
        koin.get<DayDetailsViewModel> { parametersOf(LocalDate.of(2026, 8, 10)) }
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
            modules(dataModule, forecastModule, onDevice)
        }.koin
    }

    private companion object {
        const val STORE_FILE = "test.preferences_pb"
    }
}
