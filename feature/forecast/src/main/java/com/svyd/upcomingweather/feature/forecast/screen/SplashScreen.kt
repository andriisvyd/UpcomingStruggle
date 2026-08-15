package com.svyd.upcomingweather.feature.forecast.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.preview.NoirScreenPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirGlyphCycle
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The launch screen: the condition set turning over while the forecast already in storage is read
 * back.
 *
 * Nothing is fetched here. It subscribes to the dashboard's own model and waits for that model to
 * settle on something — a forecast, an empty page, a refused location — so the dashboard draws that
 * on its first frame instead of its skeleton. A device with no place selected settles just as
 * quickly as one with a warm forecast: having nothing to show is itself an answer.
 *
 * Two bounds keep it from either flashing or trapping: [MinimumOnScreen] holds it up for a full
 * turn of the marks when storage answers at once, and [SettleTimeout] hands over to the dashboard
 * regardless when a cold fetch does not land — the dashboard has its own way of saying it is
 * working.
 */
@Composable
fun SplashScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = forecastViewModel()

    LaunchedEffect(Unit) {
        val floor = launch { delay(MinimumOnScreen) }
        withTimeoutOrNull(SettleTimeout) {
            viewModel.state.first { it !is ForecastUiState.Loading }
        }
        floor.join()
        onDone()
    }

    SplashScreen(modifier = modifier)
}

/**
 * Kept apart from [SplashScreen] so that what is drawn is a function of nothing at all, and can be
 * seen in a preview without a model behind it.
 */
@Composable
private fun SplashScreen(modifier: Modifier = Modifier) {
    NoirBackground(modifier = modifier) {
        NoirGlyphCycle(modifier = Modifier.align(Alignment.Center))
    }
}

/** A full turn of the nine marks. Below this the screen reads as a flash rather than an opening. */
private val MinimumOnScreen: Duration = 2.seconds

/** How long a fetch is waited on before the dashboard takes over the waiting. */
private val SettleTimeout: Duration = 3.seconds

@NoirScreenPreviews
@Composable
private fun SplashScreenPreview() {
    UpcomingWeatherTheme {
        SplashScreen()
    }
}
