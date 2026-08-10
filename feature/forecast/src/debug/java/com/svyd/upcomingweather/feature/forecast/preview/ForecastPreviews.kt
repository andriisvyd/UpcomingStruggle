package com.svyd.upcomingweather.feature.forecast.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.svyd.upcomingweather.core.designsystem.preview.NoirFontScalePreviews
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.screen.DayDetailsScreen
import com.svyd.upcomingweather.feature.forecast.screen.DashboardScreen
import com.svyd.upcomingweather.feature.forecast.mock.MockForecast
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState

/** Frame B — the same city after dark. */
@Preview
@Composable
private fun ForecastNightPreview() {
    UpcomingWeatherTheme {
        PreviewForecast(MockForecast.night)
    }
}

/** Frame D — first load, no cache. */
@Preview
@Composable
private fun ForecastLoadingPreview() {
    UpcomingWeatherTheme {
        PreviewForecast(MockForecast.loading)
    }
}

/** Frame E — the fetch failed and there is nothing to fall back on. */
@Preview
@Composable
private fun ForecastErrorPreview() {
    UpcomingWeatherTheme {
        PreviewForecast(MockForecast.error)
    }
}

/** Frame F — first launch, no city chosen. */
@Preview
@Composable
private fun ForecastEmptyPreview() {
    UpcomingWeatherTheme {
        PreviewForecast(MockForecast.empty)
    }
}

/** Content plus the offline banner. */
@Preview
@Composable
private fun ForecastOfflinePreview() {
    UpcomingWeatherTheme {
        PreviewForecast(MockForecast.offline)
    }
}

/** Frame H — the day details destination. */
@Preview
@Composable
private fun DayDetailsPreview() {
    UpcomingWeatherTheme {
        DayDetailsScreen(state = MockForecast.friday, onBack = {})
    }
}

/** The whole dashboard at 130% and 200% — where the day rows reflow to two lines. */
@NoirFontScalePreviews
@Composable
private fun ForecastLargeFontPreview() {
    UpcomingWeatherTheme {
        PreviewForecast(MockForecast.content)
    }
}

/** The day log at 200%, where the in-day markers drop out. */
@NoirFontScalePreviews
@Composable
private fun DayDetailsLargeFontPreview() {
    UpcomingWeatherTheme {
        DayDetailsScreen(state = MockForecast.friday, onBack = {})
    }
}

/** The empty state at 200% — the scaffold has to scroll to keep both actions reachable. */
@NoirFontScalePreviews
@Composable
private fun ForecastEmptyLargeFontPreview() {
    UpcomingWeatherTheme {
        PreviewForecast(MockForecast.empty)
    }
}

@Composable
private fun PreviewForecast(state: ForecastUiState) {
    DashboardScreen(
        state = state,
        onLocationClick = {},
        onSearchClick = {},
        onRefresh = {},
        onRetry = {},
        onDayClick = {},
    )
}
