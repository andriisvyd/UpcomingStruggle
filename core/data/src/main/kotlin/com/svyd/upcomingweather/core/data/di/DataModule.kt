package com.svyd.upcomingweather.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.android.gms.location.LocationServices
import com.svyd.upcomingweather.core.data.cloud.ForecastApi
import com.svyd.upcomingweather.core.data.cloud.SearchApi
import com.svyd.upcomingweather.core.data.location.geocoder.AndroidReverseGeocoder
import com.svyd.upcomingweather.core.data.location.DeviceLocationSource
import com.svyd.upcomingweather.core.data.location.DefaultDeviceLocationSource
import com.svyd.upcomingweather.core.data.location.permission.AndroidLocationPermission
import com.svyd.upcomingweather.core.data.location.permission.LocationPermission
import com.svyd.upcomingweather.core.data.location.position.FusedPositionProvider
import com.svyd.upcomingweather.core.data.location.position.PositionProvider
import com.svyd.upcomingweather.core.data.location.geocoder.ReverseGeocoder
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.RecentsLocalSource
import com.svyd.upcomingweather.core.data.localsource.SelectionLocalSource
import com.svyd.upcomingweather.core.data.localsource.datastore.DataStoreForecastSource
import com.svyd.upcomingweather.core.data.localsource.datastore.DataStoreRecentsSource
import com.svyd.upcomingweather.core.data.localsource.datastore.DataStoreSelectionSource
import com.svyd.upcomingweather.core.data.repository.DefaultForecastRepository
import com.svyd.upcomingweather.core.data.repository.DefaultPlaceRepository
import com.svyd.upcomingweather.core.data.repository.DefaultRecentPlacesRepository
import com.svyd.upcomingweather.core.data.repository.DefaultSelectedPlaceRepository
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository
import com.svyd.upcomingweather.core.domain.repository.RecentPlacesRepository
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import com.svyd.upcomingweather.core.domain.usecase.GetRecentPlaces
import com.svyd.upcomingweather.core.domain.usecase.ObserveDay
import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.core.domain.usecase.SearchPlaces
import com.svyd.upcomingweather.core.domain.usecase.SelectCurrentPlace
import com.svyd.upcomingweather.core.domain.usecase.SelectPlace
import kotlinx.serialization.json.Json
import java.time.Clock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Forecasts and place search live on different hosts, so there are two Retrofit instances over one
 * client. Everything below the repositories is internal to this module; what leaves it are the
 * domain's repository interfaces and the use cases built on them.
 */
val dataModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    single { OkHttpClient.Builder().build() }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(STORE_NAME)
        }
    }

    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    single(named(FORECAST_HOST)) { retrofit(get(), get(), "https://api.open-meteo.com/") }
    single(named(SEARCH_HOST)) { retrofit(get(), get(), "https://geocoding-api.open-meteo.com/") }

    single { get<Retrofit>(named(FORECAST_HOST)).create(ForecastApi::class.java) }
    single { get<Retrofit>(named(SEARCH_HOST)).create(SearchApi::class.java) }

    single<ForecastLocalSource> { DataStoreForecastSource(store = get(), json = get()) }
    single<SelectionLocalSource> { DataStoreSelectionSource(store = get(), json = get()) }
    single<RecentsLocalSource> { DataStoreRecentsSource(store = get(), json = get()) }

    single<ReverseGeocoder> { AndroidReverseGeocoder(context = androidContext()) }
    single<LocationPermission> { AndroidLocationPermission(context = androidContext()) }
    single<PositionProvider> { FusedPositionProvider(locations = get()) }
    single<DeviceLocationSource> {
        DefaultDeviceLocationSource(
            permission = get(),
            positions = get(),
            geocoder = get(),
        )
    }

    single<Clock> { Clock.systemUTC() }

    /**
     * Where the readings the repositories keep alive are held.
     *
     * Named, so that nothing can be handed the application's own scope by asking for a scope —
     * something screen-shaped injecting this by accident would outlive its screen for good.
     *
     * Never cancelled, which is the point of it: it holds what the next screen to ask will read,
     * and only the process ending should end that. A supervisor job so one reading failing cannot
     * take the others with it.
     */
    single(named(APP_SCOPE)) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)))
    }

    /** A definition rather than a literal, so a test can make the sharing above deterministic. */
    single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)) { Dispatchers.Default }

    single<ForecastRepository> {
        DefaultForecastRepository(
            api = get(),
            forecasts = get(),
            clock = get(),
            scope = get(named(APP_SCOPE)),
        )
    }
    single<PlaceRepository> { DefaultPlaceRepository(api = get(), locationSource = get()) }
    single<SelectedPlaceRepository> {
        DefaultSelectedPlaceRepository(selections = get(), scope = get(named(APP_SCOPE)))
    }
    single<RecentPlacesRepository> { DefaultRecentPlacesRepository(recents = get()) }

    factory { ObserveForecast(selection = get(), forecasts = get()) }
    factory { ObserveDay(selection = get(), forecasts = get()) }
    factory { SearchPlaces(places = get()) }
    factory { SelectPlace(selection = get(), recents = get()) }
    factory { SelectCurrentPlace(places = get(), selection = get()) }
    factory { GetRecentPlaces(recents = get()) }
}

private fun retrofit(client: OkHttpClient, json: Json, baseUrl: String): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

private const val FORECAST_HOST = "forecast"
private const val SEARCH_HOST = "search"

const val APP_SCOPE = "appScope"
const val DEFAULT_DISPATCHER = "defaultDispatcher"
private const val STORE_NAME = "upcoming-weather"
