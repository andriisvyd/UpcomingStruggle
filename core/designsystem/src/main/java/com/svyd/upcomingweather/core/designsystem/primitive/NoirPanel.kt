package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirStroke

/**
 * The one filled container in the app: 4 dp radius, 1 dp stroke, no elevation.
 *
 * Everything else sits bare on the background; a panel means "this is a separate object on the
 * page" — a field to type in, a notice about the connection.
 */
@Composable
fun NoirPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = NoirPanelDefaults.ContentPadding,
    horizontalArrangement: Arrangement.Horizontal =
        Arrangement.spacedBy(NoirPanelDefaults.ContentGap),
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = NoirStroke.hairline,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.small,
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}

/** Tokens for [NoirPanel]. */
object NoirPanelDefaults {
    val ContentPadding = PaddingValues(horizontal = NoirSpacing.m, vertical = 8.dp)

    /** Gap between the panel's icon, its text and its action. */
    val ContentGap = 8.dp
}
