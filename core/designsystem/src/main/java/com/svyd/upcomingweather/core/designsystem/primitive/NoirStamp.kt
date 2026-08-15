package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.theme.NoirStroke
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme

/**
 * A word rubber-stamped onto the page: uppercase, boxed in its own ink, off-square by [StampTilt] degrees.
 *
 * Carries no meaning of its own — the caller decides what the word and the [ink] are.
 */
@Composable
fun NoirTiltedStamp(
    text: String,
    ink: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = NoirTheme.type.conditionStamp,
        color = ink,
        modifier = modifier
            .rotate(StampTilt)
            .border(
                width = BorderWidth,
                color = ink,
                shape = MaterialTheme.shapes.small,
            )
            .padding(
                horizontal = HorizontalPadding,
                vertical = VerticalPadding,
            ),
    )
}

/** A section heading, typed like a report subhead. */
@Composable
fun NoirSectionStamp(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text.uppercase(),
        style = NoirTheme.type.sectionStamp,
        color = color,
        modifier = modifier,
    )
}

/** Degrees off square — a stamp pressed by hand is never straight. */
private const val StampTilt = -2f
private val BorderWidth = NoirStroke.stamp
private val HorizontalPadding = 8.dp
private val VerticalPadding = 4.dp
