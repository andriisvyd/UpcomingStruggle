package com.svyd.upcomingweather.feature.search

import androidx.compose.runtime.Composable
import com.svyd.upcomingweather.core.designsystem.preview.NoirFontScalePreviews
import com.svyd.upcomingweather.core.designsystem.preview.NoirScreenPreviews
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.search.mock.MockSearch
import com.svyd.upcomingweather.feature.search.model.SearchUiState

/** Frame C — a query typed, live results under it. */
@NoirScreenPreviews
@Composable
private fun SearchTypingPreview() {
    UpcomingWeatherTheme { PreviewSearch(MockSearch.typing) }
}

/** Empty query — "Cold cases". */
@NoirScreenPreviews
@Composable
private fun SearchRecentsPreview() {
    UpcomingWeatherTheme { PreviewSearch(MockSearch.idle) }
}

/** Geocoding came back empty. */
@NoirScreenPreviews
@Composable
private fun SearchNoResultsPreview() {
    UpcomingWeatherTheme { PreviewSearch(MockSearch.noResults) }
}

/** Geocoding failed. */
@NoirScreenPreviews
@Composable
private fun SearchErrorPreview() {
    UpcomingWeatherTheme { PreviewSearch(MockSearch.failed) }
}

/** The field and the rows at 130% and 200%. */
@NoirFontScalePreviews
@Composable
private fun SearchLargeFontPreview() {
    UpcomingWeatherTheme { PreviewSearch(MockSearch.typing) }
}

@Composable
private fun PreviewSearch(state: SearchUiState) {
    SearchScreen(
        state = state,
        onQueryChange = {},
        onClearQuery = {},
        onBack = {},
        onCitySelected = {},
        onUseCurrentLocation = {},
        onRetry = {},
    )
}