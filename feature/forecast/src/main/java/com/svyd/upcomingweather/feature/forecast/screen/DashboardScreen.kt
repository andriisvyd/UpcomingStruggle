package com.svyd.upcomingweather.feature.forecast.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
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
import com.svyd.upcomingweather.feature.forecast.component.Attribution
import com.svyd.upcomingweather.feature.forecast.component.DayRow
import com.svyd.upcomingweather.feature.forecast.component.ForecastSkeleton
import com.svyd.upcomingweather.feature.forecast.component.HeroBlock
import com.svyd.upcomingweather.feature.forecast.component.HourStrip
import com.svyd.upcomingweather.feature.forecast.component.OfflineBanner
import com.svyd.upcomingweather.feature.forecast.component.PullNotice
import com.svyd.upcomingweather.feature.forecast.component.NoticeHeight
import com.svyd.upcomingweather.feature.forecast.component.ReadingLedger
import com.svyd.upcomingweather.feature.forecast.model.Busy
import com.svyd.upcomingweather.feature.forecast.model.DayUi
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import com.svyd.upcomingweather.feature.forecast.model.Freshness
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    state: ForecastUiState,
    onLocationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onDayClick: (date: String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val pull = rememberPullToRefreshState()
    val scrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 0 ||
                pull.distanceFraction > 0f
        }
    }
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
            // Only once the page has moved under it, scrolled or dragged: at rest the bar sits on
            // the same sheet as the content and a rule would divide nothing.
            if (scrolled) NoirHairlineDivider()

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

                ForecastUiState.LocationUnavailable -> NoirEmptyStateMessage(
                    glyph = NoirStateMark.Error,
                    title = stringResource(R.string.forecast_no_position_title),
                    body = stringResource(R.string.forecast_no_position_body),
                ) {
                    NoirPrimaryAction(
                        stringResource(R.string.forecast_no_position_action),
                        onClick = onLocationClick,
                    )
                    NoirSecondaryAction(
                        stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                is ForecastUiState.LocationRefused -> NoirEmptyStateMessage(
                    glyph = NoirStateMark.Empty,
                    title = stringResource(R.string.forecast_refused_title),
                    body = stringResource(
                        if (state.canAskAgain) {
                            R.string.forecast_refused_again_body
                        } else {
                            R.string.forecast_refused_body
                        },
                    ),
                ) {
                    // While the platform will still offer the prompt, asking again is the way
                    // through; once it stops, only settings can reverse the refusal. A button that
                    // cannot work is worse than none, so only one of the two is ever drawn.
                    if (state.canAskAgain) {
                        NoirPrimaryAction(
                            stringResource(R.string.forecast_refused_again_action),
                            onClick = onLocationClick,
                        )
                    } else {
                        NoirPrimaryAction(
                            stringResource(R.string.forecast_refused_action),
                            onClick = onOpenSettings,
                        )
                    }
                    NoirSecondaryAction(
                        stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                ForecastUiState.Error -> NoirEmptyStateMessage(
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
                    pull = pull,
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
    pull: PullToRefreshState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onDayClick: (date: String) -> Unit,
) {
    val travel = with(LocalDensity.current) { NoticeHeight.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Clipped so the line is hidden under the app bar until the page is dragged off it.
            .clipToBounds()
            // Nothing is reported under the reader's thumb: the page itself moves and the line
            // behind it is what answers. Refreshing is always false, so letting go returns the
            // page at once and the app bar carries it from there.
            .pullToRefresh(isRefreshing = false, state = pull, onRefresh = onRefresh),
    ) {
        val pulled = pull.distanceFraction.coerceIn(0f, 1f)

        PullNotice(
            fraction = pull.distanceFraction,
            modifier = Modifier
                .align(Alignment.TopCenter)
                // Travels with the page it is uncovered by, from behind the bar down into place.
                .graphicsLayer { translationY = (pulled - 1f) * travel },
        )

        LazyColumn(
            state = listState,
            contentPadding = NoirInsetDefaults.scrollableContentPadding,
            modifier = Modifier.graphicsLayer { translationY = pulled * travel },
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
                Attribution(Modifier.padding(horizontal = NoirSpacing.gutter))
            }
        }
    }
}

/**
 * The app-bar status line: the city, plus the temperature once the hero has scrolled away.
 *
 * The states that carry no forecast carry no place name either — where the name comes from is the
 * data layer's business, and on the current-location path it arrives no earlier than the weather.
 */
@Composable
private fun ForecastUiState.title(heroCollapsed: Boolean): String = when (this) {
    ForecastUiState.Empty,
    ForecastUiState.Error,
    is ForecastUiState.LocationRefused,
    ForecastUiState.LocationUnavailable,
        -> stringResource(R.string.forecast_app_title)

    is ForecastUiState.Loading -> when (busy) {
        Busy.Locating -> stringResource(R.string.forecast_locating_title)
        Busy.Updating -> stringResource(R.string.forecast_loading_title)
    }

    is ForecastUiState.Content -> when (busy) {
        Busy.Locating -> stringResource(R.string.forecast_locating_title)
        Busy.Updating -> stringResource(R.string.forecast_updating_title)
        null -> if (heroCollapsed) "$city · ${hero.temperature}" else city
    }
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
                    freshness = Freshness.Stale(refreshedAt = "10:12"),
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
