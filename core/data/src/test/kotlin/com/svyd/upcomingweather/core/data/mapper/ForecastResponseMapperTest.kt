package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.data.cloud.dto.ForecastResponse
import com.svyd.upcomingweather.core.domain.model.Condition
import com.svyd.upcomingweather.core.domain.model.PartOfDay
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.Remark
import com.svyd.upcomingweather.core.domain.model.hoursFrom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ForecastResponseMapperTest {

    private val response: ForecastResponse =
        Fixtures.json.decodeFromString(Fixtures.read("forecast-budapest.json"))

    private val forecast = response.toForecast(PlaceLabel.Named("Budapest"))

    @Test
    fun `the label is carried in, since the response has no place in it`() {
        assertEquals(PlaceLabel.Named("Budapest"), forecast.label)
    }

    @Test
    fun `times keep the clock of the place, not the reader`() {
        assertEquals(ZoneId.of("Europe/Budapest"), forecast.timeZone)
        assertEquals(ZoneId.of("Europe/Budapest"), forecast.current.observedAt.zone)
        assertEquals(
            "the naive local string is read as the place's own time",
            ZonedDateTime.of(2026, 8, 9, 15, 15, 0, 0, ZoneId.of("Europe/Budapest")),
            forecast.current.observedAt,
        )
    }

    @Test
    fun `current conditions come across whole`() {
        val current = forecast.current
        assertEquals(32.1, current.temperature.celsius, 0.01)
        assertEquals(32.0, current.feelsLike.celsius, 0.01)
        assertEquals(20, current.humidity.value)
        assertEquals(0, current.cloudCover.value)
        assertEquals(1.4, current.wind.speed.kilometresPerHour, 0.01)
        assertEquals(16.2, current.wind.gust?.kilometresPerHour ?: 0.0, 0.01)
        assertEquals(1017.6, current.pressure.seaLevel.hectopascals, 0.01)
        assertEquals(1004.8, current.pressure.surface.hectopascals, 0.01)
    }

    @Test
    fun `the remark follows from the condition and the time of day`() {
        assertEquals(Condition.Clear, forecast.current.condition)
        assertEquals(PartOfDay.Day, forecast.current.partOfDay)
        assertEquals(Remark.ClearAndBright, forecast.current.remark)
    }

    @Test
    fun `a week of days arrives, today first`() {
        assertEquals(7, forecast.days.size)
        assertEquals(LocalDate.of(2026, 8, 9), forecast.days.first().date)

        val today = forecast.days.first()
        assertEquals(21.8, today.minimum.celsius, 0.01)
        assertEquals(32.6, today.maximum.celsius, 0.01)
        assertEquals(0, today.precipitationChance.value)
        assertNotNull(today.sunrise)
        assertEquals(ZoneId.of("Europe/Budapest"), today.sunrise.zone)
    }

    @Test
    fun `every hour is filed under the day it belongs to`() {
        assertEquals(
            "168 hourly readings over seven days",
            168,
            forecast.days.sumOf { it.hours.size },
        )
        forecast.days.forEach { day ->
            assertEquals("${day.date} should hold 24 hours", 24, day.hours.size)
            assertTrue(
                "every hour under ${day.date} should belong to it",
                day.hours.all { it.time.toLocalDate() == day.date },
            )
        }
    }

    @Test
    fun `hours carry their own part of day`() {
        val hours = forecast.days.first().hours
        assertEquals(PartOfDay.Night, hours.first { it.time.hour == 3 }.partOfDay)
        assertEquals(PartOfDay.Day, hours.first { it.time.hour == 14 }.partOfDay)
    }

    /** The mapper feeds the domain's own queries; this is the strip the dashboard will draw. */
    @Test
    fun `the mapped forecast answers the queries the screens ask`() {
        val evening = ZonedDateTime.of(2026, 8, 9, 22, 30, 0, 0, ZoneId.of("Europe/Budapest"))

        val strip = forecast.hoursFrom(evening, count = 4)

        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 9) to 22,
                LocalDate.of(2026, 8, 9) to 23,
                LocalDate.of(2026, 8, 10) to 0,
                LocalDate.of(2026, 8, 10) to 1,
            ),
            strip.map { it.time.toLocalDate() to it.time.hour },
        )
    }
}
