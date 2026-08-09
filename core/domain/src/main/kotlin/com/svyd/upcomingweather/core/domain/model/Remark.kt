package com.svyd.upcomingweather.core.domain.model

/**
 * The reading the app takes of the weather — the line under the temperature.
 *
 * A remark is a verdict, not a sentence: the wording is a presentation concern and lives with the
 * strings, while which verdict applies is decided here. That split is what lets the same forecast
 * speak a different language without the rule changing.
 */
sealed interface Remark {

    data object ClearAndBright : Remark

    data object ClearAndStill : Remark

    data object CloudsCircling : Remark

    data object LidOfCloud : Remark

    data object FogOnTheStreet : Remark

    data object ThinDrizzle : Remark

    data object RainLikeADebt : Remark

    data object SnowSettling : Remark

    data object ThunderOnTheWire : Remark
}

/**
 * The verdict for a moment's weather.
 *
 * Only clear skies read differently by night; the rest do the same thing whether or not anyone can
 * see them.
 */
fun remarkFor(condition: Condition, partOfDay: PartOfDay): Remark = when (condition) {
    Condition.Clear -> when (partOfDay) {
        PartOfDay.Day -> Remark.ClearAndBright
        PartOfDay.Night -> Remark.ClearAndStill
    }

    Condition.PartlyCloudy -> Remark.CloudsCircling
    Condition.Overcast -> Remark.LidOfCloud
    Condition.Fog -> Remark.FogOnTheStreet
    Condition.Drizzle -> Remark.ThinDrizzle
    Condition.Rain -> Remark.RainLikeADebt
    Condition.Snow -> Remark.SnowSettling
    Condition.Thunderstorm -> Remark.ThunderOnTheWire
}
