package com.svyd.upcomingweather.feature.forecast.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.NoirInsetDefaults
import com.svyd.upcomingweather.core.designsystem.foundation.arrivesFromBelow
import com.svyd.upcomingweather.core.designsystem.foundation.arrivesFromEnd
import com.svyd.upcomingweather.core.designsystem.foundation.travelsBetweenScreens
import com.svyd.upcomingweather.core.designsystem.preview.NoirScreenPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTypedIcon
import com.svyd.upcomingweather.core.designsystem.primitive.NoirHairlineDivider
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBarTitle
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPrimaryAction
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSecondaryAction
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSectionStamp
import com.svyd.upcomingweather.core.designsystem.primitive.NoirEmptyStateMessage
import com.svyd.upcomingweather.core.designsystem.primitive.NoirGlyph
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.ForecastViewModel
import com.svyd.upcomingweather.feature.forecast.R
import com.svyd.upcomingweather.feature.forecast.component.Attribution
import com.svyd.upcomingweather.feature.forecast.component.DayRow
import com.svyd.upcomingweather.feature.forecast.component.ForecastSkeleton
import com.svyd.upcomingweather.feature.forecast.component.HeroBlock
import com.svyd.upcomingweather.feature.forecast.component.HourStrip
import com.svyd.upcomingweather.feature.forecast.component.OpeningGlyphTravel
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
import com.svyd.upcomingweather.feature.forecast.model.OfflineUi
import com.svyd.upcomingweather.feature.forecast.model.ReadingUi
import kotlinx.coroutines.flow.Flow

private const val HERO_KEY = "hero"
private const val HOURS_HEADER_KEY = "hoursHeader"
private const val HOURS_KEY = "hours"
private const val READINGS_KEY = "readings"
private const val DAYS_HEADER_KEY = "daysHeader"
private const val ATTRIBUTION_KEY = "attribution"

/**
 * Where each part of the page falls in the stagger as the dashboard arrives.
 *
 * The hero has no step: the glyph travels to it from the splash and the lines type themselves in,
 * which is already more motion than one block needs. Everything under it counts from the first
 * thing the hero does not cover, so the page assembles downward at a steady beat.
 */
private const val HoursHeaderOrder = 0
private const val HoursOrder = 1
private const val ReadingsOrder = 2
private const val DaysHeaderOrder = 3
private const val DaysOrder = 4

/**
 * The dashboard, wired to what it draws.
 *
 * Kept apart from [DashboardScreen] so that one stays a function of its arguments — which is what
 * lets every state of it be drawn in a preview.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onDayClick: (date: String) -> Unit,
    onRequestLocationPermission: () -> Unit,
    permissionResult: Flow<Boolean>,
    onOpenSettings: () -> Unit,
) {
    val viewModel: ForecastViewModel = forecastViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = permissionResult) {
        permissionResult.collect { canAskAgain ->
            viewModel.useCurrentLocation(canAskAgain = canAskAgain)
        }
    }

    DashboardScreen(
        modifier = modifier,
        state = state,
        onLocationClick = onRequestLocationPermission,
        onSearchClick = onSearchClick,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::refresh,
        onDayClick = onDayClick,
        onOpenSettings = onOpenSettings,
    )
}

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
        derivedStateOf { listState.canScrollBackward || pull.distanceFraction != 0f }
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
                modifier = Modifier.travelsBetweenScreens(AppBarTravel),
                navigation = {
                    NoirGlyph(
                        modifier = Modifier.padding(all = NoirSpacing.m),
                        glyph = NoirTypedIcon.Gps,
                        onClick = onLocationClick,
                        style = NoirTheme.type.glyphNavIcon,
                        tint = MaterialTheme.colorScheme.onSurface,
                        pressedTint = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {
                    NoirGlyph(
                        modifier = Modifier.padding(all = NoirSpacing.m),
                        glyph = NoirTypedIcon.Search,
                        style = NoirTheme.type.glyphNavIcon,
                        onClick = onSearchClick,
                        tint = MaterialTheme.colorScheme.onSurface,
                        pressedTint = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                NoirTopBarTitle(state.title(heroCollapsed))
            }

            // A screen-level notice rather than a row of the forecast: it sits under the bar and
            // stays there, so it cannot be scrolled past unread, and it is out of the list — a
            // zero-height row at the top would make the hero the first visible item and leave the
            // rule above permanently drawn.
            OfflineNotice(
                offline = (state as? ForecastUiState.Content)?.offline,
                onRetry = onRetry,
            )

            // Only once the page has moved under it, scrolled or dragged: at rest the bar sits on
            // the same sheet as the content and a rule would divide nothing.
            if (scrolled) NoirHairlineDivider()

            when (state) {
                is ForecastUiState.Loading -> ForecastSkeleton()

                ForecastUiState.Empty -> NoirEmptyStateMessage(
                    glyph = NoirTypedIcon.Empty,
                    title = stringResource(R.string.forecast_empty_title),
                    body = stringResource(R.string.forecast_empty_body),
                ) {
                    NoirPrimaryAction(
                        text = stringResource(R.string.forecast_trace_action),
                        onClick = onLocationClick
                    )
                    NoirSecondaryAction(
                        text = stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                ForecastUiState.LocationUnavailable -> NoirEmptyStateMessage(
                    glyph = NoirTypedIcon.Error,
                    title = stringResource(R.string.forecast_no_position_title),
                    body = stringResource(R.string.forecast_no_position_body),
                ) {
                    NoirPrimaryAction(
                        text = stringResource(R.string.forecast_no_position_action),
                        onClick = onLocationClick,
                    )
                    NoirSecondaryAction(
                        text = stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                is ForecastUiState.LocationRefused -> NoirEmptyStateMessage(
                    glyph = NoirTypedIcon.Empty,
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
                            text = stringResource(R.string.forecast_refused_again_action),
                            onClick = onLocationClick,
                        )
                    } else {
                        NoirPrimaryAction(
                            text = stringResource(R.string.forecast_refused_action),
                            onClick = onOpenSettings,
                        )
                    }
                    NoirSecondaryAction(
                        text = stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                ForecastUiState.Error -> NoirEmptyStateMessage(
                    glyph = NoirTypedIcon.Error,
                    title = stringResource(R.string.forecast_error_title),
                    body = stringResource(R.string.forecast_error_body),
                ) {
                    NoirPrimaryAction(
                        text = stringResource(R.string.forecast_error_action),
                        onClick = onRetry
                    )
                    NoirSecondaryAction(
                        text = stringResource(R.string.forecast_name_city_action),
                        onClick = onSearchClick
                    )
                }

                is ForecastUiState.Content -> DashboardContent(
                    content = state,
                    listState = listState,
                    pull = pull,
                    onRefresh = onRefresh,
                    onDayClick = onDayClick,
                )
            }
        }
    }
}

/**
 * The offline notice, pinned under the app bar.
 *
 * Slides out from behind the bar as the space for it opens, and holds its last text on the way out
 * so the exit has something to draw.
 */
