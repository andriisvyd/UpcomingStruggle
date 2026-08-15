package com.svyd.upcomingweather.feature.forecast.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.svyd.upcomingweather.feature.forecast.ForecastViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The forecast model, held by the activity rather than by whatever composed it.
 *
 * The splash and the dashboard are two different places in the tree that have to be looking at the
 * same model: the splash subscribes first and waits for a state to settle, and the dashboard finds
 * that state already there on its first frame. Left to the default owner each would get its own
 * instance, and the dashboard would start again from Loading — the flicker the splash exists to
 * remove.
 */
@Composable
internal fun forecastViewModel(): ForecastViewModel =
    koinViewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
