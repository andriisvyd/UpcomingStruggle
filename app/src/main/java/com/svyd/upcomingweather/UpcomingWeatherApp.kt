package com.svyd.upcomingweather

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.svyd.upcomingweather.feature.forecast.screen.DayDetailsScreen
import com.svyd.upcomingweather.feature.forecast.screen.DashboardScreen
import com.svyd.upcomingweather.feature.forecast.mock.MockForecast
import com.svyd.upcomingweather.navigation.DayDetailsRoute
import com.svyd.upcomingweather.navigation.ForecastRoute
import com.svyd.upcomingweather.navigation.SearchRoute

/**
 * The whole app: one host, three destinations, and the state object both screens read from.
 *
 * Everything below this function is stateless — the screens take a state and hand events back,
 * and this is where those events turn into state changes and navigation.
 */
@Composable
fun UpcomingWeatherApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val appState = rememberMockAppState()

    NavHost(
        navController = navController,
        startDestination = ForecastRoute,
        // No inset padding here: every screen paints its background — and its grain — under the
        // system bars, and insets only its content.
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable<ForecastRoute> {
            DashboardScreen(
                state = appState.forecast,
                onLocationClick = appState::traceMySteps,
                onSearchClick = { navController.navigate(SearchRoute) },
                onRefresh = appState::refresh,
                onRetry = appState::retry,
                onDayClick = { date -> navController.navigate(DayDetailsRoute(date)) },
            )
        }

        composable<SearchRoute> {

        }

        composable<DayDetailsRoute> { entry ->
            val route = entry.toRoute<DayDetailsRoute>()
            DayDetailsScreen(
                state = MockForecast.dayDetails(route.date),
                onBack = navController::popBackStack,
            )
        }
    }
}
