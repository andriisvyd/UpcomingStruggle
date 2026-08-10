package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme

/**
 * The loud action: UPPERCASE, amber, no fill and no border. Hierarchy is carried by case and
 * color, which is why there is no Button anywhere in this app.
 */
@Composable
fun NoirPrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = MinHeight)
            .padding(horizontal = NoirSpacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = NoirTheme.type.actionPrimary,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** The quiet one: lowercase, onSurfaceVariant. */
@Composable
fun NoirSecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = MinHeight)
            .padding(horizontal = NoirSpacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.lowercase(),
            style = NoirTheme.type.actionSecondary,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 48 dp target around a 24 dp glyph.
 *
 * No ripple: a spreading circle is a Material gesture, and this app has no filled surfaces for
 * one to spread across. The glyph takes the interactive ink while it is held instead — the same
 * amber that marks every other interactive element.
 */
@Composable
fun NoirIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    pressedTint: Color = MaterialTheme.colorScheme.primary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .size(NoirSpacing.touchTarget)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (pressed) pressedTint else tint,
            modifier = Modifier.size(IconSize),
        )
    }
}

/** Text actions carry no fill, so this is the target rather than a visible button. */
private val MinHeight = 44.dp
/** Glyphs are drawn at 24 dp inside a 48 dp target. */
private val IconSize = 24.dp
