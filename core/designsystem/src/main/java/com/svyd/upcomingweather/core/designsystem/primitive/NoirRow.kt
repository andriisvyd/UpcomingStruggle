package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme

/**
 * `LABEL ......... value`, with an optional second line indented under it.
 *
 * No container, no background: the row is typed straight onto whatever it sits on.
 */
@Composable
fun NoirDotLeaderRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = label.uppercase(),
                style = NoirTheme.type.readingLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline(),
            )
            DotLeader(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = Clearance,
                        // Drops the rule from the middle of the row onto the shared baseline.
                        vertical = BaselineDrop,
                    ),
            )
            Text(
                text = value,
                style = NoirTheme.type.readingValue,
                color = valueColor,
                modifier = Modifier.alignByBaseline(),
            )
        }
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = DetailIndent,
                    top = DetailSpacing,
                ),
            )
        }
    }
}

/** The dotted rule between a label and its value. */
@Composable
private fun DotLeader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
        .copy(alpha = DotAlpha),
) {
    Canvas(modifier.height(DotDiameter)) {
        val radius = DotDiameter.toPx() / 2f
        val step = DotSpacing.toPx()
        val y = size.height / 2f
        var x = radius
        while (x <= size.width - radius) {
            drawCircle(color = color, radius = radius, center = Offset(x, y))
            x += step
        }
    }
}

private val DotDiameter = 2.dp
private val DotSpacing = 4.dp
private val DotAlpha = 0.55f
/** Clear space between the rule and the label or value either side of it. */
private val Clearance = 8.dp
private val BaselineDrop = 4.dp
/** The commentary line sits under the label, indented. */
private val DetailIndent = 16.dp
private val DetailSpacing = 4.dp
