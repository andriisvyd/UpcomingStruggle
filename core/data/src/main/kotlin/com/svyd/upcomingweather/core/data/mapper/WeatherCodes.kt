package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.domain.model.Condition

/**
 * WMO weather codes, collapsed into the conditions the app draws.
 *
 * The standard separates light from moderate from dense, and freezing from ordinary. None of that
 * changes the glyph or the word on screen, so the distinctions are dropped here rather than carried
 * into the domain and ignored later.
 *
 * An unrecognised code becomes [Condition.Overcast]: the sky is doing something, and claiming it is
 * clear would be the one answer certain to look wrong.
 */
internal fun conditionOf(weatherCode: Int): Condition = when (weatherCode) {
    0 -> Condition.Clear
    1, 2 -> Condition.PartlyCloudy
    3 -> Condition.Overcast
    45, 48 -> Condition.Fog
    51, 53, 55, 56, 57 -> Condition.Drizzle
    61, 63, 65, 66, 67, 80, 81, 82 -> Condition.Rain
    71, 73, 75, 77, 85, 86 -> Condition.Snow
    95, 96, 99 -> Condition.Thunderstorm
    else -> Condition.Overcast
}
