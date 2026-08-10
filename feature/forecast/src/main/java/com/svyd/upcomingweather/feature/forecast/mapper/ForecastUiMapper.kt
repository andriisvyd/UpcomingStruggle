package com.svyd.upcomingweather.feature.forecast.mapper

import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.domain.model.Condition
import com.svyd.upcomingweather.core.domain.model.Conditions
import com.svyd.upcomingweather.core.domain.model.DayForecast
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.HourConditions
import com.svyd.upcomingweather.core.domain.model.PartOfDay
import com.svyd.upcomingweather.core.domain.model.Percentage
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.Temperature
import com.svyd.upcomingweather.core.domain.model.hoursFrom
import com.svyd.upcomingweather.core.domain.model.remarkFor
import com.svyd.upcomingweather.core.domain.model.today
import com.svyd.upcomingweather.feature.forecast.model.DayUi
import com.svyd.upcomingweather.feature.forecast.model.Freshness
import com.svyd.upcomingweather.feature.forecast.model.HeroUi
import com.svyd.upcomingweather.feature.forecast.model.HourUi
import com.svyd.upcomingweather.feature.forecast.model.OfflineUi
import com.svyd.upcomingweather.feature.forecast.model.ReadingUi
import com.svyd.upcomingweather.feature.forecast.model.SlotUi
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Turns a forecast into what the screens draw.
 *
 * Two clocks are in play, answering different questions. The hour strip and the freshness line are
 * on the reader's own clock, because "now" and "in two hours" are theirs. Sunrise, sunset and the
 * day log stay on the place's, because those describe that place's day rather than the reader's.
 */
