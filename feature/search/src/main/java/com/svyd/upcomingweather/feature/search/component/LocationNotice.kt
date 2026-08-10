package com.svyd.upcomingweather.feature.search.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSecondaryAction
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.search.R
import com.svyd.upcomingweather.feature.search.model.LocationNoticeUi

/**
 * Says why letting the phone name the city did not work, and offers the one thing that can change
 * that.
 *
 * While the platform will still offer the prompt, asking again is the way through; once it stops,
 * only settings can reverse the refusal. A device that simply has no position is asked again.
 */
@Composable
fun LocationNotice(
    notice: LocationNoticeUi,
    onAskAgain: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settlesInSettings = notice is LocationNoticeUi.Refused && !notice.canAskAgain

    Column(modifier.padding(top = NoirSpacing.s, bottom = NoirSpacing.s)) {
        Text(
            text = stringResource(notice.message()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        NoirSecondaryAction(
            text = stringResource(
                if (settlesInSettings) {
                    R.string.search_location_settings_action
                } else {
                    R.string.search_location_again_action
                },
            ),
            onClick = if (settlesInSettings) onOpenSettings else onAskAgain,
        )
    }
}

private fun LocationNoticeUi.message(): Int = when (this) {
    is LocationNoticeUi.Refused ->
        if (canAskAgain) R.string.search_location_refused_again else R.string.search_location_refused

    LocationNoticeUi.Unavailable -> R.string.search_location_unavailable
}

@NoirPreviews
@Composable
private fun LocationNoticePreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            Column(Modifier.padding(NoirSpacing.gutter)) {
                LocationNotice(
                    notice = LocationNoticeUi.Refused(canAskAgain = true),
                    onAskAgain = {},
                    onOpenSettings = {},
                )
                LocationNotice(
                    notice = LocationNoticeUi.Refused(canAskAgain = false),
                    onAskAgain = {},
                    onOpenSettings = {},
                )
                LocationNotice(
                    notice = LocationNoticeUi.Unavailable,
                    onAskAgain = {},
                    onOpenSettings = {},
                )
            }
        }
    }
}
