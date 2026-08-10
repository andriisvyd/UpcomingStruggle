package com.svyd.upcomingweather.feature.forecast.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.preview.NoirScreenPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirBlinkingCursor
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSectionStamp
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPlaceholder
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.R

/**
 * First load with nothing cached: the page types itself in.
 *
 * The cursor sits in the temperature slot — the frontier, where the first real value lands — and
 * everything below it is a faded placeholder. Nothing shimmers. With a cache this never appears.
 */
@Composable
fun ForecastSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NoirSpacing.gutter)
            .clearAndSetSemantics { },
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(start = PageInset, top = CursorTop),
        ) {
            NoirBlinkingCursor(width = 44.sp, height = 54.sp)
            Text(
                text = "°",
                style = NoirTheme.type.tempDisplay,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
        NoirPlaceholder("[ ------------ ]", Modifier.padding(top = StampGap), alpha = 0.4f)
        NoirPlaceholder("......................", Modifier.padding(top = LineGap), alpha = 0.28f)
        NoirPlaceholder(".............", Modifier.padding(top = WideLineGap), alpha = 0.28f)

        NoirSectionStamp(
            text = stringResource(R.string.forecast_hours_header),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = NoirSpacing.section, bottom = NoirSpacing.m),
        )
        NoirPlaceholder("--:--  |  --:--  |  --:--  |  --:--")
        NoirPlaceholder(" --°   |   --°   |   --°   |   --°", Modifier.padding(top = TightLineGap))

        Column(Modifier.padding(top = LedgerGap)) {
            listOf("HUMIDITY", "WIND", "SUNRISE", "PRESSURE").forEach { label ->
                NoirPlaceholder(
                    text = label.padEnd(32, '.') + " --",
                    modifier = Modifier.padding(top = ReadingGap),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        NoirSectionStamp(
            text = stringResource(R.string.forecast_days_header),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = NoirSpacing.section, bottom = NoirSpacing.s),
        )
        repeat(5) {
            NoirPlaceholder("---  ....................", Modifier.padding(top = DayGap))
        }
    }
}

@NoirScreenPreviews
@Composable
private fun ForecastSkeletonPreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            ForecastSkeleton()
        }
    }
}

private val PageInset = NoirSpacing.xs
private val CursorTop = 12.dp
private val StampGap = 16.dp
private val LineGap = NoirSpacing.m
private val WideLineGap = 16.dp
private val TightLineGap = NoirSpacing.s
private val LedgerGap = 24.dp
private val ReadingGap = 16.dp
private val DayGap = 8.dp
