package com.svyd.upcomingweather.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.NoirInsetDefaults
import com.svyd.upcomingweather.core.designsystem.icon.NoirIcons
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTextField
import com.svyd.upcomingweather.core.designsystem.primitive.NoirHairlineDivider
import com.svyd.upcomingweather.core.designsystem.primitive.NoirIconButton
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSecondaryAction
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.feature.search.component.CityRow
import com.svyd.upcomingweather.feature.search.component.LocationNotice
import com.svyd.upcomingweather.feature.search.component.LocationRow
import com.svyd.upcomingweather.feature.search.component.RecentsHeader
import com.svyd.upcomingweather.feature.search.model.CityUi
import com.svyd.upcomingweather.feature.search.model.SearchResultsUi
import com.svyd.upcomingweather.feature.search.model.SearchUiState

/**
 * Name a city, or let the phone name it for you.
 *
 * Picking a city is not a navigation result: the screen reports the choice upward and whoever
 * owns the app state persists it — the forecast screen finds out by observing, not by being told.
 */
@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onBack: () -> Unit,
    onCitySelected: (CityUi) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NoirBackground(modifier) {
        Column(Modifier.fillMaxSize()) {
            NoirTopBar(
                navigation = {
                    NoirIconButton(
                        icon = NoirIcons.Back,
                        contentDescription = stringResource(R.string.search_cd_back),
                        onClick = onBack,
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
                        .padding(end = FieldEndGap),
                    leading = {
                        Icon(
                            imageVector = NoirIcons.Search,
                            contentDescription = stringResource(R.string.search_cd_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(LeadingIconSize),
                        )
                    },
                    trailing = if (state.query.isNotEmpty()) {
                        {
                            NoirIconButton(
                                icon = NoirIcons.Close,
                                contentDescription = stringResource(R.string.search_cd_clear),
                                onClick = onClearQuery,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(TrailingIconSize),
                            )
                        }
                    } else {
                        null
                    },
                )
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = NoirSpacing.gutter),
                contentPadding = NoirInsetDefaults.scrollableContentPadding,
            ) {
                item(key = "location") {
                    LocationRow(onClick = onUseCurrentLocation)
                    state.location?.let { notice ->
                        LocationNotice(
                            notice = notice,
                            onAskAgain = onUseCurrentLocation,
                            onOpenSettings = onOpenSettings,
                        )
                    }
                    NoirHairlineDivider(Modifier.padding(vertical = DividerGap))
                }

                when (val results = state.results) {
                    is SearchResultsUi.Recents -> {
                        if (results.cities.isNotEmpty()) {
                            item(key = "recentsHeader") { RecentsHeader() }
                            items(results.cities, key = { it.id }) { city ->
                                CityRow(
                                    city = city,
                                    isRecent = true,
                                    onClick = { onCitySelected(city) },
                                )
                            }
                        }
                    }

                    is SearchResultsUi.Cities -> items(results.cities, key = { it.id }) { city ->
                        CityRow(city = city, onClick = { onCitySelected(city) })
                    }

                    is SearchResultsUi.NoResults -> item(key = "noResults") {
                        Text(
                            text = stringResource(R.string.search_no_results, results.query),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = EmptyMessageTop),
                        )
                    }

                    SearchResultsUi.Error -> item(key = "error") {
                        Column(Modifier.padding(top = NoirSpacing.gutter)) {
                            Text(
                                text = stringResource(R.string.search_error_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            NoirSecondaryAction(text = stringResource(R.string.search_error_action), onClick = onRetry)
                        }
                    }
                }
            }
        }
    }
}

private val FieldEndGap = 8.dp
private val LeadingIconSize = 20.dp
private val TrailingIconSize = 24.dp
private val DividerGap = 8.dp
private val EmptyMessageTop = 84.dp
