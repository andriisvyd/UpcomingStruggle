package com.svyd.upcomingweather.feature.forecast.model

import androidx.compose.runtime.Immutable
import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition

/**
 * Everything the forecast screen can be, and nothing else — the screen renders exactly one of
 * these and has no side-channel flags.
 *
 * Every field is finished for display. Rounding, unit conversion, time zones and wording are all
 * settled before this point; the screen only lays characters out.
 */
@Immutable
sealed interface ForecastUiState {

    /** No place has been chosen yet. */
    data object Empty : ForecastUiState

    /**
     * Location was asked for and refused.
     *
     * Separate from [Empty] because the way out differs. [canAskAgain] is what the platform will
     * still do: while it is true the system prompt appears on another attempt, and once it is false
     * only settings can reverse the refusal — so the screen offers one or the other, never a button
     * that cannot work.
     */
    data class LocationRefused(val canAskAgain: Boolean) : ForecastUiState

    /**
     * Location was granted, but the device could not say where it is.
     *
     * Separate from [LocationRefused] because trying again can work here, and from [Error] because
     * nothing was wrong with the forecast — it was never asked for.
     */
    data object LocationUnavailable : ForecastUiState

    /** Nothing stored and the first fetch is in flight. The place may not be named yet. */
    data object Loading : ForecastUiState

    data class Content(
        val city: String,
        val hero: HeroUi,
        val hours: List<HourUi>,
        val readings: List<ReadingUi>,
        val days: List<DayUi>,
        /** A fetch is under way behind what is drawn. */
        val isRefreshing: Boolean = false,
        /** Set when the last fetch failed and what is drawn came from storage. */
        val offline: OfflineUi? = null,
    ) : ForecastUiState

    /** The fetch failed and nothing was stored to fall back on. */
    data object Error : ForecastUiState
}

@Immutable
data class HeroUi(
    /** The current temperature, formatted with its degree sign. */
    val temperature: String,
    val condition: NoirCondition,
    /** Names the condition in words; also what a screen reader announces. */
    val conditionLabel: String,
    /** The line under the stamp. Decoration as far as a screen reader is concerned. */
    val line: String,
    /** What it feels like, or null where the screen does not show one. */
    val feelsLike: String? = null,
    val high: String,
    val low: String,
    /** How recently this forecast was obtained. */
    val freshness: Freshness,
)

/**
 * How current the forecast on screen is.
 *
 * Two cases rather than a timestamp and a flag, so the screen cannot show a time it should not or
 * omit one it should.
 */
@Immutable
sealed interface Freshness {

    /** Obtained recently enough that no time is worth showing. */
    data object Fresh : Freshness

    /** Older than that; [refreshedAt] is when it was obtained, on the reader's own clock. */
    data class Stale(val refreshedAt: String) : Freshness
}

@Immutable
data class HourUi(
    /** "Now", "12:00" — on the reader's clock, since the question is what happens next for them. */
    val time: String,
    val condition: NoirCondition,
    val temperature: String,
    /** Null when there is nothing worth reporting; the column keeps its height either way. */
    val precip: String? = null,
    val contentDescription: String,
)

@Immutable
data class ReadingUi(
    /** Uppercased by the row that draws it. */
    val label: String,
    val value: String,
    /** A second figure about the same reading. */
    val detail: String,
)

@Immutable
data class DayUi(
    /** The date this row opens, in ISO form. */
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
    /** Says that what is drawn came from storage. When it was obtained is on [HeroUi.freshness]. */
    val text: String,
)

/** The day-details destination. */
@Immutable
sealed interface DayDetailsUiState {

    /** Nothing to draw yet. */
    data object Loading : DayDetailsUiState

    data class Content(
        /** Names the day being shown. */
        val title: String,
        val hero: HeroUi,
        /** Heads the list of slots. */
        val logHeader: String,
        val slots: List<SlotUi>,
        val readings: List<ReadingUi>,
    ) : DayDetailsUiState

    /** No such day in the forecast, or no forecast to look in. */
    data object Unavailable : DayDetailsUiState
}

@Immutable
data class SlotUi(
    /** On the place's clock: these slots divide that place's day. */
    val time: String,
    val condition: NoirCondition,
    val precip: String? = null,
    /** Where this slot's temperature sits inside the day's span, 0..1. */
    val markerFraction: Float,
    val temperature: String,
    val contentDescription: String,
)
