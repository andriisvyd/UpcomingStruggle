package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme

/**
 * The shared scaffold for a screen that has nothing to show: a typed mark, a headline, a line of
 * explanation, and whatever the caller puts in [actions].
 */
@Composable
fun NoirEmptyStateMessage(
    glyph: NoirTypedIcon,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // At 200% type the message and its two actions are taller than the screen, so the
            // scaffold scrolls rather than pushing the secondary action out of reach.
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = HorizontalPadding,
                vertical = VerticalPadding,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NoirSpacing.s),
    ) {
        NoirGlyph(
            glyph = glyph,
            style = NoirTheme.type.glyphHero.copy(
                fontSize = GlyphSize,
                lineHeight = GlyphSize,
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = NoirSpacing.xs),
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        actions()
    }
}

private val HorizontalPadding = 28.dp
/** Holds the message off the top of the screen; the column scrolls when type is large. */
private val VerticalPadding = 84.dp
private val GlyphSize = 40.sp
