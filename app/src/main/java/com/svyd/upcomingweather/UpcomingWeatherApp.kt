package com.svyd.upcomingweather

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import java.time.LocalDate
import com.svyd.upcomingweather.feature.forecast.screen.DashboardRoute
import com.svyd.upcomingweather.feature.forecast.screen.DayDetailsRoute
import com.svyd.upcomingweather.feature.search.SearchRoute as SearchDestination
import com.svyd.upcomingweather.navigation.DayDetailsRoute
import com.svyd.upcomingweather.navigation.ForecastRoute
import com.svyd.upcomingweather.navigation.SearchRoute

/**
 * The whole app: one host, three destinations.
 *
 * Nothing but navigation happens here. Each destination owns its own state, and the choice of city
 * travels between them through the domain rather than through this function.
 */
@Composable
fun UpcomingWeatherApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

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
            DashboardRoute(
                onSearchClick = { navController.navigate(SearchRoute) },
                onDayClick = { date -> navController.navigate(DayDetailsRoute(date)) },
                onOpenSettings = { context.openAppSettings() },
            )
        }

        composable<SearchRoute> {
            SearchDestination(
                onDone = { navController.popBackStack() },
                onBack = navController::popBackStack,
                onOpenSettings = { context.openAppSettings() },
            )
        }

        composable<DayDetailsRoute> { entry ->
            val route = entry.toRoute<DayDetailsRoute>()
            DayDetailsRoute(
                date = LocalDate.parse(route.date),
                onBack = navController::popBackStack,
            )
        }
    }
}
