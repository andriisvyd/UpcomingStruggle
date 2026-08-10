package com.svyd.upcomingweather.feature.search.mapper

import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.feature.search.model.CityUi

/**
 * Places as rows.
 *
 * The subtitle is what tells five towns called London apart, so it carries the region where there is
 * one and falls back to the country alone where there is not.
 */
internal fun Place.toCityUi(): CityUi = CityUi(
    id = id,
    name = name,
    subtitle = listOfNotNull(region, country).joinToString(separator = ", "),
)
