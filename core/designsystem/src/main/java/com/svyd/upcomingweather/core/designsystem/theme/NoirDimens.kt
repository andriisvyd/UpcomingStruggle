package com.svyd.upcomingweather.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** The 4 dp grid, named by role rather than by t-shirt size where the spec fixes a value. */
object NoirSpacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp

    /** Screen gutter. */
    val gutter = 16.dp
    val l = 20.dp

    /** Gap between sections. */
    val section = 24.dp

    /** Gap between hour columns — also the width the pipe divider is centered in. */
    val columnGap = 16.dp

    /** Minimum touch target. */
    val touchTarget = 48.dp
    val appBarHeight = 64.dp
}

/**
 * The two-stroke weights in the app. Everything else separates by container color, the M3 way —
 * there are no shadows to fight in the dark theme.
 */
object NoirStroke {
    /** Row dividers, panel and field outlines. */
    val hairline = 1.dp

    /** The condition stamp's border — heavier, so it reads as pressed onto the page. */
    val stamp = 1.5.dp
}

/** Flat panels, no pills: one radius everywhere something is filled or stroked. */
internal val NoirShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(4.dp),
)
