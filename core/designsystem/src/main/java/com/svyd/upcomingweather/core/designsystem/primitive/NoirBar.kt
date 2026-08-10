package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import kotlin.math.roundToInt

/** Renders a [TypedBar] on one line: track in [trackColor], mark in [markColor]. */
@Composable
private fun TypedBarText(
    bar: TypedBar,
    modifier: Modifier = Modifier,
    markColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = NoirBarDefaults.TrackAlpha),
    style: TextStyle = NoirTheme.type.bar,
) {
    val text = remember(bar, markColor, trackColor) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = trackColor)) { append(bar.leading) }
            withStyle(SpanStyle(color = markColor)) { append(bar.body) }
            withStyle(SpanStyle(color = trackColor)) { append(bar.trailing) }
        }
    }
    Text(text = text, style = style, maxLines = 1, softWrap = false, modifier = modifier)
}

@Composable
private fun cellCount(availableWidthPx: Int, minCells: Int): Int {
    val style = NoirTheme.type.bar
    val measurer = rememberTextMeasurer()
    val cellWidth = remember(style, measurer) {
        measurer.measure(TrackChar.toString(), style).size.width
    }
    val bounded = availableWidthPx in 1 until (Int.MAX_VALUE / 2)
    // [minCells] is the fallback for an unbounded measure, never a floor that would push the bar
    // past its own width once the type — and so the cell — grows.
    return if (!bounded || cellWidth <= 0) minCells else (availableWidthPx / cellWidth).coerceAtLeast(1)
}

/**
 * A range bar that fills whatever width it is given: the cell count comes from measuring one
 * character of the bar style against the available width.
 */
@Composable
fun NoirRangeBar(
    startFraction: Float,
    endFraction: Float,
    modifier: Modifier = Modifier,
    markColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = NoirBarDefaults.TrackAlpha),
    minCells: Int = NoirBarDefaults.FallbackCells,
) {
    BoxWithConstraints(modifier) {
        val cells = cellCount(constraints.maxWidth, minCells)
        TypedBarText(
            bar = rangeBar(cells, startFraction, endFraction),
            markColor = markColor,
            trackColor = trackColor,
        )
    }
}

/** The same track carrying a single marker instead of a filled run. */
@Composable
fun NoirMarkerBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    markColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = NoirBarDefaults.TrackAlpha),
    minCells: Int = NoirBarDefaults.FallbackCells,
) {
    BoxWithConstraints(modifier) {
        val cells = cellCount(constraints.maxWidth, minCells)
        TypedBarText(
            bar = markerBar(cells, fraction),
            markColor = markColor,
            trackColor = trackColor,
        )
    }
}

/**
 * A span of the track, quantized to the character grid.
 *
 * Both fractions are 0..1 of the bar's full range. The body is never shorter than one cell, so a
 * zero-width span still reads as a mark rather than vanishing.
 */
internal fun rangeBar(
    cells: Int,
    startFraction: Float,
    endFraction: Float,
    trackChar: Char = TrackChar,
    fillChar: Char = FillChar,
): TypedBar {
    if (cells <= 0) return TypedBar("", "", "")
    val start = startFraction.coerceIn(0f, 1f)
    val end = endFraction.coerceIn(start, 1f)
    val body = ((end - start) * cells).roundToInt().coerceIn(1, cells)
    val leading = (start * cells).roundToInt().coerceIn(0, cells - body)
    return TypedBar(
        leading = trackChar.toString().repeat(leading),
        body = fillChar.toString().repeat(body),
        trailing = trackChar.toString().repeat(cells - leading - body),
    )
}

/** A single position on the track — the details screen's in-day temperature marker. */
internal fun markerBar(
    cells: Int,
    fraction: Float,
    trackChar: Char = TrackChar,
    markerChar: Char = MarkerChar,
): TypedBar = rangeBar(cells, fraction, fraction, trackChar, markerChar)

/** A bar assembled out of characters: [leading] and [trailing] are track, [body] is the mark. */
@Immutable
internal data class TypedBar(
    val leading: String,
    val body: String,
    val trailing: String,
)

/**
 * Tokens for the typed bars.
 *
 * The three characters are design marks, not copy — they are never translated, and they are the
 * reason this app draws no graphics at all.
 */
object NoirBarDefaults {
    const val TrackAlpha = 0.5f

    /** Used only when the bar is measured with an unbounded width. */
    const val FallbackCells = 12
}

private const val TrackChar = '.'
private const val FillChar = '='
private const val MarkerChar = '+'
