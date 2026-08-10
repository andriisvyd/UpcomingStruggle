package com.svyd.upcomingweather.feature.forecast.mapper

import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.domain.model.Condition
import com.svyd.upcomingweather.core.domain.model.PartOfDay
import com.svyd.upcomingweather.feature.forecast.model.Freshness
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The mapper is where a forecast stops being facts about weather and becomes characters on a page,
 * and it is the last place two zones, a span and an age are still separable.
 *
 * Every test fixes the reader's zone rather than reading the machine's, so the same assertions hold
 * wherever the suite runs.
 */
class ForecastUiMapperTest {

    // Week span

    @Test
    fun `each day is placed inside the week's whole span`() {
        val days = listOf(
            dayForecast(TOKYO, TODAY, minimum = 18.0, maximum = 28.0),
            dayForecast(TOKYO, TODAY.plusDays(1), minimum = 10.0, maximum = 20.0),
            dayForecast(TOKYO, TODAY.plusDays(2), minimum = 20.0, maximum = 30.0),
        )

        val drawn = mapper().days(forecast(TOKYO, days), NOW)

        // The week runs 10° to 30°, so every bar is drawn against a 20° track.
        assertEquals(listOf(0.4f, 0.0f, 0.5f), drawn.map { it.rangeStart })
        assertEquals(listOf(0.9f, 0.5f, 1.0f), drawn.map { it.rangeEnd })
    }

    /** Readings captured from the provider, where the defect this pins was found. */
    @Test
    fun `two days that print the same figures draw the same bar`() {
        val days = listOf(
            dayForecast(TOKYO, TODAY, minimum = 21.4, maximum = 30.7),
            dayForecast(TOKYO, TODAY.plusDays(1), minimum = 21.7, maximum = 28.4),
            dayForecast(TOKYO, TODAY.plusDays(2), minimum = 21.6, maximum = 27.5),
        )

        val drawn = mapper().days(forecast(TOKYO, days), NOW)
        val saturday = drawn[1]
        val sunday = drawn[2]

        // Both read 22° to 28°, so both are drawn the same. Measured against the unrounded
        // readings their bars end a sixth of the track apart.
        assertEquals(listOf("22deg", "28deg"), listOf(saturday.min, saturday.max))
        assertEquals(listOf(sunday.min, sunday.max), listOf(saturday.min, saturday.max))
        assertEquals(sunday.rangeStart, saturday.rangeStart)
        assertEquals(sunday.rangeEnd, saturday.rangeEnd)
    }

    @Test
    fun `a slot is marked against the day in the degrees the row prints`() {
        val day = dayForecast(
            TOKYO,
            TODAY,
            minimum = 21.6,
            maximum = 27.5,
            hours = hoursOf(TOKYO, TODAY, temperature = { 21.6 + it * 0.1 }),
        )

        val slots = mapper().slots(day)

        // 21.6 and 21.9 both print 22°, so both sit at the foot of a 22°–28° day.
        assertEquals("22deg", slots.first().temperature)
        assertEquals("22deg", slots[1].temperature)
        assertEquals(slots[1].markerFraction, slots.first().markerFraction)
    }

    @Test
    fun `a week with no span at all does not divide by zero`() {
        val days = week(TOKYO, TODAY, count = 3, minimum = 20.0, maximum = 20.0)

        val drawn = mapper().days(forecast(TOKYO, days), NOW)

        assertEquals(listOf(0f, 0f, 0f), drawn.map { it.rangeStart })
        assertEquals(listOf(0f, 0f, 0f), drawn.map { it.rangeEnd })
    }

    // Two clocks

    @Test
    fun `the hour strip is labelled on the reader's clock`() {
        val forecast = forecast(TOKYO, week(TOKYO, TODAY))

        val hours = mapper().hours(forecast, NOW, count = 3)

        // 12:00 in Tokyo is 05:00 in Budapest; the hour in progress is named rather than timed.
        assertEquals(listOf(FakeForecastStrings.NOW, "06:00", "07:00"), hours.map { it.time })
    }

