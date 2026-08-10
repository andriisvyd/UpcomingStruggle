package com.svyd.upcomingweather.feature.search

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
import org.koin.androidx.compose.koinViewModel

/**
 * Search, wired to what it draws.
 *
 * Picking a city is not a navigation result: the choice is written down and the forecast screen
 * finds out by observing, so all this hands back is the instruction to close.
 */
@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    val prompt = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
        viewModel.useCurrentLocation(canAskAgain = activity.canAskForLocation(), then = onDone)
    }

    SearchScreen(
        modifier = modifier,
        state = state,
        onQueryChange = viewModel::query,
        onClearQuery = viewModel::clearQuery,
        onBack = onBack,
        onCitySelected = { city -> viewModel.select(city, then = onDone) },
        onUseCurrentLocation = {
            prompt.launch(LOCATION_PERMISSIONS)
        },
        onOpenSettings = onOpenSettings,
        onRetry = viewModel::retry,
    )
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

private const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

private val LOCATION_PERMISSIONS = arrayOf(
    COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)
