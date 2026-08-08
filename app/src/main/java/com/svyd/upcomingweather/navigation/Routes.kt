package com.svyd.upcomingweather.navigation

import kotlinx.serialization.Serializable

/**
 * The three destinations, as types rather than strings.
 *
 * Note what is *not* here: a city. Choosing a city is app state, not a navigation result — search
 * writes it and the forecast screen observes it, so nothing has to be handed back up the stack.
 * The details route carries a date and re-reads the forecast already in hand.
 */
@Serializable
data object ForecastRoute

@Serializable
data object SearchRoute

@Serializable
data class DayDetailsRoute(val date: String)
