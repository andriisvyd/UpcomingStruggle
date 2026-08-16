package com.svyd.upcomingweather.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.NoirInsetDefaults
import com.svyd.upcomingweather.core.designsystem.primitive.NoirGlyph
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTextField
import com.svyd.upcomingweather.core.designsystem.primitive.NoirHairlineDivider
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSecondaryAction
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTypedIcon
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.search.component.CityRow
import com.svyd.upcomingweather.feature.search.component.LocationNotice
import com.svyd.upcomingweather.feature.search.component.LocationRow
import com.svyd.upcomingweather.feature.search.component.RecentsHeader
import com.svyd.upcomingweather.feature.search.mock.MockSearch
import com.svyd.upcomingweather.feature.search.model.CityUi
import com.svyd.upcomingweather.feature.search.model.SearchResultsUi
import com.svyd.upcomingweather.feature.search.model.SearchUiState
import org.koin.androidx.compose.koinViewModel

/**
 * Search, wired to what it draws.
 *
 * Picking a city is not a navigation result: the choice is written down and the forecast screen
 * finds out by observing, so all this hands back is the instruction to close.
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onRequestLocationPermission: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SearchScreen(
        modifier = modifier,
        state = state,
        onQueryChange = viewModel::query,
        onClearQuery = viewModel::clearQuery,
        onBack = onBack,
        onCitySelected = { city -> viewModel.select(city, then = onDone) },
        onUseCurrentLocation = onRequestLocationPermission,
        onOpenSettings = onOpenSettings,
        onRetry = viewModel::retry,
    )
}


/**
 * Name a city, or let the phone name it for you.
 *
 * Picking a city is not a navigation result: the screen reports the choice upward and whoever
 * owns the app state persists it — the forecast screen finds out by observing, not by being told.
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    state: SearchUiState,
    onQueryChange: (String) -> Unit = {},
    onClearQuery: () -> Unit = {},
    onBack: () -> Unit = {},
    onCitySelected: (CityUi) -> Unit = {},
    onUseCurrentLocation: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    NoirBackground(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        val listState = rememberLazyListState()
        val scrolled by remember {
            derivedStateOf { listState.canScrollBackward }
        }

        Column(Modifier.fillMaxSize()) {
            NoirTopBar(
                modifier = Modifier.padding(horizontal = NoirSpacing.s),
                navigation = {
                    NoirGlyph(
                        modifier = Modifier
                            .size(NoirSpacing.touchTarget),
                        glyph = NoirTypedIcon.Back,
                        style = NoirTheme.type.glyphNavIcon,
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onSurface,
                        pressedTint = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {},
            ) {
                NoirTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    placeholder = stringResource(R.string.search_placeholder),
                    autoFocus = true,
                    onImeAction = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = FieldEndGap),
                    leading = {
                        NoirGlyph(
                            glyph = NoirTypedIcon.Empty,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailing = if (state.query.isNotEmpty()) {
                        {
                            NoirGlyph(
                                modifier = Modifier
                                    .padding(all = NoirSpacing.m)
                                    .size(TrailingIconSize),
                                glyph = NoirTypedIcon.Clear,
                                onClick = onClearQuery,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        null
                    },
                )
            }

            if (scrolled) NoirHairlineDivider()

            LazyColumn(
                state = listState,
                contentPadding = NoirInsetDefaults.scrollableContentPadding,
            ) {
                item(key = "location") {
                    Column(
                        modifier = Modifier.padding(horizontal = NoirSpacing.s),
                    ) {
                        LocationRow(onClick = onUseCurrentLocation)
                        state.location?.let { notice ->
                            LocationNotice(
                                notice = notice,
                                onAskAgain = onUseCurrentLocation,
                                onOpenSettings = onOpenSettings,
                            )
                        }
                        NoirHairlineDivider(
                            Modifier.padding(
                                vertical = DividerGap,
                                horizontal = NoirSpacing.s,
                            )
                        )
                    }
                }

                when (val results = state.results) {
                    // The recents are what the screen opens on. They are already there when it
                    // arrives, so there is nothing for them to arrive from.
                    is SearchResultsUi.Recents -> {
                        if (results.cities.isNotEmpty()) {
                            item(key = "recentsHeader") { RecentsHeader() }
                            items(results.cities, key = CityUi::id) { city ->
                                CityRow(
                                    modifier = Modifier.padding(horizontal = NoirSpacing.gutter),
                                    city = city,
                                    isRecent = true,
                                    onClick = { onCitySelected(city) },
                                )
                            }
                        }
                    }

                    // Results are replaced under the reader as they type, so the list reports what
                    // changed rather than redrawing: rows fade in and out where they are, and a
                    // city that is in both answers travels from its old place to its new one.
                    is SearchResultsUi.Cities -> items(
                        items = results.cities,
                        key = CityUi::id,
                    ) { city ->
                        CityRow(
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = RowFade,
                                    placementSpec = RowMove,
                                    fadeOutSpec = RowFade,
                                )
                                .padding(horizontal = NoirSpacing.gutter),
                            city = city,
                            onClick = { onCitySelected(city) },
                        )
                    }

                    is SearchResultsUi.NoResults -> item(key = "noResults") {
                        Text(
                            text = stringResource(R.string.search_no_results, results.query),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = NoirSpacing.gutter)
                                .padding(top = EmptyMessageTop),
                        )
                    }

                    SearchResultsUi.Error -> item(key = "error") {
                        Column(
                            Modifier
                                .padding(horizontal = NoirSpacing.gutter)
                                .padding(top = NoirSpacing.gutter),
                        ) {
                            Text(
                                text = stringResource(R.string.search_error_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            NoirSecondaryAction(
                                text = stringResource(R.string.search_error_action),
                                onClick = onRetry
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchTypingPreview() {
    UpcomingWeatherTheme {
        SearchScreen(state = MockSearch.typing)
    }
}

/** A row turning up, or dropping out. Short: what it reports is that the list has changed. */
private val RowFade = tween<Float>(durationMillis = 160, easing = LinearOutSlowInEasing)

/** A row that is in both answers, moving from where it was to where it now belongs. */
private val RowMove = tween<IntOffset>(durationMillis = 280, easing = FastOutSlowInEasing)

private val FieldEndGap = 8.dp
private val LeadingIconSize = 20.dp
private val TrailingIconSize = 24.dp
private val DividerGap = 8.dp
private val EmptyMessageTop = 84.dp
