package com.svyd.upcomingweather.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The eight inks of the noir palette, in two grades (day / night).
 *
 * They are named after the hue, never after what they end up meaning: this module has no idea
 * that [steel] happens to be rain and [sodium] happens to be a clear sky. Features do that mapping.
 */
@Immutable
data class NoirInks(
    /** Sodium lamp amber. */
    val sodium: Color,
    /** Cold moon steel. */
    val moonSteel: Color,
    /** Grey with just enough green to stay off [graphite]. */
    val greySage: Color,
    /** Neutral graphite — identical to `onSurfaceVariant`. */
    val graphite: Color,
    /** Warm putty. */
    val warmPutty: Color,
    /** Steel blue, reserved for anything wet. */
    val steel: Color,
    /** Teal ice. */
    val tealIce: Color,
    /** Rust. */
    val rust: Color,
)

/** Tokens that have no Material 3 role to live in. */
@Immutable
data class NoirColors(
    val inks: NoirInks,
    /** Accent for precipitation figures — deliberately *not* `primary`, which marks interactivity. */
    val precip: Color,
    /** Opacity of the full-frame film-grain overlay. */
    val grainAlpha: Float,
    val isDark: Boolean,
)

// ---- day · gritty paper -------------------------------------------------------------------

private val DayBackground = Color(0xFFE2DFD7)
private val DaySurface = Color(0xFFEDEAE1)
private val DaySurfaceContainerHighest = Color(0xFFD6D2C6)
private val DayOnSurface = Color(0xFF191813)
private val DayOnSurfaceVariant = Color(0xFF625E52)
private val DayOutlineVariant = Color(0xFFC4C0B3)
private val DayPrimary = Color(0xFF8A5510)
private val DayOnPrimary = Color(0xFFF6EFDF)
private val DayPrecip = Color(0xFF3E6C99)

// ---- night · native -----------------------------------------------------------------------

private val NightBackground = Color(0xFF0B0E13)
private val NightSurface = Color(0xFF12161D)
private val NightSurfaceContainerHighest = Color(0xFF1B212B)
private val NightOnSurface = Color(0xFFE8E6DF)
private val NightOnSurfaceVariant = Color(0xFF98A0AD)
private val NightOutlineVariant = Color(0xFF2E3542)
private val NightPrimary = Color(0xFFE0A458)
private val NightOnPrimary = Color(0xFF241503)
private val NightPrecip = Color(0xFF7FA3C9)

internal val DayColorScheme: ColorScheme = lightColorScheme(
    primary = DayPrimary,
    onPrimary = DayOnPrimary,
    background = DayBackground,
    onBackground = DayOnSurface,
    surface = DaySurface,
    onSurface = DayOnSurface,
    surfaceContainerHighest = DaySurfaceContainerHighest,
    onSurfaceVariant = DayOnSurfaceVariant,
    outlineVariant = DayOutlineVariant,
)

internal val NightColorScheme: ColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightOnPrimary,
    background = NightBackground,
    onBackground = NightOnSurface,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceContainerHighest = NightSurfaceContainerHighest,
    onSurfaceVariant = NightOnSurfaceVariant,
    outlineVariant = NightOutlineVariant,
)

internal val DayNoirColors = NoirColors(
    inks = NoirInks(
        sodium = Color(0xFF8F5B0E),
        moonSteel = Color(0xFF4E6480),
        greySage = Color(0xFF5A665C),
        graphite = Color(0xFF5A5F68),
        warmPutty = Color(0xFF6E6754),
        steel = Color(0xFF3E6C99),
        tealIce = Color(0xFF37707A),
        rust = Color(0xFF9C4A32),
    ),
    precip = DayPrecip,
    grainAlpha = 0.1f,
    isDark = false,
)

internal val NightNoirColors = NoirColors(
    inks = NoirInks(
        sodium = Color(0xFFD9A050),
        moonSteel = Color(0xFF8FA3C0),
        greySage = Color(0xFF9AA89E),
        graphite = Color(0xFF98A0AD),
        warmPutty = Color(0xFFA8A092),
        steel = Color(0xFF7FA3C9),
        tealIce = Color(0xFF86BFC6),
        rust = Color(0xFFC96A4C),
    ),
    precip = NightPrecip,
    grainAlpha = 0.08f,
    isDark = true,
)

internal val LocalNoirColors = staticCompositionLocalOf { DayNoirColors }
