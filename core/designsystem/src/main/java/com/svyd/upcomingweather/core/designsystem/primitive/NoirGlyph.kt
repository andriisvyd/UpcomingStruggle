package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
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

@Composable
fun NoirGlyph(
    modifier: Modifier = Modifier,
    glyph: NoirTypedIcon,
    onClick: (() -> Unit)? = null,
    tint: Color = Color.Unspecified,
    trim: Boolean = false,
    pressedTint: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = NoirTheme.type.glyphHour,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val clickable = onClick != null
    Text(
        modifier = if (clickable) modifier.then(
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
        ) else modifier,
        text = if (trim) glyph.mark.trim() else glyph.mark,
        style = style,
        color = if (pressed && onClick != null) pressedTint else tint,
        // A three-character mark is wider than the slot it sits in; it spills into the gap
        // rather than wrapping, so surrounding cells stay on their columns.
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
    )
}

/**
 * A condition drawn as its character in its own ink.
 *
 * Decorative: the literal condition is carried by the stamp beside it, so this is silenced for
 * screen readers by the row that merges it.
 */
@Composable
fun NoirConditionGlyph(
    modifier: Modifier = Modifier,
    condition: NoirCondition,
    style: TextStyle = NoirTheme.type.glyphDay,
    trim: Boolean = false,
    color: Color = condition.ink(),
) {
    NoirGlyph(
        modifier = modifier,
        glyph = NoirTypedIcon.Condition(condition),
        trim = trim,
        style = style,
        tint = color
    )
}
