package com.svyd.upcomingweather.feature.forecast.screen

import androidx.compose.foundation.ScrollState
import com.svyd.upcomingweather.core.designsystem.primitive.NoirConditionGlyph
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.NoirInsetDefaults
import com.svyd.upcomingweather.core.designsystem.foundation.scaledByFont
import com.svyd.upcomingweather.core.designsystem.foundation.travelsBetweenScreens
import com.svyd.upcomingweather.core.designsystem.preview.NoirScreenPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.designsystem.primitive.NoirEmptyStateMessage
import com.svyd.upcomingweather.core.designsystem.primitive.NoirGlyph
import com.svyd.upcomingweather.core.designsystem.primitive.NoirHairlineDivider
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTypedIcon
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBarTitle
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSectionStamp
import com.svyd.upcomingweather.core.designsystem.primitive.NoirMarkerBar
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.DayDetailsViewModel
import com.svyd.upcomingweather.feature.forecast.R
import com.svyd.upcomingweather.feature.forecast.component.Attribution
import com.svyd.upcomingweather.feature.forecast.component.ForecastSkeleton
import com.svyd.upcomingweather.feature.forecast.component.HeroBlock
import com.svyd.upcomingweather.feature.forecast.component.ReadingLedger
import com.svyd.upcomingweather.feature.forecast.model.DayDetailsUiState
import com.svyd.upcomingweather.feature.forecast.model.Freshness
import com.svyd.upcomingweather.feature.forecast.model.HeroUi
import com.svyd.upcomingweather.feature.forecast.model.ReadingUi
import com.svyd.upcomingweather.feature.forecast.model.SlotUi
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

/** One day of that forecast. The date comes from the row that was tapped. */
@Composable
fun DayDetailsScreen(
    date: LocalDate,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val viewModel: DayDetailsViewModel = koinViewModel { parametersOf(date) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    DayDetailsScreen(modifier = modifier, state = state, onBack = onBack)
}


/**
 * One day, opened from a row of the five-day list.
 *
 * Reads from the same forecast the list was built from, so a stored one is drawn at once and
 * replaced when the fetch behind it lands.
 */
@Composable
fun DayDetailsScreen(
    modifier: Modifier = Modifier,
    state: DayDetailsUiState,
    onBack: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val scrolled by remember {
        derivedStateOf {
            scrollState.value != 0
        }
    }
    NoirBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            NoirTopBar(
                modifier = Modifier
                    .padding(horizontal = NoirSpacing.s)
                    .travelsBetweenScreens(AppBarTravel),
                navigation = {
                    NoirGlyph(
                        modifier = Modifier.size(NoirSpacing.touchTarget),
                        glyph = NoirTypedIcon.Back,
                        style = NoirTheme.type.glyphNavIcon,
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onSurface,
                        pressedTint = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                NoirTopBarTitle(state.topBarTitle())
            }

            if (scrolled) NoirHairlineDivider()

            when (state) {
                is DayDetailsUiState.Loading -> ForecastSkeleton()

                DayDetailsUiState.Unavailable -> NoirEmptyStateMessage(
                    glyph = NoirTypedIcon.Empty,
                    title = stringResource(R.string.forecast_day_unavailable_title),
                    body = stringResource(R.string.forecast_day_unavailable_body),
                )

                is DayDetailsUiState.Content -> DayLog(
                    state = state,
                    scrollState = scrollState,
                )
            }
        }
    }
}

@Composable
private fun DayLog(
    state: DayDetailsUiState.Content,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            // After the scroll, so it pads the content rather than the viewport: the log passes
            // under the navigation bar and still ends clear of it.
            .padding(NoirInsetDefaults.scrollableContentPadding)
            .padding(horizontal = NoirSpacing.gutter),
    ) {
        HeroBlock(hero = state.hero)

        NoirSectionStamp(
            modifier = Modifier.padding(top = NoirSpacing.section, bottom = NoirSpacing.m),
            text = state.logHeader,
        )

        state.slots.forEachIndexed { index, slot ->
            if (index > 0) NoirHairlineDivider()
            DaySlotRow(slot)
        }

        ReadingLedger(
            readings = state.readings,
            modifier = Modifier.padding(top = LedgerGap),
        )

        Attribution(Modifier.padding(horizontal = NoirSpacing.gutter))
    }
}

