package com.svyd.upcomingweather.feature.search.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.icon.NoirIcons
import com.svyd.upcomingweather.core.designsystem.primitive.NoirListRow
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.feature.search.R
import com.svyd.upcomingweather.feature.search.model.CityUi

/** Always the first row, always in primary: hand the job to the phone's own location. */
@Composable
fun LocationRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NoirListRow(
        headline = stringResource(R.string.search_location_action),
        headlineColor = MaterialTheme.colorScheme.primary,
        headlineStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        leading = {
            Icon(
                imageVector = NoirIcons.MyLocation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(RowIconSize),
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
    isRecent: Boolean = false,
) {
    NoirListRow(
        headline = city.name,
        supporting = city.subtitle,
        leading = if (isRecent) {
            {
                Icon(
                    imageVector = NoirIcons.Recent,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(RowIconSize),
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
        modifier = modifier.padding(start = HeaderInset, top = NoirSpacing.gutter, bottom = NoirSpacing.xs),
    )
}

private val RowIconSize = 24.dp
private val HeaderInset = 4.dp
