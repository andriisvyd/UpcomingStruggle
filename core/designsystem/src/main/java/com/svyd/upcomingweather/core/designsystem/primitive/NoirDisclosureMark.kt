package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection

/**
 * The typed arrow at the end of a row that opens something.
 *
 * Mirrors with the layout direction, so a row reads as leading somewhere in both directions.
 * Decorative: the row that contains it announces the action.
 */
@Composable
fun NoirDisclosureMark(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val mark = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> Forward
        LayoutDirection.Rtl -> Backward
    }
    Text(text = mark, style = style, color = color, modifier = modifier)
}

private const val Forward = ">"
private const val Backward = "<"
