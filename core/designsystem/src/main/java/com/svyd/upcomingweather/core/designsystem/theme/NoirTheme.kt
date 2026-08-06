package com.svyd.upcomingweather.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * The one theme. A fixed day/night pair picked by the system dark-mode flag — no dynamic color,
 * no gradients, no elevation.
 */
@Composable
fun UpcomingWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalNoirColors provides if (darkTheme) NightNoirColors else DayNoirColors,
        LocalNoirTypography provides NoirTypography(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) NightColorScheme else DayColorScheme,
            typography = NoirMaterialTypography,
            shapes = NoirShapes,
            content = content,
        )
    }
}

/** Access point for the tokens Material 3 has no room for. */
object NoirTheme {
    val colors: NoirColors
        @Composable @ReadOnlyComposable get() = LocalNoirColors.current

    val inks: NoirInks
        @Composable @ReadOnlyComposable get() = LocalNoirColors.current.inks

    val type: NoirTypography
        @Composable @ReadOnlyComposable get() = LocalNoirTypography.current
}
