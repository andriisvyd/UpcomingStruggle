package com.svyd.upcomingweather

import android.Manifest
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import java.time.LocalDate
import com.svyd.upcomingweather.feature.forecast.screen.DashboardScreen
import com.svyd.upcomingweather.feature.forecast.screen.DayDetailsScreen
import com.svyd.upcomingweather.feature.forecast.screen.SplashScreen
import com.svyd.upcomingweather.feature.search.SearchScreen
import com.svyd.upcomingweather.core.designsystem.foundation.LocalScreenTransitionScope
import com.svyd.upcomingweather.core.designsystem.foundation.LocalSharedTransitionScope
import com.svyd.upcomingweather.core.designsystem.foundation.ScreenTransitionScope
import com.svyd.upcomingweather.core.designsystem.foundation.opensAsCircle
import com.svyd.upcomingweather.navigation.DayDetailsRoute
import com.svyd.upcomingweather.navigation.ForecastRoute
import com.svyd.upcomingweather.navigation.SearchRoute
import com.svyd.upcomingweather.navigation.SplashRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * The whole app: one host, four destinations.
 *
 * Nothing but navigation happens here. Each destination owns its own state, and the choice of city
 * travels between them through the domain rather than through this function.
 *
 * Screens slide; nothing fades. A page on its way out is not worth watching, so it leaves at the
 * pace it is pushed and the arriving one sets the timing. The splash is a destination rather than a
 * gate in front of the host, because handing the glyph over to the dashboard needs both screens
 * inside one [SharedTransitionLayout] and the scope a `composable` gives its content.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
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

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = SplashRoute,
                enterTransition = { slideInHorizontally(SlideSpec) { width -> width } },
                exitTransition = { slideOutHorizontally(SlideSpec) { width -> -width / 4 } },
                popEnterTransition = { slideInHorizontally(SlideSpec) { width -> -width / 4 } },
                popExitTransition = { slideOutHorizontally(SlideSpec) { width -> width } },
            ) {
                // Both ends are still: the glyph travelling to its place on the dashboard is the
                // whole transition, and a page sliding under it would only be something else to
                // watch. What the dashboard does with the rest of its content is the dashboard's.

                composable<SplashRoute>(exitTransition = { ExitTransition.None }) {
                    CompositionLocalProvider(
                        LocalScreenTransitionScope provides ScreenTransitionScope(
                            scope = this,
                            animateChildren = false,
                        )
                    ) {
                        SplashScreen(
                            onDone = {
                                navController.navigate(ForecastRoute) {
                                    popUpTo(SplashRoute) { inclusive = true }
                                }
                            },
                        )
                    }
                }

                composable<ForecastRoute>(
                    enterTransition = { EnterTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    // Search opens as a circle over this page: a page sliding out from under the
                    // circle would be a second thing to watch. A day still takes the parallax.
                    exitTransition = {
                        if (targetState.destination.hasRoute<SearchRoute>()) {
                            ExitTransition.None
                        } else {
                            slideOutHorizontally(SlideSpec) { width -> -width / 4 }
                        }
                    },
                ) {
                    CompositionLocalProvider(
                        LocalScreenTransitionScope provides ScreenTransitionScope(
                            scope = this,
                            animateChildren = firstVisit(),
                        )
                    ) {
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
                }

                // The circle is the whole transition, in both directions: nothing slides, and the
                // page is held on screen on the way out by the circle still closing.
                composable<SearchRoute>(
                    enterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
                ) {
                    CompositionLocalProvider(
                        LocalScreenTransitionScope provides ScreenTransitionScope(
                            scope = this,
                            // The rows here arrive on the beat their own list sets, not on the
                            // screen change. The scope is what the circle is cut from, and it is
                            // here for nothing else.
                            animateChildren = false,
                        )
                    ) {
                        SearchScreen(
                            modifier = Modifier.opensAsCircle(),
                            onDone = { navController.popBackStack() },
                            onBack = navController::popBackStack,
                            onRequestLocationPermission = {
                                permissionLauncher.launch(LOCATION_PERMISSIONS)
                                navController.popBackStack()
                            },
                            onOpenSettings = { context.openAppSettings() },
                        )
                    }
                }

                composable<DayDetailsRoute> { entry ->
                    val route = entry.toRoute<DayDetailsRoute>()
                    CompositionLocalProvider(
                        LocalScreenTransitionScope provides ScreenTransitionScope(
                            scope = this,
                            animateChildren = true,
                        )
                    ) {
                        DayDetailsScreen(
                            date = LocalDate.parse(route.date),
                            onBack = navController::popBackStack,
                        )
                    }
                }
            }
        }
    }
}

/**
 * True the first time a destination is drawn, false every time it is returned to.
 *
 * "Did this come from the splash" is the wrong question to ask the back stack, because the splash
 * is popped inclusive the moment it hands over — by the time the dashboard is drawn there is no
 * previous entry to name. It is also more than is being asked: a page assembles itself once, and
 * whether the thing before it was a splash or something added later is beside the point.
 *
 * Kept in the destination's own saved state, so it survives everything that leaves and re-enters
 * this composition while the entry stays on the stack: a trip to a day, and a rotation.
 */
@Composable
private fun firstVisit(): Boolean {
    val visited = rememberSaveable { mutableStateOf(false) }
    // Read once. Recording the visit must not cut short the arrival it is recording.
    val first = remember { !visited.value }
    SideEffect { visited.value = true }
    return first
}

/** One page's worth of travel. Short enough that a tap and its answer read as one gesture. */
private val SlideSpec = tween<IntOffset>(durationMillis = 220, easing = FastOutSlowInEasing)

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