internal class ForecastUiMapper(
    private val strings: ForecastStrings,
    private val maxAge: Duration,
    private val deviceZone: () -> ZoneId = ZoneId::systemDefault,
) {

    /** Null when nothing could name the place; [currentLocationName] stands in for it. */
    fun city(label: PlaceLabel): String? = when (label) {
        is PlaceLabel.Named -> label.name
        PlaceLabel.NamelessCurrentLocation -> null
    }

    fun currentLocationName(): String = strings.currentLocation()

    /** Says that what is drawn came from storage because the last attempt did not arrive. */
    fun offline(): OfflineUi = OfflineUi(strings.offline())

    fun hero(forecast: Forecast, now: Instant): HeroUi {
        val today = forecast.today(now.atZone(forecast.timeZone))
        return HeroUi(
            temperature = forecast.current.temperature.degrees(),
            condition = forecast.current.condition.noir(forecast.current.partOfDay),
            conditionLabel = strings.condition(forecast.current.condition),
            line = strings.remark(forecast.current.remark),
            feelsLike = forecast.current.feelsLike.degrees(),
            high = today?.maximum?.degrees() ?: strings.noValue(),
            low = today?.minimum?.degrees() ?: strings.noValue(),
            freshness = freshness(forecast.retrievedAt, now),
        )
    }

    /** A whole day rather than a moment, so there is no feels-like and the verdict is the day's. */
    fun dayHero(day: DayForecast, retrievedAt: Instant, now: Instant): HeroUi = HeroUi(
        temperature = day.maximum.degrees(),
        condition = day.condition.noir(PartOfDay.Day),
        conditionLabel = strings.condition(day.condition),
        line = strings.remark(remarkFor(day.condition, PartOfDay.Day)),
        feelsLike = null,
        high = day.maximum.degrees(),
        low = day.minimum.degrees(),
        freshness = freshness(retrievedAt, now),
    )

    /** A day has no humidity or wind of its own; what it does have is rain and daylight. */
    fun dayReadings(day: DayForecast): List<ReadingUi> = listOf(
        ReadingUi(
            label = strings.reading(ReadingLabel.Precipitation),
            value = strings.percentage(day.precipitationChance.value),
            detail = strings.detail(
                ReadingDetail.Rainfall,
                strings.millimetres(day.precipitation.value),
            ),
        ),
        ReadingUi(
            label = strings.reading(ReadingLabel.Sunrise),
            value = day.sunrise.onPlaceClock(),
            detail = strings.detail(ReadingDetail.Sunset, day.sunset.onPlaceClock()),
        ),
    )

    fun dayTitle(day: DayForecast, now: Instant, zone: ZoneId): String =
        strings.dayTitle(dayName(day.date, now.atZone(zone).toLocalDate()), day.date.format(DAY_DATE))

    fun dayLogHeader(day: DayForecast, now: Instant, zone: ZoneId): String =
        strings.dayLogHeader(dayName(day.date, now.atZone(zone).toLocalDate()))

    fun hours(forecast: Forecast, now: Instant, count: Int): List<HourUi> =
        forecast.hoursFrom(now.atZone(forecast.timeZone), count)
            .mapIndexed { index, hour -> hour.toUi(isFirst = index == 0) }

    fun days(forecast: Forecast, now: Instant): List<DayUi> {
        val span = forecast.days.span()
        val today = now.atZone(forecast.timeZone).toLocalDate()
        return forecast.days.map { day -> day.toUi(span, today) }
    }

    fun readings(conditions: Conditions, day: DayForecast?): List<ReadingUi> = buildList {
        add(
            ReadingUi(
                label = strings.reading(ReadingLabel.Humidity),
                value = strings.percentage(conditions.humidity.value),
                detail = strings.detail(
                    ReadingDetail.CloudCover,
                    strings.percentage(conditions.cloudCover.value),
                ),
            ),
        )
        add(
            ReadingUi(
                label = strings.reading(ReadingLabel.Wind),
                value = strings.speed(conditions.wind.speed.kilometresPerHour.roundToInt()),
                detail = strings.detail(
                    ReadingDetail.Gusts,
                    conditions.wind.gust
                        ?.let { strings.speed(it.kilometresPerHour.roundToInt()) }
                        ?: strings.noValue(),
                ),
            ),
        )
        if (day != null) {
            add(
                ReadingUi(
                    label = strings.reading(ReadingLabel.Sunrise),
                    value = day.sunrise.onPlaceClock(),
                    detail = strings.detail(ReadingDetail.Sunset, day.sunset.onPlaceClock()),
                ),
            )
        }
        add(
            ReadingUi(
                label = strings.reading(ReadingLabel.Pressure),
                value = strings.pressure(conditions.pressure.seaLevel.hectopascals.roundToInt()),
                detail = strings.detail(
                    ReadingDetail.GroundPressure,
                    strings.pressure(conditions.pressure.surface.hectopascals.roundToInt()),
                ),
            ),
        )
    }

    fun slots(day: DayForecast): List<SlotUi> {
        val span = day.minimum.rounded()..day.maximum.rounded()
        return day.hours
            .filter { it.time.hour % SLOT_HOURS == 0 }
            .map { hour ->
                val time = hour.time.onPlaceClock()
                SlotUi(
                    time = time,
                    condition = hour.condition.noir(hour.partOfDay),
                    precip = hour.precipitationChance.reportable(),
                    markerFraction = span.fractionOf(hour.temperature.rounded()),
                    temperature = hour.temperature.degrees(),
                    contentDescription = spoken(
                        time,
                        strings.condition(hour.condition),
                        hour.temperature.degrees(),
                    ),
                )
            }
    }

    private fun freshness(retrievedAt: Instant, now: Instant): Freshness =
        if (Duration.between(retrievedAt, now) < maxAge) {
            Freshness.Fresh
        } else {
            Freshness.Stale(refreshedAt = retrievedAt.atZone(deviceZone()).format(HOUR_MINUTE))
        }

    private fun HourConditions.toUi(isFirst: Boolean): HourUi {
        val label = if (isFirst) strings.now() else time.onReaderClock()
        return HourUi(
            time = label,
            condition = condition.noir(partOfDay),
            temperature = temperature.degrees(),
            precip = precipitationChance.reportable(),
            contentDescription = spoken(
                label,
                strings.condition(condition),
                temperature.degrees(),
            ),
        )
    }

    /**
     * The day in progress is the only one the calendar has no useful word for. The rest are named
     * by their weekday, which fits the column the row draws them in.
     */
    private fun dayName(date: LocalDate, today: LocalDate): String =
        if (date == today) strings.today() else date.format(WEEKDAY)

    private fun DayForecast.toUi(span: IntRange, today: LocalDate): DayUi {
        val name = dayName(date, today)
        return DayUi(
            date = date.toString(),
            name = name,
            condition = condition.noir(PartOfDay.Day),
            precip = precipitationChance.reportable(),
            min = minimum.degrees(),
            max = maximum.degrees(),
            rangeStart = span.fractionOf(minimum.rounded()),
            rangeEnd = span.fractionOf(maximum.rounded()),
            contentDescription = spoken(
                name,
                strings.condition(condition),
                minimum.degrees(),
                maximum.degrees(),
            ),
        )
    }

    /**
     * The week's whole span, which every day's bar is drawn against.
     *
     * In the same whole degrees the rows print. A bar drawn from the unrounded reading disagrees
     * with the two figures beside it — two days both reading 22° to 28° would draw different bars —
     * and restating those figures is the only job the bar has.
     */
    private fun List<DayForecast>.span(): IntRange {
        val low = minOfOrNull { it.minimum.rounded() } ?: 0
        val high = maxOfOrNull { it.maximum.rounded() } ?: low
        return low..high
    }

    private fun IntRange.fractionOf(value: Int): Float {
        val width = last - first
        if (width <= 0) return 0f
        return ((value - first).toFloat() / width).coerceIn(0f, 1f)
    }

    private fun Condition.noir(partOfDay: PartOfDay): NoirCondition = when (this) {
        Condition.Clear ->
            if (partOfDay == PartOfDay.Day) NoirCondition.ClearDay else NoirCondition.ClearNight

        Condition.PartlyCloudy -> NoirCondition.Partly
        Condition.Overcast -> NoirCondition.Overcast
        Condition.Fog -> NoirCondition.Fog
        Condition.Drizzle -> NoirCondition.Drizzle
        Condition.Rain -> NoirCondition.Rain
        Condition.Snow -> NoirCondition.Snow
        Condition.Thunderstorm -> NoirCondition.Thunder
    }

    /** Times that describe the place's own day. */
    private fun ZonedDateTime.onPlaceClock(): String = format(HOUR_MINUTE)

    /** Times that answer "when, for me". */
    private fun ZonedDateTime.onReaderClock(): String =
        withZoneSameInstant(deviceZone()).format(HOUR_MINUTE)

    /** The reading as it is printed and as every bar is measured against, so the two agree. */
    private fun Temperature.rounded(): Int = celsius.roundToInt()

    private fun Temperature.degrees(): String = strings.temperature(rounded())

    /** Below the threshold there is nothing worth saying, and the column keeps its height. */
    private fun Percentage.reportable(): String? =
        value.takeIf { it >= REPORTABLE_PRECIPITATION }?.let(strings::percentage)

    private fun spoken(vararg parts: String): String = parts.joinToString(separator = ", ")

    private companion object {
        const val SLOT_HOURS = 3
        const val REPORTABLE_PRECIPITATION = 10

        val HOUR_MINUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
        val WEEKDAY: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        val DAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    }
}
