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
import com.svyd.upcomingweather.core.data.location.permission.CoarseLocationPermission
import com.svyd.upcomingweather.core.data.location.permission.LocationPermission
import com.svyd.upcomingweather.core.data.location.position.FusedPositionProvider
import com.svyd.upcomingweather.core.data.location.position.PositionProvider
import com.svyd.upcomingweather.core.data.location.geocoder.ReverseGeocoder
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.LocationPromptLocalSource
import com.svyd.upcomingweather.core.data.localsource.RecentsLocalSource
import com.svyd.upcomingweather.core.data.localsource.SelectionLocalSource
import com.svyd.upcomingweather.core.data.localsource.datastore.DataStoreForecastSource
import com.svyd.upcomingweather.core.data.localsource.datastore.DataStoreLocationPromptSource
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
import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.core.domain.usecase.SearchPlaces
import com.svyd.upcomingweather.core.domain.usecase.SelectCurrentPlace
import com.svyd.upcomingweather.core.domain.usecase.SelectPlace
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
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
    single<LocationPromptLocalSource> { DataStoreLocationPromptSource(store = get()) }

    single<ReverseGeocoder> { AndroidReverseGeocoder(context = androidContext()) }
    single<LocationPermission> { CoarseLocationPermission(context = androidContext()) }
    single<PositionProvider> { FusedPositionProvider(locations = get()) }
    single<DeviceLocationSource> {
        DefaultDeviceLocationSource(
            permission = get(),
            positions = get(),
            geocoder = get(),
            prompts = get(),
        )
    }

    single<ForecastRepository> { DefaultForecastRepository(api = get(), forecasts = get()) }
    single<PlaceRepository> { DefaultPlaceRepository(api = get(), locationSource = get()) }
    single<SelectedPlaceRepository> { DefaultSelectedPlaceRepository(selections = get()) }
    single<RecentPlacesRepository> { DefaultRecentPlacesRepository(recents = get()) }

    factory { ObserveForecast(selection = get(), forecasts = get()) }
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
private const val STORE_NAME = "upcoming-weather"
