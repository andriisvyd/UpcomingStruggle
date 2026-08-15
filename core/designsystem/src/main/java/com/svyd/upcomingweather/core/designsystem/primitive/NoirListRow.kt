package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One line of a list: an optional leading slot, a headline, an optional second line.
 *
 * A row, not a card — there is nothing under it but the page.
 */
@Composable
fun NoirListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: @Composable (() -> Unit)? = null,
    headlineColor: Color = MaterialTheme.colorScheme.onSurface,
    pressedTint: Color = MaterialTheme.colorScheme.primary,
    supportingPressedTint: Color = MaterialTheme.colorScheme.primary,
    headlineStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    minHeight: Dp = NoirListRowDefaults.MinHeight,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (() -> Unit)? = null,
) {
    val pressed by interactionSource.collectIsPressedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null)
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        role = Role.Button,
                        onClick = onClick,
                        indication = null,
                    )
                else
                    Modifier
            )
            .heightIn(min = minHeight)
            .padding(ContentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(LeadingGap))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = headline,
                style = headlineStyle,
                color = if (pressed) pressedTint else headlineColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (pressed) supportingPressedTint else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = SupportingSpacing),
                )
            }
        }
    }
}

/** Tokens for [NoirListRow]. */
object NoirListRowDefaults {
    /** A minimum, not a height — a row with a second line is taller. */
    val MinHeight = 60.dp
}

/** Gap between the leading slot and the headline. */
private val LeadingGap = 16.dp
private val SupportingSpacing = 4.dp
private val ContentPadding = 4.dp
