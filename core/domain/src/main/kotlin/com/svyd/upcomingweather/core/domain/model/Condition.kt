package com.svyd.upcomingweather.core.domain.model

/**
 * What the sky is doing, reduced to the cases a forecast can act on.
 *
 * Providers describe weather in far more detail than this — dozens of codes separating light from
 * moderate from freezing rain. Anything finer than these eight makes no difference to what gets
 * drawn or said, so the data layer collapses its vocabulary into this one.
 */
enum class Condition {
    Clear,
    PartlyCloudy,
    Overcast,
    Fog,
    Drizzle,
    Rain,
    Snow,
    Thunderstorm,
}

/**
 * Whether the sun is up.
 *
 * Kept apart from [Condition] because it is a different fact about the same moment: clear at noon
 * and clear at midnight are one condition seen twice, not two conditions.
 */
enum class PartOfDay { Day, Night }
