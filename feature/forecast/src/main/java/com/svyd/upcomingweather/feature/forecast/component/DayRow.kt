package com.svyd.upcomingweather.feature.forecast.component

import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.designsystem.primitive.NoirConditionGlyph

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.largeFontScale
import com.svyd.upcomingweather.core.designsystem.foundation.scaledByFont
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirRangeBar
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.core.designsystem.primitive.NoirDisclosureMark
import com.svyd.upcomingweather.feature.forecast.R
import com.svyd.upcomingweather.feature.forecast.model.DayUi

/**
 * One day of the five: name, glyph, precip, low, the typed range bar, high, and the `>` that
 * says the row opens something.
 *
 * The whole row is the target, and it announces as a single element. Past ~130% type the six
 * cells stop fitting on one line, so the row reflows to two and drops the range bar — the bar is
 * a second reading of the low and high, which are still right there.
 */
@Composable
fun DayRow(
    modifier: Modifier = Modifier,
    day: DayUi,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = DayRowDefaults.ContentPadding,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(R.string.forecast_cd_open_day, day.name),
                onClick = onClick,
            )
            .heightIn(min = RowMinHeight.scaledByFont())
            // Inside the clickable, not outside it: the row reads inset but takes a tap anywhere
            // across the screen, including the gutters.
            .padding(contentPadding)
            .semantics(mergeDescendants = true) { contentDescription = day.contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NoirSpacing.s),
    ) {
        if (largeFontScale()) {
            CompactDay(day)
        } else {
            WideDay(day)
        }
        NoirDisclosureMark(modifier = Modifier.width(DisclosureWidth.scaledByFont()))
    }
}

/** Tokens for [DayRow]. */
object DayRowDefaults {
    /**
     * Aligns the row's content with the ledger and the section headers above it. The row itself
     * still spans the full width, so the target is edge to edge.
     */
    val ContentPadding = PaddingValues(horizontal = NoirSpacing.gutter)
}

/** The spec layout: everything on one line, the bar taking whatever is left. */
@Composable
private fun RowScope.WideDay(day: DayUi) {
    Text(
        text = day.name,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(DayNameWidth.scaledByFont()),
    )
    ConditionGlyph(day)
    Text(
        text = day.precip.orEmpty(),
        style = MaterialTheme.typography.labelSmall,
        color = NoirTheme.colors.precip,
        modifier = Modifier.width(PrecipCell.scaledByFont()),
    )
    Text(
        text = day.min,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        modifier = Modifier.width(MinTempWidth.scaledByFont()),
    )
    NoirRangeBar(
        startFraction = day.rangeStart,
        endFraction = day.rangeEnd,
        modifier = Modifier.weight(1f),
    )
    Text(
        text = day.max,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.End,
        modifier = Modifier.width(PrecipCell.scaledByFont()),
    )
}

/**
 * Large type, still one line: name, glyph, the range as figures.
 *
 * Six cells do not fit across 360 dp at 200%, so the row keeps what only it can say — which day,
 * what it does, how cold and how warm — and gives up the range bar (a second reading of the two
 * figures beside it) and the precip cell. Both remain in the row's spoken description, and precip
 * gets its own ledger reading on the day's details screen.
 */
@Composable
private fun RowScope.CompactDay(day: DayUi) {
    Text(
        text = day.name,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
    )
    ConditionGlyph(day)
    Text(
        text = "${day.min}–${day.max}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        softWrap = false,
    )
}

@Composable
private fun ConditionGlyph(day: DayUi) {
    NoirConditionGlyph(
        condition = day.condition,
        style = NoirTheme.type.glyphDay,
        modifier = Modifier.width(GlyphCell.scaledByFont()),
    )
}

@NoirPreviews
@Composable
private fun DayRowPreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            Column {
                DayRow(
                    day = DayUi(
                        date = "2026-08-04",
                        name = "Today",
                        condition = NoirCondition.Partly,
                        precip = "10%",
                        min = "19°",
                        max = "29°",
                        rangeStart = 0.19f,
                        rangeEnd = 0.81f,
                        contentDescription = "Today: partly cloudy, 10% chance of precipitation, 19 to 29 degrees",
                    ),
                )
            }
        }
    }
}

private val RowMinHeight = 52.dp
private val DayNameWidth = 56.dp
private val MinTempWidth = 32.dp
private val DisclosureWidth = 12.dp
private val GlyphCell = 24.dp
private val PrecipCell = 32.dp