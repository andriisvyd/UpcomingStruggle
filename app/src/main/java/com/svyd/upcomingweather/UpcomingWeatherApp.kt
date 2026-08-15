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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import java.time.LocalDate
import com.svyd.upcomingweather.feature.forecast.screen.DashboardScreen
import com.svyd.upcomingweather.feature.forecast.screen.DayDetailsScreen
import com.svyd.upcomingweather.feature.forecast.screen.SplashScreen
import com.svyd.upcomingweather.feature.search.SearchScreen
import com.svyd.upcomingweather.navigation.DayDetailsRoute
import com.svyd.upcomingweather.navigation.ForecastRoute
import com.svyd.upcomingweather.navigation.SearchRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * The whole app: one host, three destinations, behind an opening.
 *
 * Nothing but navigation happens here. Each destination owns its own state, and the choice of city
 * travels between them through the domain rather than through this function.
 *
 * The splash sits outside the host rather than in it: it is not somewhere anyone can navigate to or
 * back to, and there is no transition to speak of — the host replaces it on the frame it finishes.
 * Whether it is finished is kept across configuration changes, so a rotation on the dashboard does
 * not open the app again.
 */
@Composable
fun UpcomingWeatherApp(modifier: Modifier = Modifier) {
    var opened by rememberSaveable { mutableStateOf(false) }
    if (!opened) {
        SplashScreen(onDone = { opened = true }, modifier = modifier)
        return
    }

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
            DashboardScreen(
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
            SearchScreen(
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
            DayDetailsScreen(
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
