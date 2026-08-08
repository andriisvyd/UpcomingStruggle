package com.svyd.upcomingweather.feature.forecast.mock

import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition

import com.svyd.upcomingweather.feature.forecast.model.DayDetailsUiState
import com.svyd.upcomingweather.feature.forecast.model.DayUi
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import com.svyd.upcomingweather.feature.forecast.model.HeroUi
import com.svyd.upcomingweather.feature.forecast.model.HourUi
import com.svyd.upcomingweather.feature.forecast.model.OfflineUi
import com.svyd.upcomingweather.feature.forecast.model.ReadingUi
import com.svyd.upcomingweather.feature.forecast.model.SlotUi

/**
 * Budapest on the fourth of August, exactly as the spec draws it.
 *
 * This is what a data layer would hand the screen once it exists: finished strings, fractions
 * already worked out. Previews and the app both read from here.
 */
object MockForecast {

    val content: ForecastUiState.Content = ForecastUiState.Content(
        city = "Budapest",
        hero = HeroUi(
            temperature = "27°",
            condition = NoirCondition.Partly,
            conditionLabel = "Partly cloudy",
            line = "Clouds circle like old regrets.",
            feelsLike = "29°",
            high = "29°",
            low = "19°",
            updatedAt = "10:12",
        ),
        hours = listOf(
            hour("Now", NoirCondition.Partly, "27°"),
            hour("12:00", NoirCondition.ClearDay, "28°"),
            hour("15:00", NoirCondition.ClearDay, "28°"),
            hour("18:00", NoirCondition.Partly, "26°", "10%"),
            hour("21:00", NoirCondition.ClearNight, "22°"),
            hour("00:00", NoirCondition.ClearNight, "21°"),
            hour("03:00", NoirCondition.ClearNight, "19°"),
            hour("06:00", NoirCondition.ClearDay, "19°"),
            hour("09:00", NoirCondition.ClearDay, "24°"),
        ),
        readings = listOf(
            ReadingUi("Humidity", "46%", "clouds packing 40%"),
            ReadingUi("Wind", "12 km/h", "gusts of 26, no warning"),
            ReadingUi("Sunrise", "05:31", "dark again by 20:14"),
            ReadingUi("Pressure", "1014 hPa", "1010 hPa on the street"),
        ),
        days = days,
    )

    /** Frame B — the same city after dark. */
    val night: ForecastUiState.Content = content.copy(
        hero = HeroUi(
            temperature = "22°",
            condition = NoirCondition.ClearNight,
            conditionLabel = "Clear",
            line = "The night held its breath.",
            feelsLike = "21°",
            high = "29°",
            low = "19°",
            updatedAt = "22:41",
        ),
        hours = listOf(
            hour("Now", NoirCondition.ClearNight, "22°"),
            hour("00:00", NoirCondition.ClearNight, "21°"),
            hour("03:00", NoirCondition.ClearNight, "19°"),
            hour("06:00", NoirCondition.ClearDay, "19°"),
            hour("09:00", NoirCondition.ClearDay, "24°"),
            hour("12:00", NoirCondition.ClearDay, "28°"),
            hour("15:00", NoirCondition.Partly, "28°", "10%"),
            hour("18:00", NoirCondition.Partly, "26°"),
        ),
        readings = listOf(
            ReadingUi("Humidity", "62%", "clouds packing 10%"),
            ReadingUi("Wind", "8 km/h", "gusts of 14, no warning"),
            ReadingUi("Sunrise", "05:31", "dark again by 20:14"),
            ReadingUi("Pressure", "1016 hPa", "1002 hPa on the street"),
        ),
    )

    val refreshing: ForecastUiState.Content = content.copy(isRefreshing = true)

    val offline: ForecastUiState.Content =
        content.copy(offline = OfflineUi("Offline — cold trail from 09:12"))

    val loading: ForecastUiState = ForecastUiState.Loading(city = "Budapest")

    val error: ForecastUiState = ForecastUiState.Error(city = "Budapest")

