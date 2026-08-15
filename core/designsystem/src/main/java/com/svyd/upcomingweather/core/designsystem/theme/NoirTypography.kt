package com.svyd.upcomingweather.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.svyd.upcomingweather.core.designsystem.R

/**
 * One family for the whole app: Courier Prime, bundled.
 *
 * Monospace is doing real work here — figures column-align without any manual alignment, and the
 * typed bars, ledgers and dividers only line up because every character is one cell wide. The
 * system monospace would hold those metrics too, but it is a sans face; the page is supposed to
 * look typed. Courier Prime is OFL — attribution is owed once the app has somewhere to show it.
 */
val NoirFontFamily: FontFamily = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal),
    Font(R.font.courier_prime_bold, FontWeight.Bold),
    Font(R.font.courier_prime_italic, FontWeight.Normal, FontStyle.Italic),
)

private fun noir(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    trackingPercent: Float = 0f,
    italic: Boolean = false,
) = TextStyle(
    fontFamily = NoirFontFamily,
    fontWeight = weight,
    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = (size * trackingPercent).sp,
)

/** Type tokens that have no Material 3 slot to live in. */
@Immutable
data class NoirTypography(
    /** 72 / 76 sp Regular — the hero temperature. */
    val tempDisplay: TextStyle = noir(72, 76),
    /** 18 / 24 sp Bold — app-bar status line. */
    val cityTitle: TextStyle = noir(18, 24, FontWeight.Bold),
    /** 13 / 16 sp Bold UPPERCASE +8% — section headers. */
    val sectionStamp: TextStyle = noir(13, 16, FontWeight.Bold, trackingPercent = 0.08f),
    /** 16 / 22 sp Italic — the voiced line under the hero. */
    val heroLine: TextStyle = noir(16, 22, italic = true),
    /** 12 / 16 sp Bold UPPERCASE +10% — the bordered stamp. */
    val conditionStamp: TextStyle = noir(12, 16, FontWeight.Bold, trackingPercent = 0.10f),
    /** 44 sp Bold — typed glyph, hero slot. */
    val glyphHero: TextStyle = noir(44, 48, FontWeight.Bold),
    /** 16 sp Bold — typed glyph, hour column (28 dp slot). */
    val glyphHour: TextStyle = noir(16, 20, FontWeight.Bold),
    /** 18 sp Medium — typed glyph, nav icon. */
    val glyphNavIcon: TextStyle = noir(18, 20, FontWeight.SemiBold),
    /** 14 sp Bold — typed glyph, day row (24 dp slot). */
    val glyphDay: TextStyle = noir(14, 18, FontWeight.Bold),
    /** 11 / 14 sp Bold UPPERCASE +8% — ledger labels. */
    val readingLabel: TextStyle = noir(11, 14, FontWeight.Bold, trackingPercent = 0.08f),
    /** 16 sp Bold — ledger values. */
    val readingValue: TextStyle = noir(16, 20, FontWeight.Bold),
    /** 14 / 20 sp Bold UPPERCASE +9% — primary actions. */
    val actionPrimary: TextStyle = noir(14, 20, FontWeight.Bold, trackingPercent = 0.09f),
    /** 14 / 20 sp Regular lowercase — secondary actions. */
    val actionSecondary: TextStyle = noir(14, 20),
    /** 10 sp Bold — the typed range bars. */
    val bar: TextStyle = noir(10, 14, FontWeight.Bold),
    /** 12 sp — the typed pipe dividers between hour columns. */
    val divider: TextStyle = noir(12, 23, FontWeight.Medium),
)

internal val NoirMaterialTypography = Typography(
    titleLarge = noir(18, 24, FontWeight.Bold),
    titleMedium = noir(16, 22, FontWeight.Medium),
    bodyLarge = noir(16, 24),
    bodyMedium = noir(14, 20),
    labelLarge = noir(14, 20, FontWeight.Bold),
    labelMedium = noir(12, 16),
    labelSmall = noir(11, 14),
)

internal val LocalNoirTypography = staticCompositionLocalOf { NoirTypography() }
