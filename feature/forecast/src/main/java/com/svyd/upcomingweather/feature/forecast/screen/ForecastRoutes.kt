package com.svyd.upcomingweather.feature.forecast.screen

import android.Manifest
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.upcomingweather.feature.forecast.DayDetailsViewModel
import com.svyd.upcomingweather.feature.forecast.ForecastViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

/**
 * The dashboard, wired to what it draws.
 *
 * Kept apart from [DashboardScreen] so that one stays a function of its arguments — which is what
 * lets every state of it be drawn in a preview.
 */
@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit,
    onDayClick: (date: String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: ForecastViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val activity = LocalActivity.current

    // Only an Activity can show the system prompt, so this is the one part of finding the device
    // that cannot live below the UI. Whatever it answers, the use case is asked again: granted, it
    // succeeds; refused, it comes back as the failure that puts the refusal on screen — carrying
    // whether the platform will still offer the prompt, which is readable only from here.
    val prompt = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
        viewModel.useCurrentLocation(canAskAgain = activity.canAskForLocation())
    }

    DashboardScreen(
        modifier = modifier,
        state = state,
        onLocationClick = {
            viewModel.locationPromptShown()
            prompt.launch(LOCATION_PERMISSIONS)
        },
        onSearchClick = onSearchClick,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::refresh,
        onDayClick = onDayClick,
        onOpenSettings = onOpenSettings,
    )
}

/** One day of that forecast. The date comes from the row that was tapped. */
@Composable
fun DayDetailsRoute(
    date: LocalDate,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val viewModel: DayDetailsViewModel = koinViewModel { parametersOf(date) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    DayDetailsScreen(modifier = modifier, state = state, onBack = onBack)
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
