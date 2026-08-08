package com.svyd.upcomingweather.feature.forecast.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.NoirInsetDefaults
import com.svyd.upcomingweather.core.designsystem.icon.NoirIcons
import com.svyd.upcomingweather.core.designsystem.preview.NoirScreenPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.designsystem.primitive.NoirStateMark
import com.svyd.upcomingweather.core.designsystem.primitive.NoirHairlineDivider
import com.svyd.upcomingweather.core.designsystem.primitive.NoirIconButton
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBarTitle
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPrimaryAction
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSecondaryAction
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSectionStamp
import com.svyd.upcomingweather.core.designsystem.primitive.NoirEmptyStateMessage
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.R
import com.svyd.upcomingweather.feature.forecast.component.DayRow
import com.svyd.upcomingweather.feature.forecast.component.ForecastSkeleton
import com.svyd.upcomingweather.feature.forecast.component.HeroBlock
import com.svyd.upcomingweather.feature.forecast.component.HourStrip
import com.svyd.upcomingweather.feature.forecast.component.OfflineBanner
import com.svyd.upcomingweather.feature.forecast.component.ReadingLedger
import com.svyd.upcomingweather.feature.forecast.model.DayUi
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import com.svyd.upcomingweather.feature.forecast.model.HeroUi
import com.svyd.upcomingweather.feature.forecast.model.HourUi
import com.svyd.upcomingweather.feature.forecast.model.ReadingUi

private const val HERO_KEY = "hero"
private const val HOURS_HEADER_KEY = "hoursHeader"
private const val HOURS_KEY = "hours"
private const val READINGS_KEY = "readings"
private const val OFFLINE_KEY = "offline"
private const val DAYS_HEADER_KEY = "daysHeader"
private const val ATTRIBUTION_KEY = "attribution"

/**
 * The dashboard. Today and the week live in one scroll.
 *
 * Renders whatever [state] it is handed and reports every gesture upward — it holds no data of
 * its own, and the only state it does own is where the list is scrolled to.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    state: ForecastUiState,
    onLocationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onDayClick: (date: String) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val heroCollapsed by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            layout.totalItemsCount > 0 && layout.visibleItemsInfo.none { it.key == HERO_KEY }
        }
    }

    NoirBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            NoirTopBar(
                navigation = {
                    NoirIconButton(
                        icon = NoirIcons.MyLocation,
                        contentDescription = stringResource(R.string.forecast_cd_location),
                        onClick = onLocationClick,
                    )
                },
                actions = {
                    NoirIconButton(
                        icon = NoirIcons.Search,
                        contentDescription = stringResource(R.string.forecast_cd_search),
                        onClick = onSearchClick,
                    )
                },
            ) {
                NoirTopBarTitle(state.title(heroCollapsed))
            }

            when (state) {
                is ForecastUiState.Loading -> ForecastSkeleton()

                ForecastUiState.Empty -> NoirEmptyStateMessage(
                    glyph = NoirStateMark.Empty,
                    title = stringResource(R.string.forecast_empty_title),
                    body = stringResource(R.string.forecast_empty_body),
                ) {
                    NoirPrimaryAction(
                        stringResource(R.string.forecast_trace_action),
                        onClick = onLocationClick
                    )
                    NoirSecondaryAction(
                        stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                is ForecastUiState.Error -> NoirEmptyStateMessage(
                    glyph = NoirStateMark.Error,
                    title = stringResource(R.string.forecast_error_title),
                    body = stringResource(R.string.forecast_error_body),
                ) {
                    NoirPrimaryAction(
                        stringResource(R.string.forecast_error_action),
                        onClick = onRetry
                    )
                    NoirSecondaryAction(
                        stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                is ForecastUiState.Content -> DashboardContent(
                    content = state,
                    listState = listState,
                    onRefresh = onRefresh,
                    onRetry = onRetry,
                    onDayClick = onDayClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    modifier: Modifier = Modifier,
    content: ForecastUiState.Content,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onDayClick: (date: String) -> Unit,
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = content.isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            state = listState,
            contentPadding = NoirInsetDefaults.scrollableContentPadding,
        ) {
            if (content.offline != null) {
                item(key = OFFLINE_KEY) {
                    OfflineBanner(
                        modifier = Modifier
                            .padding(horizontal = NoirSpacing.gutter)
                            .padding(
                                top = NoirSpacing.s,
                                bottom = NoirSpacing.s,
                            ),
                        offline = content.offline,
                        onRetry = onRetry,
                    )
                }
            }

            item(key = HERO_KEY) {
                HeroBlock(
                    modifier = Modifier.padding(horizontal = NoirSpacing.gutter),
                    hero = content.hero,
                )
            }

            item(key = HOURS_HEADER_KEY) {
                NoirSectionStamp(
                    modifier = Modifier
                        .padding(horizontal = NoirSpacing.gutter)
                        .padding(
                            top = NoirSpacing.section,
                            bottom = NoirSpacing.m,
                        ),
                    text = stringResource(R.string.forecast_hours_header),
                )
            }
            item(key = HOURS_KEY) {
                HourStrip(hours = content.hours)
            }

            item(key = READINGS_KEY) {
                ReadingLedger(
                    modifier = Modifier
                        .padding(horizontal = NoirSpacing.gutter)
                        .padding(top = LedgerGap),
                    readings = content.readings,
                )
            }

            item(key = DAYS_HEADER_KEY) {
                NoirSectionStamp(
                    modifier = Modifier
                        .padding(horizontal = NoirSpacing.gutter)
                        .padding(top = NoirSpacing.section, bottom = NoirSpacing.m),
                    text = stringResource(R.string.forecast_days_header),
                )
            }
            itemsIndexed(content.days) { index, day ->
                if (index > 0) {
                    NoirHairlineDivider(Modifier.padding(horizontal = NoirSpacing.gutter))
                }
                // No padding here — the row pads its own content so the tap target reaches
                // both screen edges.
                DayRow(day = day, onClick = { onDayClick(day.date) })
            }

            item(key = ATTRIBUTION_KEY) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = AttributionGap,
                            bottom = NoirSpacing.section,
                        ),
                    text = stringResource(R.string.forecast_attribution),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** The app-bar status line: the city, plus the temperature once the hero has scrolled away. */