@Composable
private fun OfflineNotice(offline: OfflineUi?, onRetry: () -> Unit) {
    var last by remember { mutableStateOf(offline) }
    offline?.let { last = it }

    AnimatedVisibility(
        visible = offline != null,
        enter = expandVertically() + slideInVertically { height -> -height },
        exit = shrinkVertically() + slideOutVertically { height -> -height },
    ) {
        last?.let {
            OfflineBanner(
                modifier = Modifier
                    .padding(horizontal = NoirSpacing.gutter)
                    .padding(top = NoirSpacing.s, bottom = NoirSpacing.s),
                offline = it,
                onRetry = onRetry,
            )
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
            item(key = HERO_KEY) {
                HeroBlock(
                    modifier = Modifier.padding(horizontal = NoirSpacing.gutter),
                    hero = content.hero,
                    glyphTravel = OpeningGlyphTravel,
                )
            }

            item(key = HOURS_HEADER_KEY) {
                NoirSectionStamp(
                    modifier = Modifier
                        .arrivesFromBelow(HoursHeaderOrder)
                        .padding(horizontal = NoirSpacing.gutter)
                        .padding(
                            top = NoirSpacing.section,
                            bottom = NoirSpacing.m,
                        ),
                    text = stringResource(R.string.forecast_hours_header),
                )
            }
            // The one strip that scrolls sideways is the one that arrives sideways: what the page
            // does on the way in says which way it can be moved once it is there.
            item(key = HOURS_KEY) {
                HourStrip(
                    modifier = Modifier.arrivesFromEnd(HoursOrder),
                    hours = content.hours,
                )
            }

            item(key = READINGS_KEY) {
                ReadingLedger(
                    modifier = Modifier
                        .arrivesFromBelow(ReadingsOrder)
                        .padding(horizontal = NoirSpacing.gutter)
                        .padding(top = LedgerGap),
                    readings = content.readings,
                )
            }

            item(key = DAYS_HEADER_KEY) {
                NoirSectionStamp(
                    modifier = Modifier
                        .arrivesFromBelow(DaysHeaderOrder)
                        .padding(horizontal = NoirSpacing.gutter)
                        .padding(top = NoirSpacing.section, bottom = NoirSpacing.m),
                    text = stringResource(R.string.forecast_days_header),
                )
            }
            itemsIndexed(content.days) { index, day ->
                // The rule and the row are two children of one item and take the same step, so a
                // day arrives whole rather than the line under it catching up.
                val order = DaysOrder + index
                if (index > 0) {
                    NoirHairlineDivider(
                        Modifier
                            .arrivesFromBelow(order)
                            .padding(horizontal = NoirSpacing.gutter),
                    )
                }
                // No padding here — the row pads its own content so the tap target reaches
                // both screen edges.
                DayRow(
                    modifier = Modifier.arrivesFromBelow(order),
                    day = day,
                    onClick = { onDayClick(day.date) },
                )
            }

            item(key = ATTRIBUTION_KEY) {
                Attribution(
                    Modifier
                        .arrivesFromBelow(DaysOrder + content.days.size)
                        .padding(horizontal = NoirSpacing.gutter),
                )
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
