package com.svyd.upcomingweather.feature.forecast.di

import com.svyd.upcomingweather.core.domain.usecase.ObserveForecast
import com.svyd.upcomingweather.feature.forecast.DayDetailsViewModel
import com.svyd.upcomingweather.feature.forecast.ForecastViewModel
import com.svyd.upcomingweather.feature.forecast.mapper.AndroidForecastStrings
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastStrings
import com.svyd.upcomingweather.feature.forecast.mapper.ForecastUiMapper
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.time.LocalDate

/**
 * What the forecast screens need beyond the domain: the wording, and the mapper that spends it.
 *
 * The age the mapper compares against is the same one the domain refetches on, so what the screen
 * says about staleness and what the app does about it cannot drift apart.
 */
val forecastModule = module {

    single<ForecastStrings> { AndroidForecastStrings(context = androidContext()) }

    factory {
        ForecastUiMapper(strings = get(), maxAge = ObserveForecast.MAX_AGE)
    }

    viewModel {
        ForecastViewModel(
            observeForecast = get(),
            selectCurrentPlace = get(),
            recordLocationPrompt = get(),
            mapper = get(),
            clock = get(),
        )
    }

    viewModel { (date: LocalDate) ->
        DayDetailsViewModel(observeDay = get(), date = date, mapper = get(), clock = get())
    }
}
