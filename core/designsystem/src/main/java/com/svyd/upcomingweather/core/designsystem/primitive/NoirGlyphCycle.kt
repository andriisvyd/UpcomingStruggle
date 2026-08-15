package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.svyd.upcomingweather.core.designsystem.foundation.animationsEnabled
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * The condition set read out one mark at a time, each in its own ink.
 *
 * The marks are three cells of the same monospaced face, so they replace each other in place rather
 * than resizing the slot they sit in. A blank of the full three cells is drawn under them all the
 * same: a mark that ends in a space measures narrower than one that does not, which would nudge
 * whatever the cycle is centred against.
 *
 * With animations off, and in previews, the first mark is shown and nothing turns over.
 */
@Composable
fun NoirGlyphCycle(
    modifier: Modifier = Modifier,
    conditions: List<NoirCondition> = NoirCondition.entries,
    beat: Duration = Beat,
    style: TextStyle = NoirTheme.type.glyphHero,
) {
    val cycling = animationsEnabled()
    var index by remember(conditions) { mutableIntStateOf(0) }

    LaunchedEffect(conditions, beat, cycling) {
        if (!cycling) return@LaunchedEffect
        while (true) {
            delay(beat)
            index = (index + 1) % conditions.size
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        NoirConditionGlyph(condition = Sizer, style = style, color = Color.Transparent)
        NoirConditionGlyph(condition = conditions[index], style = style)
    }
}

/** The one mark with no space in it, and so the width every other one is held to. */
private val Sizer = NoirCondition.Rain

/**
 * Fast enough to read as the set flickering past, slow enough that each mark registers as itself.
 *
 * Nine marks at this beat come to just under a second, which is the shortest a launch is held.
 */
private val Beat: Duration = 110.milliseconds
