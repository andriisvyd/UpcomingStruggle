package com.svyd.upcomingweather.core.designsystem.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Grows a fixed dimension with the user's font setting.
 *
 * For boxes that exist to hold text — an hour column, a bar, a field. A dp that never moves while
 * the text inside it doubles is a clipped layout at 200%.
 */
@Composable
@ReadOnlyComposable
fun Dp.scaledByFont(max: Float = 2f): Dp = this * LocalDensity.current.fontScale.coerceIn(1f, max)

/**
 * True once type has grown far enough that a dense row cannot hold its cells side by side.
 *
 * Layouts that answer `true` here reflow rather than shrink: nothing is dropped except marks that
 * are decorative, and every figure stays on screen.
 */
@Composable
@ReadOnlyComposable
fun largeFontScale(threshold: Float = 1.4f): Boolean = LocalDensity.current.fontScale >= threshold
