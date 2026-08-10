package com.svyd.upcomingweather.feature.forecast.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.icon.NoirIcons
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPanel
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSecondaryAction
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.R
import com.svyd.upcomingweather.feature.forecast.model.OfflineUi

/**
 * Shown when the refresh failed but the cache still renders. The content underneath stays fully
 * interactive — this is a notice, not a blocker.
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    offline: OfflineUi,
    onRetry: () -> Unit,
) {
    NoirPanel(modifier) {
        Icon(
            imageVector = NoirIcons.CloudOff,
            contentDescription = stringResource(R.string.forecast_cd_offline),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(IconSize),
        )
        Text(
            text = offline.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        NoirSecondaryAction(text = stringResource(R.string.forecast_retry_action), onClick = onRetry)
    }
}

@NoirPreviews
@Composable
private fun OfflineBannerPreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            OfflineBanner(
                offline = OfflineUi("Offline — showing what was kept"),
                onRetry = {},
                modifier = Modifier.padding(NoirSpacing.gutter),
            )
        }
    }
}

private val IconSize = 20.dp
