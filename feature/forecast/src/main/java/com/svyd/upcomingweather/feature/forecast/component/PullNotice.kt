package com.svyd.upcomingweather.feature.forecast.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.R

/**
 * The line uncovered when the page is dragged down.
 *
 * It sits behind the page rather than over it, so pulling reads as lifting the sheet off the platen
 * to see what is written underneath. What it says changes at the point where letting go would
 * actually ask for a forecast, which is the only thing the reader needs to know mid-gesture.
 */
@Composable
fun PullNotice(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    val pulled = fraction.coerceAtLeast(0f)
    if (pulled <= 0f) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NoticeHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(
                if (pulled >= 1f) R.string.forecast_pull_release else R.string.forecast_pull_hint,
            ),
            style = NoirTheme.type.sectionStamp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@NoirPreviews
@Composable
private fun PullNoticePreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            PullNotice(fraction = 0.5f)
            PullNotice(fraction = 1f)
        }
    }
}

/** Matches the distance the page travels, so the line is exactly uncovered at the threshold. */
internal val NoticeHeight = 56.dp
