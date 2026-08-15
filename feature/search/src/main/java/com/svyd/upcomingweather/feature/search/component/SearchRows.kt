package com.svyd.upcomingweather.feature.search.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.primitive.NoirGlyph
import com.svyd.upcomingweather.core.designsystem.primitive.NoirListRow
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTypedIcon
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.feature.search.R
import com.svyd.upcomingweather.feature.search.model.CityUi

/** Always the first row, always in primary: hand the job to the phone's own location. */
@Composable
fun LocationRow(
    modifier: Modifier = Modifier,
    pressedTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    NoirListRow(
        headline = stringResource(R.string.search_location_action),
        headlineColor = MaterialTheme.colorScheme.primary,
        headlineStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        pressedTint = pressedTint,
        interactionSource = interactionSource,
        leading = {
            NoirGlyph(
                glyph = NoirTypedIcon.Gps,
                tint = if (pressed) pressedTint else MaterialTheme.colorScheme.primary,
            )
        },
        onClick = onClick,
        modifier = modifier,
    )
}

/** One city. Recents carry a clock; live results carry nothing. */
@Composable
fun CityRow(
    city: CityUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pressedTint: Color = MaterialTheme.colorScheme.primary,
    isRecent: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    NoirListRow(
        headline = city.name,
        supporting = city.subtitle,
        interactionSource = interactionSource,
        leading = if (isRecent) {
            {
                NoirGlyph(
                    glyph = NoirTypedIcon.Clock,
                    tint = if (pressed) pressedTint else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            null
        },
        onClick = onClick,
        modifier = modifier.padding(start = if (isRecent) 0.dp else NoirSpacing.s),
    )
}

/** The "Cold cases" header above stored rows. */
@Composable
fun RecentsHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.search_recents_header).uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            start = HeaderInset,
            top = NoirSpacing.gutter,
            bottom = NoirSpacing.xs
        ),
    )
}

private val RowIconSize = 24.dp
private val HeaderInset = 4.dp
