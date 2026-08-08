package com.svyd.upcomingweather.feature.forecast.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirDotLeaderRow
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.model.ReadingUi

/** The readings, typed as a ledger: label, dot leader, value, and a line of commentary under it. */
@Composable
fun ReadingLedger(
    readings: List<ReadingUi>,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        readings.forEach { reading ->
            NoirDotLeaderRow(
                label = reading.label,
                value = reading.value,
                detail = reading.detail,
                modifier = Modifier.padding(top = ReadingGap),
            )
        }
    }
}

@NoirPreviews
@Composable
private fun ReadingLedgerPreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            ReadingLedger(
                readings = listOf(
                    ReadingUi("Humidity", "46%", "clouds packing 40%"),
                    ReadingUi("Wind", "12 km/h", "gusts of 26, no warning"),
                ),
                modifier = Modifier.padding(horizontal = NoirSpacing.gutter),
            )
        }
    }
}

private val ReadingGap = 12.dp
