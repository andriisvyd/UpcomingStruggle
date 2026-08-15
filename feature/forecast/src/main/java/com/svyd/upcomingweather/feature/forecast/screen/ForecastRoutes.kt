package com.svyd.upcomingweather.feature.forecast.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svyd.upcomingweather.feature.forecast.DayDetailsViewModel
import com.svyd.upcomingweather.feature.forecast.ForecastViewModel
import kotlinx.coroutines.flow.Flow
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
    onRequestLocationPermission: () -> Unit,
    permissionResult: Flow<Boolean>,
    onOpenSettings: () -> Unit,
) {
    val viewModel: ForecastViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = permissionResult) {
        permissionResult.collect { canAskAgain ->
            viewModel.useCurrentLocation(canAskAgain = canAskAgain)
        }
    }

    DashboardScreen(
        modifier = modifier,
        state = state,
        onLocationClick = onRequestLocationPermission,
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
