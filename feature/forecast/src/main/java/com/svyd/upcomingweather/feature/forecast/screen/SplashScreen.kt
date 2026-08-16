package com.svyd.upcomingweather.feature.forecast.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.travelsBetweenScreens
import com.svyd.upcomingweather.core.designsystem.preview.NoirScreenPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirBlinkingCursor
import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.designsystem.primitive.NoirConditionGlyph
import com.svyd.upcomingweather.core.designsystem.primitive.NoirGlyphCycle
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.component.OpeningGlyphTravel
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The launch screen: the condition set turning over while the forecast already in storage is read
 * back, coming to rest on the one the dashboard is about to draw.
 *
 * Nothing is fetched here. It subscribes to the dashboard's own model and waits for that model to
 * settle on something — a forecast, an empty page, a refused location — so the dashboard draws that
 * on its first frame instead of its skeleton. A device with no place selected settles just as
 * quickly as one with a warm forecast: having nothing to show is itself an answer.
 *
 * When the answer is a forecast, the cycle stops on that forecast's mark and rests on it for
 * [RestBeforeTravel] before handing over, so what travels to the dashboard is the mark that belongs
 * there and has been seen to belong there. It has to stop first: a mark that flew into place and
 * then changed would read as a mistake being corrected.
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
    var landed by remember { mutableStateOf<NoirCondition?>(null) }

    LaunchedEffect(Unit) {
        val floor = launch { delay(MinimumOnScreen) }
        val settled = viewModel.state.first { it !is ForecastUiState.Loading }
        floor.join()

        landed = (settled as? ForecastUiState.Content)?.hero?.condition
        if (landed != null) delay(RestBeforeTravel)
        onDone()
    }

    SplashScreen(modifier = modifier, landed = landed)
}

/**
 * Kept apart from [SplashScreen] so that what is drawn is a function of its arguments, and can be
 * seen in a preview without a model behind it.
 *
 * [landed] is what the cycle has come to rest on, and null while it is still turning. Only the
 * rested mark carries the shared key: while the set is still going past there is nothing yet that
 * the dashboard could be said to be showing too.
 */
@Composable
private fun SplashScreen(
    modifier: Modifier = Modifier,
    landed: NoirCondition? = null,
) {
    var settled by remember { mutableStateOf<NoirCondition?>(null) }
    NoirBackground(modifier = modifier) {
        val condition = settled
        if (condition == null ) {
            NoirGlyphCycle(
                modifier = Modifier.align(Alignment.Center),
                onCycleEnd = { condition ->
                    if (landed == condition) {
                        settled = condition
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(Modifier.weight(2f))
                NoirConditionGlyph(
                    condition = condition,
                    style = NoirTheme.type.glyphHero,
                    trim = true,
                    modifier = Modifier
                        .travelsBetweenScreens(OpeningGlyphTravel),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = NoirSpacing.m),
                ) {
                    NoirBlinkingCursor(
                        invertedBlink = true,
                        width = 44.sp,
                        height = 54.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** A full turn of the nine marks. Below this the screen reads as a flash rather than an opening. */
private val MinimumOnScreen: Duration = 3.seconds

/**
 * How long the rested mark is held before it travels.
 *
 * Long enough to be read as a mark rather than as the last frame of the cycle — the answer arrives
 * here, and it is the one thing the splash has to say.
 */
private val RestBeforeTravel: Duration = 1.9.seconds

@NoirScreenPreviews
@Composable
private fun SplashScreenPreview() {
    UpcomingWeatherTheme {
        SplashScreen(landed = NoirCondition.Thunder)
    }
}
