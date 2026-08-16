package com.svyd.upcomingweather.feature.forecast.screen

import com.svyd.upcomingweather.core.designsystem.foundation.ScreenTravel

/**
 * The bar as one thing across the dashboard and a day's page, so it holds its place while the pages
 * slide beneath it.
 *
 * It is the same bar in the same spot on both, so nothing about it actually moves — what the naming
 * buys is that only one of the two is drawn. Both screens are composed for the length of a slide,
 * and [com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar] has no fill, so two bars
 * left to their own devices would show one title through the other.
 *
 * The pace matches the slide it is sitting out rather than the glyph's, because that is how long
 * there are two bars to choose between.
 */
internal val AppBarTravel = ScreenTravel(key = "app-bar", durationMillis = PageSlideMillis)

/** The navigation host's own figure for a page's travel, which this has to agree with. */
private const val PageSlideMillis = 220