/** The app-bar line: the day being shown, or the app's own name when there is none. */
@Composable
private fun DayDetailsUiState.topBarTitle(): String = when (this) {
    is DayDetailsUiState.Content -> title
    // Named before it is read, so the bar the list was under does not start saying the app's name.
    is DayDetailsUiState.Loading -> title
    DayDetailsUiState.Unavailable -> stringResource(R.string.forecast_app_title)
}

/** One 3-hour slot of the day log, its temperature marked on the day's own span. */
@Composable
fun DaySlotRow(
    slot: SlotUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NoirSpacing.touchTarget.scaledByFont())
            .semantics(mergeDescendants = true) { contentDescription = slot.contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NoirSpacing.s),
    ) {
        Text(
            text = slot.time,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.width(TimeWidth.scaledByFont()),
        )
        NoirConditionGlyph(
            condition = slot.condition,
            style = NoirTheme.type.glyphDay,
            modifier = Modifier.width(GlyphCell.scaledByFont()),
        )
        Text(
            text = slot.precip.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = NoirTheme.colors.precip,
            modifier = Modifier.width(PrecipCell.scaledByFont()),
        )
        // This row has four cells to the day row's six, so the marker keeps its track at every
        // font scale — it just gets fewer cells to quantize into.
        NoirMarkerBar(
            fraction = slot.markerFraction,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = slot.temperature,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(TempWidth.scaledByFont()),
        )
    }
}

@NoirScreenPreviews
@Composable
private fun DayDetailsScreenPreview() {
    UpcomingWeatherTheme {
        DayDetailsScreen(
            state = DayDetailsUiState.Content(
                title = "Friday · Aug 7",
                hero = HeroUi(
                    temperature = "27°",
                    condition = NoirCondition.Rain,
                    conditionLabel = "Rain",
                    line = "Bloody awful rain, cold as a debt.",
                    high = "27°",
                    low = "18°",
                    freshness = Freshness.Stale(refreshedAt = "10:12"),
                ),
                logHeader = "Friday, step by step",
                slots = listOf(
                    SlotUi(
                        time = "00:00",
                        condition = NoirCondition.ClearNight,
                        markerFraction = 0.11f,
                        temperature = "19°",
                        contentDescription = "00:00, clear, 19 degrees",
                    ),
                    SlotUi(
                        time = "03:00",
                        condition = NoirCondition.Rain,
                        precip = "30%",
                        markerFraction = 0f,
                        temperature = "18°",
                        contentDescription = "03:00, rain, 30 percent, 18 degrees",
                    ),
                    SlotUi(
                        time = "06:00",
                        condition = NoirCondition.Rain,
                        precip = "55%",
                        markerFraction = 0f,
                        temperature = "18°",
                        contentDescription = "06:00, rain, 55 percent, 18 degrees",
                    ),
                    SlotUi(
                        time = "09:00",
                        condition = NoirCondition.Rain,
                        precip = "60%",
                        markerFraction = 0.22f,
                        temperature = "20°",
                        contentDescription = "09:00, rain, 60 percent, 20 degrees",
                    ),
                    SlotUi(
                        time = "12:00",
                        condition = NoirCondition.Rain,
                        precip = "60%",
                        markerFraction = 0.56f,
                        temperature = "23°",
                        contentDescription = "12:00, rain, 60 percent, 23 degrees",
                    ),
                    SlotUi(
                        time = "15:00",
                        condition = NoirCondition.Partly,
                        precip = "35%",
                        markerFraction = 0.89f,
                        temperature = "26°",
                        contentDescription = "15:00, partly cloudy, 35 percent, 26 degrees",
                    ),
                    SlotUi(
                        time = "18:00",
                        condition = NoirCondition.Partly,
                        markerFraction = 0.78f,
                        temperature = "25°",
                        contentDescription = "18:00, partly cloudy, 25 degrees",
                    ),
                    SlotUi(
                        time = "21:00",
                        condition = NoirCondition.ClearNight,
                        markerFraction = 0.33f,
                        temperature = "21°",
                        contentDescription = "21:00, clear, 21 degrees",
                    ),
                ),
                readings = listOf(
                    ReadingUi("Humidity", "74%", "clouds packing 90%"),
                    ReadingUi("Wind", "18 km/h", "gusts of 34, no warning"),
                    ReadingUi("Precip", "60%", "4.2 mm on the books"),
                    ReadingUi("Pressure", "1004 hPa", "1000 hPa on the street"),
                ),
            ),
        )
    }
}

private val LedgerGap = 8.dp
private val TimeWidth = 48.dp
private val TempWidth = 36.dp
private val GlyphCell = 24.dp
private val PrecipCell = 32.dp
