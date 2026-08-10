package com.svyd.upcomingweather.feature.forecast.mapper

import android.content.Context
import androidx.annotation.StringRes
import com.svyd.upcomingweather.core.domain.model.Condition
import com.svyd.upcomingweather.core.domain.model.Remark
import com.svyd.upcomingweather.feature.forecast.R

/**
 * Every word and every unit the mapper puts on screen.
 *
 * Behind an interface so the mapper stays a plain function of its inputs: resources need a context,
 * and a mapper holding one cannot be read in a unit test. Nothing a reader sees is written in Kotlin.
 */
internal interface ForecastStrings {

    fun remark(remark: Remark): String

    fun condition(condition: Condition): String

    fun reading(reading: ReadingLabel): String

    fun detail(detail: ReadingDetail, value: String): String

    /** Names the day in progress. Every other day is named by the calendar. */
    fun today(): String

    /** Names the day being shown, alongside its date. */
    fun dayTitle(name: String, date: String): String

    /** Heads the list of slots on the day screen. */
    fun dayLogHeader(name: String): String

    fun millimetres(value: Double): String

    /** The label on the first column of the hour strip. */
    fun now(): String

    /** Stands in for a figure that is not available. */
    fun noValue(): String

    /** Names a place that nothing could put a name to. */
    fun currentLocation(): String

    /** Says that what is drawn came from storage. */
    fun offline(): String

    fun temperature(degrees: Int): String

    fun percentage(value: Int): String

    fun speed(kilometresPerHour: Int): String

    fun pressure(hectopascals: Int): String
}

internal enum class ReadingLabel { Humidity, Wind, Sunrise, Pressure, Precipitation }

internal enum class ReadingDetail { CloudCover, Gusts, Sunset, GroundPressure, Rainfall }

internal class AndroidForecastStrings(private val context: Context) : ForecastStrings {

    override fun remark(remark: Remark): String = string(
        when (remark) {
            Remark.ClearAndBright -> R.string.forecast_remark_clear_day
            Remark.ClearAndStill -> R.string.forecast_remark_clear_night
            Remark.CloudsCircling -> R.string.forecast_remark_partly_cloudy
            Remark.LidOfCloud -> R.string.forecast_remark_overcast
            Remark.FogOnTheStreet -> R.string.forecast_remark_fog
            Remark.ThinDrizzle -> R.string.forecast_remark_drizzle
            Remark.RainLikeADebt -> R.string.forecast_remark_rain
            Remark.SnowSettling -> R.string.forecast_remark_snow
            Remark.ThunderOnTheWire -> R.string.forecast_remark_thunder
        },
    )

    override fun condition(condition: Condition): String = string(
        when (condition) {
            Condition.Clear -> R.string.forecast_condition_clear
            Condition.PartlyCloudy -> R.string.forecast_condition_partly_cloudy
            Condition.Overcast -> R.string.forecast_condition_overcast
            Condition.Fog -> R.string.forecast_condition_fog
            Condition.Drizzle -> R.string.forecast_condition_drizzle
            Condition.Rain -> R.string.forecast_condition_rain
            Condition.Snow -> R.string.forecast_condition_snow
            Condition.Thunderstorm -> R.string.forecast_condition_thunder
        },
    )

    override fun reading(reading: ReadingLabel): String = string(
        when (reading) {
            ReadingLabel.Humidity -> R.string.forecast_reading_humidity
            ReadingLabel.Wind -> R.string.forecast_reading_wind
            ReadingLabel.Sunrise -> R.string.forecast_reading_sunrise
            ReadingLabel.Pressure -> R.string.forecast_reading_pressure
            ReadingLabel.Precipitation -> R.string.forecast_reading_precipitation
        },
    )

    override fun detail(detail: ReadingDetail, value: String): String = context.getString(
        when (detail) {
            ReadingDetail.CloudCover -> R.string.forecast_detail_cloud_cover
            ReadingDetail.Gusts -> R.string.forecast_detail_gusts
            ReadingDetail.Sunset -> R.string.forecast_detail_sunset
            ReadingDetail.GroundPressure -> R.string.forecast_detail_ground_pressure
            ReadingDetail.Rainfall -> R.string.forecast_detail_rainfall
        },
        value,
    )

    override fun today(): String = string(R.string.forecast_day_today)

    override fun dayTitle(name: String, date: String): String =
        context.getString(R.string.forecast_day_title, name, date)

    override fun dayLogHeader(name: String): String =
        context.getString(R.string.forecast_day_log_header, name)

    override fun millimetres(value: Double): String =
        context.getString(R.string.forecast_millimetres, value)

    override fun now(): String = string(R.string.forecast_hour_now)

    override fun noValue(): String = string(R.string.forecast_no_value)

    override fun currentLocation(): String = string(R.string.forecast_current_location)

    override fun offline(): String = string(R.string.forecast_offline)

    override fun temperature(degrees: Int): String =
        context.getString(R.string.forecast_degrees, degrees)

    override fun percentage(value: Int): String =
        context.getString(R.string.forecast_percentage, value)

    override fun speed(kilometresPerHour: Int): String =
        context.getString(R.string.forecast_wind_speed, kilometresPerHour)

    override fun pressure(hectopascals: Int): String =
        context.getString(R.string.forecast_pressure, hectopascals)

    private fun string(@StringRes id: Int): String = context.getString(id)
}
