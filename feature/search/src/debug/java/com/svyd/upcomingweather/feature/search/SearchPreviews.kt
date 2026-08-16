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
    UpcomingWeatherTheme { SearchScreen(state = MockSearch.typing) }
}

/** Empty query — "Cold cases". */
@NoirScreenPreviews
@Composable
private fun SearchRecentsPreview() {
    UpcomingWeatherTheme { SearchScreen(state = MockSearch.idle) }
}

/** Geocoding came back empty. */
@NoirScreenPreviews
@Composable
private fun SearchNoResultsPreview() {
    UpcomingWeatherTheme { SearchScreen(state = MockSearch.noResults) }
}

/** Geocoding failed. */
@NoirScreenPreviews
@Composable
private fun SearchErrorPreview() {
    UpcomingWeatherTheme { SearchScreen(state = MockSearch.failed) }
}

/** The field and the rows at 130% and 200%. */
@NoirFontScalePreviews
@Composable
private fun SearchLargeFontPreview() {
    UpcomingWeatherTheme { SearchScreen(state = MockSearch.typing) }
}
