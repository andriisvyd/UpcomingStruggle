package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.domain.model.Condition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodesTest {

    @Test
    fun `the codes collapse onto the conditions the app draws`() {
        val expected = mapOf(
            Condition.Clear to listOf(0),
            Condition.PartlyCloudy to listOf(1, 2),
            Condition.Overcast to listOf(3),
            Condition.Fog to listOf(45, 48),
            Condition.Drizzle to listOf(51, 53, 55, 56, 57),
            Condition.Rain to listOf(61, 63, 65, 66, 67, 80, 81, 82),
            Condition.Snow to listOf(71, 73, 75, 77, 85, 86),
            Condition.Thunderstorm to listOf(95, 96, 99),
        )

        expected.forEach { (condition, codes) ->
            codes.forEach { code ->
                assertEquals("code $code", condition, conditionOf(code))
            }
        }
    }

    /** Claiming clear skies is the one wrong answer that is certain to look wrong. */
    @Test
    fun `a code nobody recognises is not reported as clear`() {
        listOf(-1, 4, 30, 100, 999).forEach { code ->
            assertEquals("code $code", Condition.Overcast, conditionOf(code))
        }
    }
}
