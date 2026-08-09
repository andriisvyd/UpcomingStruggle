package com.svyd.upcomingweather.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class ForecastQueriesTest {

    @Test
    fun `the day in progress is the one where the weather is`() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        val forecast = forecast(zone = tokyo, from = LocalDate.of(2026, 8, 8), days = 3)

        // 23:00 on the 8th in Budapest is already 06:00 on the 9th in Tokyo.
        val budapestEvening = ZonedDateTime.of(
            2026, 8, 8, 23, 0, 0, 0, ZoneId.of("Europe/Budapest"),
        )

        assertEquals(LocalDate.of(2026, 8, 9), forecast.today(budapestEvening)?.date)
    }

    @Test
    fun `the hour in progress is the first hour reported`() {
        val zone = ZoneId.of("Europe/Budapest")
        val forecast = forecast(zone = zone, from = LocalDate.of(2026, 8, 8), days = 2)

        val hours = forecast.hoursFrom(
            moment = ZonedDateTime.of(2026, 8, 8, 14, 20, 0, 0, zone),
            count = 3,
        )

        assertEquals(
            listOf(14, 15, 16),
            hours.map { it.time.hour },
        )
    }

    @Test
    fun `hours run past midnight into the following day`() {
        val zone = ZoneId.of("Europe/Budapest")
        val forecast = forecast(zone = zone, from = LocalDate.of(2026, 8, 8), days = 2)

        val hours = forecast.hoursFrom(
            moment = ZonedDateTime.of(2026, 8, 8, 22, 30, 0, 0, zone),
            count = 4,
        )

        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 8) to 22,
                LocalDate.of(2026, 8, 8) to 23,
                LocalDate.of(2026, 8, 9) to 0,
                LocalDate.of(2026, 8, 9) to 1,
            ),
            hours.map { it.time.toLocalDate() to it.time.hour },
        )
    }

    @Test
    fun `a moment from another zone is read where the weather is`() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        val forecast = forecast(zone = tokyo, from = LocalDate.of(2026, 8, 8), days = 2)

        // 00:30 UTC on the 8th is 09:30 in Tokyo.
        val utcMorning = ZonedDateTime.of(2026, 8, 8, 0, 30, 0, 0, ZoneId.of("UTC"))
        val hours = forecast.hoursFrom(moment = utcMorning, count = 2)

        assertEquals(listOf(9, 10), hours.map { it.time.hour })
    }

    @Test
    fun `asking for more hours than remain returns what there is`() {
        val zone = ZoneId.of("Europe/Budapest")
        val forecast = forecast(zone = zone, from = LocalDate.of(2026, 8, 8), days = 1)

        val hours = forecast.hoursFrom(
            moment = ZonedDateTime.of(2026, 8, 8, 21, 0, 0, 0, zone),
            count = 8,
        )

        assertEquals(3, hours.size)
        assertEquals(listOf(21, 22, 23), hours.map { it.time.hour })
    }

    @Test
    fun `a moment past the forecast leaves nothing to report`() {
        val zone = ZoneId.of("Europe/Budapest")
        val forecast = forecast(zone = zone, from = LocalDate.of(2026, 8, 8), days = 1)

        val hours = forecast.hoursFrom(
            moment = ZonedDateTime.of(2026, 8, 20, 9, 0, 0, 0, zone),
            count = 4,
        )

        assertTrue(hours.isEmpty())
    }

    @Test
    fun `no hours are asked for and none are given`() {
        val zone = ZoneId.of("Europe/Budapest")
        val forecast = forecast(zone = zone, from = LocalDate.of(2026, 8, 8), days = 2)

        val hours = forecast.hoursFrom(
            moment = ZonedDateTime.of(2026, 8, 8, 9, 0, 0, 0, zone),
            count = 0,
        )

        assertTrue(hours.isEmpty())
    }

    @Test
    fun `a date the forecast does not reach has no day`() {
        val zone = ZoneId.of("Europe/Budapest")
        val forecast = forecast(zone = zone, from = LocalDate.of(2026, 8, 8), days = 2)

        assertNull(forecast.day(LocalDate.of(2026, 8, 30)))
    }
}
