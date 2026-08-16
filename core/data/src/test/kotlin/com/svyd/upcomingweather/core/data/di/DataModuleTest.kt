package com.svyd.upcomingweather.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import com.svyd.upcomingweather.core.domain.usecase.ObserveRecentPlaces
import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.core.domain.usecase.SearchPlaces
import com.svyd.upcomingweather.core.domain.usecase.SelectCurrentPlace
import com.svyd.upcomingweather.core.domain.usecase.SelectPlace
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.io.File

/**
 * Builds everything [dataModule] exists to provide.
 *
 * Koin binds at runtime, so a missing or miswired dependency is a crash on first use rather than a
 * compile error. Resolving each type here moves that crash into the build.
 *
 * The two lists below are also the module's public surface: four repositories, five use cases, and
 * nothing else leaves it.
 */
class DataModuleTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `every repository the module provides can be built`() = runTest {
        val koin = koin()

        koin.get<ForecastRepository>()
        koin.get<PlaceRepository>()
        koin.get<SelectedPlaceRepository>()
        koin.get<RecentPlacesRepository>()
    }

    @Test
    fun `every use case the module provides can be built`() = runTest {
        val koin = koin()

        koin.get<ObserveForecast>()
        koin.get<SearchPlaces>()
        koin.get<SelectPlace>()
        koin.get<SelectCurrentPlace>()
        koin.get<ObserveRecentPlaces>()
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
            modules(dataModule, onDevice)
        }.koin
    }

    private companion object {
        const val STORE_FILE = "test.preferences_pb"
    }
}
