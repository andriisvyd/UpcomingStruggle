package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme

/**
 * The typed weather set: nine Courier characters, each with the ink it is drawn in.
 *
 * Mark and ink travel together so they cannot drift apart. Callers translate their own condition
 * type into one of these; nothing here reaches back into a domain or feature module.
 */
enum class NoirCondition(val mark: String) {
    ClearDay(" * "),
    ClearNight(" ) "),
    Partly("(*)"),
    Overcast("(~)"),
    Fog("==="),
    Drizzle("// "),
    Rain("///"),
    Snow("***"),
    Thunder("/!\\"),
}

@Composable
@ReadOnlyComposable
fun NoirCondition.ink(): Color = when (this) {
    NoirCondition.ClearDay -> NoirTheme.inks.sodium
    NoirCondition.ClearNight -> NoirTheme.inks.moonSteel
    NoirCondition.Partly -> NoirTheme.inks.greySage
    NoirCondition.Overcast -> NoirTheme.inks.graphite
    NoirCondition.Fog -> NoirTheme.inks.warmPutty
    NoirCondition.Drizzle, NoirCondition.Rain -> NoirTheme.inks.steel
    NoirCondition.Snow -> NoirTheme.inks.tealIce
    NoirCondition.Thunder -> NoirTheme.inks.rust
}

/**
 * A condition drawn as its character in its own ink.
 *
 * Decorative: the literal condition is carried by the stamp beside it, so this is silenced for
 * screen readers by the row that merges it.
 */
@Composable
fun NoirConditionGlyph(
    condition: NoirCondition,
    modifier: Modifier = Modifier,
    style: TextStyle = NoirTheme.type.glyphDay,
    color: Color = condition.ink(),
) {
    Text(
        text = condition.mark,
        style = style,
        color = color,
        // A three-character mark is wider than the slot it sits in; it spills into the gap
        // rather than wrapping, so surrounding cells stay on their columns.
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        modifier = modifier,
    )
}
