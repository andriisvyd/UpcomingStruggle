package com.svyd.upcomingweather.feature.forecast.model

import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition

import androidx.compose.runtime.Immutable

/**
 * Everything the forecast screen can be, and nothing else — the screen renders exactly one of
 * these and has no side-channel flags.
 *
 * Every field is a finished display string. Rounding, unit conversion, timezone shifting and the
 * copy deck all happen before this point; the UI only lays characters out.
 */
@Immutable
sealed interface ForecastUiState {

    /** First launch: no city has been picked yet. */
    data object Empty : ForecastUiState

    /** A city is selected, nothing is cached, the first fetch is in flight. */
    data class Loading(val city: String) : ForecastUiState

    data class Content(
        val city: String,
        val hero: HeroUi,
        val hours: List<HourUi>,
        val readings: List<ReadingUi>,
        val days: List<DayUi>,
        val isRefreshing: Boolean = false,
        /** Non-null when the last refresh failed but the cache still renders. */
        val offline: OfflineUi? = null,
    ) : ForecastUiState

    /** The fetch failed and there is nothing cached to fall back on. */
    data class Error(val city: String) : ForecastUiState
}

@Immutable
data class HeroUi(
    /** "27°" */
    val temperature: String,
    val condition: NoirCondition,
    /** Semantic, literal, not voiced — "Partly cloudy". Doubles as the screen-reader label. */
    val conditionLabel: String,
    /** The voiced line under the stamp. Decorative as far as a screen reader is concerned. */
    val line: String,
    /** "29°", or null on the day-details variant, which has no feels-like. */
    val feelsLike: String? = null,
    val high: String,
    val low: String,
    /** "10:12" — when the trail was last warm. */
    val updatedAt: String,
)

@Immutable
data class HourUi(
    /** "Now", "12:00" — already in the city's timezone, 24-hour. */
    val time: String,
    val condition: NoirCondition,
    val temperature: String,
    /** Null below 10% — the column keeps the line's height either way. */
    val precip: String? = null,
    val contentDescription: String,
)

@Immutable
data class ReadingUi(
    /** "HUMIDITY" — uppercased by the row anyway. */
    val label: String,
    val value: String,
    val detail: String,
)

@Immutable
data class DayUi(
    /** The details route's key. */
    val date: String,
    /** "Today", "Wed". */
    val name: String,
    val condition: NoirCondition,
    val precip: String? = null,
    val min: String,
    val max: String,
    /** Where this day's low sits inside the week's span, 0..1. */
    val rangeStart: Float,
    /** Where this day's high sits inside the week's span, 0..1. */
    val rangeEnd: Float,
    val contentDescription: String,
)

@Immutable
data class OfflineUi(
    /** "Offline — cold trail from 09:12" */
    val text: String,
)

/** The day-details destination. It renders from cache, so it has no states of its own. */
@Immutable
data class DayDetailsUiState(
    /** "Friday · Aug 7" */
    val title: String,
    val hero: HeroUi,
    /** "Friday, step by step" */
    val logHeader: String,
    val slots: List<SlotUi>,
    val readings: List<ReadingUi>,
)

@Immutable
data class SlotUi(
    val time: String,
    val condition: NoirCondition,
    val precip: String? = null,
    /** Where this slot's temperature sits inside the day's span, 0..1. */
    val markerFraction: Float,
    val temperature: String,
    val contentDescription: String,
)
