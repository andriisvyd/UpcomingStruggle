package com.svyd.upcomingweather.feature.forecast.component

import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.designsystem.primitive.NoirConditionGlyph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.scaledByFont
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPipeDivider
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.model.HourUi

/**
 * The 3-hour strip: bare columns split by typed pipes, bleeding to both screen edges.
 *
 * The divider lives in the gap rather than between items so the first column starts flush with
 * the gutter.
 */
@Composable
fun HourStrip(
    modifier: Modifier = Modifier,
    hours: List<HourUi>,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = NoirSpacing.gutter),
    ) {
        itemsIndexed(hours, key = { _, hour -> hour.time }) { index, hour ->
            Row(
                modifier = Modifier.animateItem(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (index > 0) {
                    NoirPipeDivider(modifier = Modifier.width(NoirSpacing.columnGap.scaledByFont()))
                }
                HourColumn(hour)
            }
        }
    }
}

/** One 64 × 104 dp column: hour, glyph, temperature, and a line kept clear for precip. */
@Composable
fun HourColumn(
    hour: HourUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            // The column is a box built to hold four lines of type, so it grows with them —
            // otherwise "12:00" alone is wider than the slot at 200%.
            .width(ColumnWidth.scaledByFont())
            .heightIn(min = ColumnMinHeight.scaledByFont())
            .semantics(mergeDescendants = true) { contentDescription = hour.contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        // The stack is centered in the column and evenly spaced, so it reads against the pipe
        // divider beside it instead of bunching at the top with dead space underneath.
        verticalArrangement = Arrangement.spacedBy(StackGap, Alignment.CenterVertically),
    ) {
        Text(
            text = hour.time,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NoirConditionGlyph(
            condition = hour.condition,
            style = NoirTheme.type.glyphHour,
        )
        Text(
            text = hour.temperature,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        // Reserved regardless if there is a figure, so every column is the same height.
        Text(
            text = hour.precip.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = NoirTheme.colors.precip,
            modifier = Modifier.heightIn(min = PrecipLineHeight),
        )
    }
}

@NoirPreviews
@Composable
private fun HourStripPreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            HourStrip(
                hours = listOf(
                    HourUi(
                        "Now",
                        NoirCondition.Partly,
                        "27°",
                        contentDescription = "Now, 27 degrees"
                    ),
                    HourUi(
                        "12:00",
                        NoirCondition.ClearDay,
                        "28°",
                        contentDescription = "12:00, 28 degrees"
                    ),
                    HourUi(
                        time = "18:00",
                        condition = NoirCondition.Partly,
                        temperature = "26°",
                        precip = "10%",
                        contentDescription = "18:00, 26 degrees, 10 percent chance of rain",
                    ),
                    HourUi(
                        "21:00",
                        NoirCondition.ClearNight,
                        "22°",
                        contentDescription = "21:00, 22 degrees"
                    ),
                ),
            )
        }
    }
}

private val ColumnWidth = 64.dp
private val ColumnMinHeight = 104.dp
private val StackGap = 4.dp
private val PrecipLineHeight = 16.dp
