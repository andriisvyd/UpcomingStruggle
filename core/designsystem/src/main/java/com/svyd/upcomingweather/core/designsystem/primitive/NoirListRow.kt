package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.clickable
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
    headlineStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    minHeight: Dp = NoirListRowDefaults.MinHeight,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .heightIn(min = minHeight)
            .padding(NoirListRowDefaults.ContentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(NoirListRowDefaults.LeadingGap))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = headline,
                style = headlineStyle,
                color = headlineColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = NoirListRowDefaults.SupportingSpacing),
                )
            }
        }
    }
}

/** Tokens for [NoirListRow]. */
object NoirListRowDefaults {
    /** A minimum, not a height — a row with a second line is taller. */
    val MinHeight = 60.dp
    val ContentPadding = 4.dp

    /** Gap between the leading slot and the headline. */
    val LeadingGap = 14.dp
    val SupportingSpacing = 1.dp
}
