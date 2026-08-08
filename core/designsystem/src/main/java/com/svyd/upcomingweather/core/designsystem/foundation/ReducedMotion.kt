package com.svyd.upcomingweather.core.designsystem.foundation

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * False when the user has turned animations off system-wide, and in previews, which render a
 * single frame.
 *
 * Decorative motion checks this and settles on a still frame rather than disappearing: a mark
 * that carries meaning stays on screen, it just stops moving.
 */
@Composable
fun animationsEnabled(): Boolean {
    if (LocalInspectionMode.current) return false
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}
