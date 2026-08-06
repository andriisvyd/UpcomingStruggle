package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Tokens for [NoirBlinkingCursor] and [NoirPlaceholderDefaults]. */
object PlaceholderDefaults {
    /** On for this long, off for this long, hard steps — no fade. */
    const val BLINK_MILLIS = 530

    /** Sized in sp: the cursor stands in for a character, so it grows with the type. */
    val CursorWidth = 9.sp
    val CursorHeight = 15.sp

    /** Faded back far enough to read as what is still coming rather than as content. */
    const val PLACEHOLDER_ALPHA = 0.38f
}

/**
 * The block cursor that marks the frontier — the slot where the next real value will land.
 *
 * Hard steps, no fade.
 */
@Composable
fun NoirBlinkingCursor(
    modifier: Modifier = Modifier,
    width: TextUnit = PlaceholderDefaults.CursorWidth,
    height: TextUnit = PlaceholderDefaults.CursorHeight,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val size = with(LocalDensity.current) { DpSize(width.toDp(), height.toDp()) }

    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha = transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            // Two holds and a crossing: the value stays at 1 through the first half, then the
            // one-millisecond gap between the second and third keyframes steps it to 0 without
            // an interpolated fade.
            animation = keyframes {
                durationMillis = PlaceholderDefaults.BLINK_MILLIS * 2
                1f at 0
                1f at PlaceholderDefaults.BLINK_MILLIS - 1
                0f at PlaceholderDefaults.BLINK_MILLIS
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "cursorAlpha",
    ).value
    Box(
        modifier
            .size(size)
            .alpha(alpha)
            .background(color),
    )
}

/**
 * A line of the page that has not been typed yet — dot leaders and dashes, faded back so it reads
 * as what is still coming rather than as content.
 */
@Composable
fun NoirPlaceholderDefaults(
    text: String,
    modifier: Modifier = Modifier,
    alpha: Float = PlaceholderDefaults.PLACEHOLDER_ALPHA,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        maxLines = 1,
        softWrap = false,
        modifier = modifier,
    )
}
