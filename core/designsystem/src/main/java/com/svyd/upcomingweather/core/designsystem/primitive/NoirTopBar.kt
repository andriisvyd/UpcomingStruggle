package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme

/**
 * 64 dp of bar with a centered middle and a 48 dp slot on each side. No fill, no elevation — it is
 * the same page the content is typed on.
 *
 * The middle is a slot rather than a string so that a screen can put a search field there instead
 * of a title.
 */
@Composable
fun NoirTopBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = NoirTopBarDefaults.windowInsets,
    navigation: @Composable () -> Unit = { Spacer(Modifier.size(NoirSpacing.touchTarget)) },
    actions: @Composable RowScope.() -> Unit = { Spacer(Modifier.size(NoirSpacing.touchTarget)) },
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // The bar clears the status bar itself, so no screen has to think about it.
            .windowInsetsPadding(windowInsets)
            // A minimum, not a height: the status line has to be able to grow with the type.
            .heightIn(min = MinHeight)
            .padding(
                horizontal = HorizontalPadding,
                vertical = VerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigation()
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
        actions()
    }
}

/**
 * The status line in the middle of the bar.
 *
 * Not typed: the same place is reported on in more than one way — a status while a fetch runs, the
 * temperature once the hero scrolls off — and a line that retyped at each of those would be the
 * loudest thing on a page whose own lines had settled.
 */
@Composable
fun NoirTopBarTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = NoirTheme.type.cityTitle,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Tokens for [NoirTopBar]. */
object NoirTopBarDefaults {
    /** The status bar, and any cutout at the sides — what the bar has to clear. */
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
}

/** A minimum, not a height — the status line has to be able to grow with the type. */
private val MinHeight = NoirSpacing.appBarHeight

/** The 48 dp icon slots supply the rest of the gutter. */
private val HorizontalPadding = 4.dp
private val VerticalPadding = 4.dp
