package com.svyd.upcomingweather.feature.search.di

import com.svyd.upcomingweather.feature.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {

    viewModel {
        SearchViewModel(
            searchPlaces = get(),
            selectPlace = get(),
            selectCurrentPlace = get(),
            recentPlaces = get(),
            recordLocationPrompt = get(),
        )
    }
}