    val empty: ForecastUiState = ForecastUiState.Empty

    /** Frame H — a day opened from the list. Renders from the forecast above, never from a fetch. */
    val friday: DayDetailsUiState = DayDetailsUiState(
        title = "Friday · Aug 7",
        hero = HeroUi(
            temperature = "27°",
            condition = NoirCondition.Rain,
            conditionLabel = "Rain",
            line = "Bloody awful rain, cold as a debt.",
            feelsLike = null,
            high = "27°",
            low = "18°",
            updatedAt = "10:12",
        ),
        logHeader = "Friday, step by step",
        slots = listOf(
            slot("00:00", NoirCondition.ClearNight, null, 19, 18, 27),
            slot("03:00", NoirCondition.Rain, "30%", 18, 18, 27),
            slot("06:00", NoirCondition.Rain, "55%", 18, 18, 27),
            slot("09:00", NoirCondition.Rain, "60%", 20, 18, 27),
            slot("12:00", NoirCondition.Rain, "60%", 23, 18, 27),
            slot("15:00", NoirCondition.Partly, "35%", 26, 18, 27),
            slot("18:00", NoirCondition.Partly, null, 25, 18, 27),
            slot("21:00", NoirCondition.ClearNight, null, 21, 18, 27),
        ),
        readings = listOf(
            ReadingUi("Humidity", "74%", "clouds packing 90%"),
            ReadingUi("Wind", "18 km/h", "gusts of 34, no warning"),
            ReadingUi("Precip", "60%", "4.2 mm on the books"),
            ReadingUi("Pressure", "1004 hPa", "1000 hPa on the street"),
        ),
    )

    /** Whatever day the list was tapped on. Unknown dates fall back to Friday. */
    fun dayDetails(date: String): DayDetailsUiState = when (date) {
        "2026-08-07" -> friday
        else -> days.firstOrNull { it.date == date }?.let(::detailsFor) ?: friday
    }
}

// The week: min 16°, max 32°, so the span every range bar is drawn against is 16 degrees.
private const val WEEK_MIN = 16
private const val WEEK_MAX = 32

private val days = listOf(
    day("2026-08-04", "Today", NoirCondition.Partly, "10%", 19, 29),
    day("2026-08-05", "Wed", NoirCondition.ClearDay, null, 20, 31),
    day("2026-08-06", "Thu", NoirCondition.ClearDay, "20%", 21, 32),
    day("2026-08-07", "Fri", NoirCondition.Rain, "60%", 18, 27),
    day("2026-08-08", "Sat", NoirCondition.Rain, "80%", 16, 24),
)

private val dayTitles = mapOf(
    "2026-08-04" to ("Today · Aug 4" to "Today, step by step"),
    "2026-08-05" to ("Wednesday · Aug 5" to "Wednesday, step by step"),
    "2026-08-06" to ("Thursday · Aug 6" to "Thursday, step by step"),
    "2026-08-07" to ("Friday · Aug 7" to "Friday, step by step"),
    "2026-08-08" to ("Saturday · Aug 8" to "Saturday, step by step"),
)

private fun day(
    date: String,
    name: String,
    condition: NoirCondition,
    precip: String?,
    min: Int,
    max: Int,
) = DayUi(
    date = date,
    name = name,
    condition = condition,
    precip = precip,
    min = "$min°",
    max = "$max°",
    rangeStart = fraction(min, WEEK_MIN, WEEK_MAX),
    rangeEnd = fraction(max, WEEK_MIN, WEEK_MAX),
    contentDescription = buildString {
        append("$name: ${condition.spoken}")
        if (precip != null) append(", $precip chance of precipitation")
        append(", $min to $max degrees")
    },
)

private fun hour(
    time: String,
    condition: NoirCondition,
    temperature: String,
    precip: String? = null,
) = HourUi(
    time = time,
    condition = condition,
    temperature = temperature,
    precip = precip,
    contentDescription = buildString {
        append("$time, ${temperature.dropLast(1)} degrees")
        if (precip != null) append(", ${precip.dropLast(1)} percent chance of rain")
    },
)