    @Test
    fun `sunrise and sunset stay on the place's clock`() {
        val day = dayForecast(TOKYO, TODAY)

        val sunrise = mapper().dayReadings(day).single { it.label == "reading:Sunrise" }

        assertEquals("05:00", sunrise.value)
        assertEquals("detail:Sunset=20:00", sunrise.detail)
    }

    @Test
    fun `the day log stays on the place's clock`() {
        val day = dayForecast(TOKYO, TODAY)

        val slots = mapper().slots(day)

        assertEquals(listOf("00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00"), slots.map { it.time })
    }

    @Test
    fun `the day being read is the place's day, not the reader's`() {
        // 16:00 UTC is already the tenth in Tokyo and still the ninth in Budapest.
        val crossing = Instant.parse("2026-08-09T16:00:00Z")
        val forecast = forecast(TOKYO, week(TOKYO, TODAY))

        val drawn = mapper().days(forecast, crossing)

        assertEquals(FakeForecastStrings.TODAY, drawn.first().name)
    }

    // Freshness

    @Test
    fun `a forecast younger than the age needs no time on it`() {
        val forecast = forecast(TOKYO, week(TOKYO, TODAY), retrievedAt = NOW.minusSeconds(59))

        val hero = mapper().hero(forecast, NOW)

        assertEquals(Freshness.Fresh, hero.freshness)
    }

    @Test
    fun `an older forecast carries when it was obtained, on the reader's clock`() {
        val forecast = forecast(TOKYO, week(TOKYO, TODAY), retrievedAt = NOW.minusSeconds(300))

        val hero = mapper().hero(forecast, NOW)

        // 02:55 UTC is 04:55 in Budapest, where the reader is — not 11:55, where the weather is.
        assertEquals(Freshness.Stale(refreshedAt = "04:55"), hero.freshness)
    }

    @Test
    fun `a forecast exactly the age old has stopped being current`() {
        val forecast = forecast(TOKYO, week(TOKYO, TODAY), retrievedAt = NOW.minusSeconds(60))

        val hero = mapper().hero(forecast, NOW)

        assertTrue(hero.freshness is Freshness.Stale)
    }

    // Condition to glyph

    @Test
    fun `clear by day and clear by night reach different glyphs`() {
        val byDay = conditions(TODAY.atStartOfDay(TOKYO), partOfDay = PartOfDay.Day)
        val byNight = conditions(TODAY.atStartOfDay(TOKYO), partOfDay = PartOfDay.Night)

        val mapper = mapper()

        assertEquals(
            NoirCondition.ClearDay,
            mapper.hero(forecast(TOKYO, week(TOKYO, TODAY), current = byDay), NOW).condition,
        )
        assertEquals(
            NoirCondition.ClearNight,
            mapper.hero(forecast(TOKYO, week(TOKYO, TODAY), current = byNight), NOW).condition,
        )
    }

    @Test
    fun `a whole day is drawn with the day glyph whenever it is read`() {
        val days = week(TOKYO, TODAY, count = 1)
        val atNight = Instant.parse("2026-08-09T15:00:00Z")

        val drawn = mapper().days(forecast(TOKYO, days), atNight)

        assertEquals(NoirCondition.ClearDay, drawn.single().condition)
    }

    // Hour strip

    @Test
    fun `the strip starts at the hour in progress rather than the next one`() {
        // 12:20 in Tokyo: the reader is standing in the hour that began at 12:00.
        val partWay = Instant.parse("2026-08-10T03:20:00Z")
        val forecast = forecast(TOKYO, week(TOKYO, TODAY))

        val hours = mapper().hours(forecast, partWay, count = 1)

        // Temperatures run 20° + the hour, so the twelfth hour is the one at 32°.
        assertEquals("32deg", hours.single().temperature)
    }

