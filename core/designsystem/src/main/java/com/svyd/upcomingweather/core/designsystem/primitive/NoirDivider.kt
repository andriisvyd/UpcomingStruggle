package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.svyd.upcomingweather.core.designsystem.theme.NoirStroke
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme

/** The 1 dp hairline that separates rows. The only rule this app draws. */
@Composable
fun NoirHairlineDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = NoirStroke.hairline,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/** A vertical divider typed as a stack of pipes, for gaps between columns. */
@Composable
fun NoirPipeDivider(
    modifier: Modifier = Modifier,
    pipes: Int = NoirDividerDefaults.PIPES,
) {
    Text(
        text = List(pipes) { PIPE }.joinToString("\n"),
        style = NoirTheme.type.divider,
        color = MaterialTheme.colorScheme.onSurfaceVariant
            .copy(alpha = NoirDividerDefaults.ALPHA),
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

/** The mark the divider is typed from — a design token, not copy: it is never translated. */
private const val PIPE = "|"

/** Tokens for [NoirPipeDivider]. */
object NoirDividerDefaults {
    /** Four marks run to roughly the height of an hour column. */
    const val PIPES = 4
    const val ALPHA = 0.75f
}