private fun slot(
    time: String,
    condition: NoirCondition,
    precip: String?,
    temperature: Int,
    dayMin: Int,
    dayMax: Int,
) = SlotUi(
    time = time,
    condition = condition,
    precip = precip,
    markerFraction = fraction(temperature, dayMin, dayMax),
    temperature = "$temperature°",
    contentDescription = buildString {
        append("$time, $temperature degrees")
        if (precip != null) append(", ${precip.dropLast(1)} percent chance of rain")
    },
)

/** A day the spec does not draw in full: the log is shaped around its own min and max. */
private fun detailsFor(day: DayUi): DayDetailsUiState {
    val min = day.min.dropLast(1).toInt()
    val max = day.max.dropLast(1).toInt()
    val shape = listOf(0.1f, 0f, 0f, 0.3f, 0.75f, 1f, 0.8f, 0.35f)
    val times = listOf("00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00")
    val (title, logHeader) = dayTitles.getValue(day.date)
    return DayDetailsUiState(
        title = title,
        hero = HeroUi(
            temperature = day.max,
            condition = day.condition,
            conditionLabel = day.condition.spoken.replaceFirstChar(Char::uppercase),
            line = day.condition.line,
            feelsLike = null,
            high = day.max,
            low = day.min,
            updatedAt = "10:12",
        ),
        logHeader = logHeader,
        slots = times.mapIndexed { index, time ->
            val temperature = min + ((max - min) * shape[index]).toInt()
            val night = time in listOf("00:00", "03:00", "21:00")
            slot(
                time = time,
                // After dark, clear and partly cloudy become the moon; heavier weather keeps its
                // own glyph around the clock.
                condition = if (night) day.condition.afterDark() else day.condition,
                precip = day.precip.takeIf { index in 3..5 },
                temperature = temperature,
                dayMin = min,
                dayMax = max,
            )
        },
        readings = listOf(
            ReadingUi("Humidity", "58%", "clouds packing 30%"),
            ReadingUi("Wind", "14 km/h", "gusts of 22, no warning"),
            ReadingUi("Precip", day.precip ?: "0%", "nothing on the books"),
            ReadingUi("Pressure", "1012 hPa", "1008 hPa on the street"),
        ),
    )
}

private fun NoirCondition.afterDark(): NoirCondition = when (this) {
    NoirCondition.ClearDay, NoirCondition.Partly -> NoirCondition.ClearNight
    else -> this
}

private fun fraction(value: Int, min: Int, max: Int): Float =
    ((value - min).toFloat() / (max - min).coerceAtLeast(1))

/** Literal, for screen readers — the voiced copy never gets read aloud. */
private val NoirCondition.spoken: String
    get() = when (this) {
        NoirCondition.ClearDay, NoirCondition.ClearNight -> "clear"
        NoirCondition.Partly -> "partly cloudy"
        NoirCondition.Overcast -> "overcast"
        NoirCondition.Fog -> "fog"
        NoirCondition.Drizzle -> "drizzle"
        NoirCondition.Rain -> "rain likely"
        NoirCondition.Snow -> "snow"
        NoirCondition.Thunder -> "thunderstorm"
    }

private val NoirCondition.line: String
    get() = when (this) {
        NoirCondition.ClearDay -> "A sky this clean is lying."
        NoirCondition.ClearNight -> "The night held its breath."
        NoirCondition.Partly -> "Clouds circle like old regrets."
        NoirCondition.Overcast -> "A disgusting grey lid on the city."
        NoirCondition.Fog -> "Fog, hiding the city's evidence."
        NoirCondition.Drizzle -> "Drizzle, a slow confession."
        NoirCondition.Rain -> "Bloody awful rain, cold as a debt."
        NoirCondition.Snow -> "Snow falling like it had orders."
        NoirCondition.Thunder -> "The sky turned blasphemous."
    }