@Composable
private fun ForecastUiState.title(heroCollapsed: Boolean): String = when (this) {
    ForecastUiState.Empty -> stringResource(R.string.forecast_app_title)
    is ForecastUiState.Loading -> city
    is ForecastUiState.Error -> city
    is ForecastUiState.Content -> if (heroCollapsed) "$city · ${hero.temperature}" else city
}

@NoirScreenPreviews
@Composable
private fun DashboardScreenPreview() {
    UpcomingWeatherTheme {
        DashboardScreen(
            state = ForecastUiState.Content(
                city = "Budapest",
                hero = HeroUi(
                    temperature = "27°",
                    condition = NoirCondition.Partly,
                    conditionLabel = "Partly cloudy",
                    line = "Clouds circle like old regrets.",
                    feelsLike = "29°",
                    high = "29°",
                    low = "19°",
                    updatedAt = "10:12",
                ),
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
                        "15:00",
                        NoirCondition.ClearDay,
                        "28°",
                        contentDescription = "15:00, 28 degrees"
                    ),
                    HourUi(
                        "18:00",
                        NoirCondition.Partly,
                        "26°",
                        contentDescription = "18:00, 26 degrees"
                    ),
                    HourUi(
                        "20:00",
                        NoirCondition.Partly,
                        "26°",
                        contentDescription = "18:00, 26 degrees"
                    ),
                ),
                readings = listOf(
                    ReadingUi("Humidity", "46%", "clouds packing 40%"),
                    ReadingUi("Wind", "12 km/h", "gusts of 26, no warning"),
                ),
                days = listOf(
                    DayUi(
                        date = "2026-08-04",
                        name = "Today",
                        condition = NoirCondition.Partly,
                        precip = "10%",
                        min = "19°",
                        max = "29°",
                        rangeStart = 0.19f,
                        rangeEnd = 0.81f,
                        contentDescription = "Today: partly cloudy, 19 to 29 degrees",
                    ),
                    DayUi(
                        date = "2026-08-05",
                        name = "Wed",
                        condition = NoirCondition.ClearDay,
                        min = "20°",
                        max = "31°",
                        rangeStart = 0.25f,
                        rangeEnd = 0.94f,
                        contentDescription = "Wednesday: clear, 20 to 31 degrees",
                    ),
                    DayUi(
                        date = "2026-08-04",
                        name = "Today",
                        condition = NoirCondition.Partly,
                        precip = "10%",
                        min = "19°",
                        max = "29°",
                        rangeStart = 0.19f,
                        rangeEnd = 0.81f,
                        contentDescription = "Today: partly cloudy, 19 to 29 degrees",
                    ),
                    DayUi(
                        date = "2026-08-05",
                        name = "Wed",
                        condition = NoirCondition.ClearDay,
                        min = "20°",
                        max = "31°",
                        rangeStart = 0.25f,
                        rangeEnd = 0.94f,
                        contentDescription = "Wednesday: clear, 20 to 31 degrees",
                    ),
                    DayUi(
                        date = "2026-08-05",
                        name = "Wed",
                        condition = NoirCondition.ClearDay,
                        min = "20°",
                        max = "31°",
                        rangeStart = 0.25f,
                        rangeEnd = 0.94f,
                        contentDescription = "Wednesday: clear, 20 to 31 degrees",
                    ),
                ),
            ),
        )
    }
}

private val LedgerGap = 8.dp
private val AttributionGap = 16.dp
