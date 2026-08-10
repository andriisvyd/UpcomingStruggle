package com.svyd.upcomingweather

import android.app.Application
import com.svyd.upcomingweather.core.data.di.dataModule
import com.svyd.upcomingweather.feature.forecast.di.forecastModule
import com.svyd.upcomingweather.feature.search.di.searchModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class UpcomingWeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@UpcomingWeatherApplication)
            modules(dataModule, forecastModule, searchModule)
        }
    }
}
