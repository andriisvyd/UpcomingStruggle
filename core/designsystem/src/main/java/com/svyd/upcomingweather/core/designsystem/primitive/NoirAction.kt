package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier,
    text: String,
    pressedTint: Color = MaterialTheme.colorScheme.secondary,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                role = Role.Button,
                onClick = onClick,
                indication = null,
            )
            .heightIn(min = MinHeight)
            .padding(horizontal = NoirSpacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = NoirTheme.type.actionPrimary,
            color = if (pressed) pressedTint else MaterialTheme.colorScheme.primary,
        )
    }
}

/** The quiet one: lowercase, onSurfaceVariant. */
@Composable
fun NoirSecondaryAction(
    modifier: Modifier = Modifier,
    text: String,
    pressedTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                role = Role.Button,
                onClick = onClick,
                indication = null,
            )
            .heightIn(min = MinHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.lowercase(),
            style = NoirTheme.type.actionSecondary,
            color = if (pressed) pressedTint else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Text actions carry no fill, so this is the target rather than a visible button. */
private val MinHeight = 44.dp

/** Glyphs are drawn at 24 dp inside a 48 dp target. */
private val IconSize = 24.dp
