package com.svyd.upcomingweather

import android.Manifest
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val permissionResult = remember {
        Channel<Boolean>(Channel.BUFFERED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
        scope.launch {
            permissionResult.send(activity.canAskForLocation())
        }
    }

    NavHost(
        navController = navController,
        startDestination = ForecastRoute,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable<ForecastRoute> {
            DashboardRoute(
                onSearchClick = { navController.navigate(SearchRoute) },
                onDayClick = { date -> navController.navigate(DayDetailsRoute(date)) },
                onRequestLocationPermission = {
                    permissionLauncher.launch(LOCATION_PERMISSIONS)
                },
                permissionResult = permissionResult.receiveAsFlow(),
                onOpenSettings = { context.openAppSettings() },
            )
        }

        composable<SearchRoute> {
            SearchDestination(
                onDone = { navController.popBackStack() },
                onBack = navController::popBackStack,
                onRequestLocationPermission = {
                    permissionLauncher.launch(LOCATION_PERMISSIONS)
                    navController.popBackStack()
                },
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

/**
 * Whether the platform will still put the prompt in front of the reader.
 *
 * True after a refusal that can be reversed by asking again, false once the system has stopped
 * offering — which is the only way to tell those two apart, and it needs an Activity to read.
 * Absent an Activity there is nothing to ask with, so nothing is claimed.
 */
private fun Activity?.canAskForLocation(): Boolean =
    this != null && ActivityCompat.shouldShowRequestPermissionRationale(this, COARSE_LOCATION)


/**
 * Both grades are asked for together, which is what puts the precise-or-approximate choice in front
 * of the reader rather than deciding it for them.
 */
private const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

private val LOCATION_PERMISSIONS = arrayOf(
    COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)
