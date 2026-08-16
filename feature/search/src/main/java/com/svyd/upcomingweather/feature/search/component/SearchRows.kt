package com.svyd.upcomingweather.feature.search.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                modifier = Modifier
                    .size(NoirSpacing.touchTarget),
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
    modifier: Modifier = Modifier,
    city: CityUi,
    onClick: () -> Unit,
    pressedTint: Color = MaterialTheme.colorScheme.primary,
    isRecent: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    NoirListRow(
        modifier = modifier,
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
    )
}

/** The "Cold cases" header above stored rows. */
@Composable
fun RecentsHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.search_recents_header).uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .padding(top = NoirSpacing.gutter)
            .padding(horizontal = NoirSpacing.gutter),
    )
}
