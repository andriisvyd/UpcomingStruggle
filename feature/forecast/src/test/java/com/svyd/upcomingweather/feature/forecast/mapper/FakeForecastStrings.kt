package com.svyd.upcomingweather.feature.forecast.mapper

import com.svyd.upcomingweather.core.domain.model.Condition
import com.svyd.upcomingweather.core.domain.model.Remark

/**
 * Returns tokens rather than wording, so an assertion names what the mapper chose instead of
 * repeating a sentence from `strings.xml`.
 *
 * A test that expects "Felt like 29°" fails when the wording is edited; one that expects `29deg`
 * fails only when the mapper stops rounding or stops reaching for the temperature.
 */
internal class FakeForecastStrings : ForecastStrings {

    override fun remark(remark: Remark): String = "remark:${remark::class.simpleName}"

    override fun condition(condition: Condition): String = "condition:${condition.name}"

    override fun reading(reading: ReadingLabel): String = "reading:${reading.name}"

    override fun detail(detail: ReadingDetail, value: String): String = "detail:${detail.name}=$value"

    override fun today(): String = TODAY

    override fun dayTitle(name: String, date: String): String = "title:$name|$date"

    override fun dayLogHeader(name: String): String = "log:$name"

    override fun millimetres(value: Double): String = "${value}mm"

    override fun now(): String = NOW

    override fun noValue(): String = NO_VALUE

    override fun currentLocation(): String = CURRENT_LOCATION

    override fun offline(): String = OFFLINE

    override fun temperature(degrees: Int): String = "${degrees}deg"

    override fun percentage(value: Int): String = "$value%"

    override fun speed(kilometresPerHour: Int): String = "${kilometresPerHour}kmh"

    override fun pressure(hectopascals: Int): String = "${hectopascals}hPa"

    companion object {
        const val TODAY = "TODAY"
        const val NOW = "NOW"
        const val NO_VALUE = "--"
        const val CURRENT_LOCATION = "CURRENT"
        const val OFFLINE = "OFFLINE"
    }
}