    // Precipitation

    @Test
    fun `a chance too small to act on is not reported`() {
        val day = dayForecast(TOKYO, TODAY, hours = hoursOf(TOKYO, TODAY, precipitationChance = { 9 }))

        val slots = mapper().slots(day)

        assertNull(slots.first().precip)
    }

    @Test
    fun `a chance worth reporting is`() {
        val day = dayForecast(TOKYO, TODAY, hours = hoursOf(TOKYO, TODAY, precipitationChance = { 10 }))

        val slots = mapper().slots(day)

        assertEquals("10%", slots.first().precip)
    }

    // Day names

    @Test
    fun `only the day in progress is named, the rest are weekdays`() {
        val drawn = mapper().days(forecast(TOKYO, week(TOKYO, TODAY, count = 3)), NOW)

        assertEquals(
            listOf(
                FakeForecastStrings.TODAY,
                TODAY.plusDays(1).format(WEEKDAY),
                TODAY.plusDays(2).format(WEEKDAY),
            ),
            drawn.map { it.name },
        )
    }

    @Test
    fun `the day title names the day alongside its date`() {
        val day = dayForecast(TOKYO, TODAY)

        val title = mapper().dayTitle(day, NOW, TOKYO)

        assertEquals("title:${FakeForecastStrings.TODAY}|${TODAY.format(DAY_DATE)}", title)
    }

    // Hero and readings

    @Test
    fun `a forecast that does not reach the reader's day has no high or low to give`() {
        val later = week(TOKYO, TODAY.plusDays(10))

        val hero = mapper().hero(forecast(TOKYO, later), NOW)

        assertEquals(FakeForecastStrings.NO_VALUE, hero.high)
        assertEquals(FakeForecastStrings.NO_VALUE, hero.low)
    }

    @Test
    fun `the sunrise reading is dropped when there is no day to read it from`() {
        val readings = mapper().readings(conditions(TODAY.atStartOfDay(TOKYO)), day = null)

        assertEquals(
            listOf("reading:Humidity", "reading:Wind", "reading:Pressure"),
            readings.map { it.label },
        )
    }

    @Test
    fun `a wind with no gust reported reads as no value`() {
        val still = conditions(TODAY.atStartOfDay(TOKYO), gust = null)

        val wind = mapper().readings(still, day = null).single { it.label == "reading:Wind" }

        assertEquals("detail:Gusts=${FakeForecastStrings.NO_VALUE}", wind.detail)
    }

    // Slots

    @Test
    fun `each slot is marked against the day's own span`() {
        val day = dayForecast(TOKYO, TODAY, minimum = 18.0, maximum = 28.0)

        val slots = mapper().slots(day)

        // Hours run 20° + the hour against an 18°–28° day: midnight sits a fifth of the way up.
        assertEquals(0.2f, slots.first().markerFraction)
        assertEquals(0.5f, slots[1].markerFraction)
    }

    @Test
    fun `a place with no name of its own is left for the caller to name`() {
        assertNull(mapper().city(com.svyd.upcomingweather.core.domain.model.PlaceLabel.NamelessCurrentLocation))
        assertEquals(FakeForecastStrings.CURRENT_LOCATION, mapper().currentLocationName())
    }

    private fun mapper() = ForecastUiMapper(
        strings = FakeForecastStrings(),
        maxAge = MAX_AGE,
        deviceZone = { BUDAPEST },
    )

    private companion object {
        val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
        val BUDAPEST: ZoneId = ZoneId.of("Europe/Budapest")

        /** 12:00 where the weather is, 05:00 where the reader is. */
        val NOW: Instant = Instant.parse("2026-08-10T03:00:00Z")
        val TODAY: LocalDate = LocalDate.of(2026, 8, 10)

        val MAX_AGE: Duration = Duration.ofMinutes(1)

        val WEEKDAY: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        val DAY_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    }
}
