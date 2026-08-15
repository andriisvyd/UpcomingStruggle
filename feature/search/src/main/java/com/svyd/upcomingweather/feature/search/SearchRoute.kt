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
    onRequestLocationPermission: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SearchScreen(
        modifier = modifier,
        state = state,
        onQueryChange = viewModel::query,
        onClearQuery = viewModel::clearQuery,
        onBack = onBack,
        onCitySelected = { city -> viewModel.select(city, then = onDone) },
        onUseCurrentLocation = onRequestLocationPermission,
        onOpenSettings = onOpenSettings,
        onRetry = viewModel::retry,
    )
}
