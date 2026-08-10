package com.svyd.upcomingweather.feature.forecast

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A beat, so that a screen left open notices time passing.
 *
 * Nothing else emits once a fetch has settled, and how current a forecast is is the one thing on
 * screen that goes wrong by standing still.
 */
internal fun ticks(period: Duration = TICK): Flow<Unit> = flow {
    while (true) {
        emit(value = Unit)
        delay(duration = period.toMillis().milliseconds)
    }
}

private val TICK: Duration = Duration.ofSeconds(30)
