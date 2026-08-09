package com.svyd.upcomingweather.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RemarkTest {

    @Test
    fun `clear skies read differently by day and by night`() {
        assertEquals(Remark.ClearAndBright, remarkFor(Condition.Clear, PartOfDay.Day))
        assertEquals(Remark.ClearAndStill, remarkFor(Condition.Clear, PartOfDay.Night))
    }

    @Test
    fun `every other condition reads the same whoever is awake`() {
        val unaffected = Condition.entries - Condition.Clear
        unaffected.forEach { condition ->
            assertEquals(
                "$condition should not depend on the time of day",
                remarkFor(condition, PartOfDay.Day),
                remarkFor(condition, PartOfDay.Night),
            )
        }
    }

    @Test
    fun `no two conditions share a verdict`() {
        val byDay = Condition.entries.map { remarkFor(it, PartOfDay.Day) }
        assertEquals(
            "each condition earns its own remark",
            Condition.entries.size,
            byDay.toSet().size,
        )
    }

    @Test
    fun `rain and drizzle are told apart`() {
        assertNotEquals(
            remarkFor(Condition.Rain, PartOfDay.Day),
            remarkFor(Condition.Drizzle, PartOfDay.Day),
        )
    }
}
