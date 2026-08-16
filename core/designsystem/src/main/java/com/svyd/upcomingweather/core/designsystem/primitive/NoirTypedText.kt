package com.svyd.upcomingweather.core.designsystem.primitive

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.svyd.upcomingweather.core.designsystem.foundation.LocalScreenTransitionScope
import com.svyd.upcomingweather.core.designsystem.foundation.animationsEnabled
import kotlin.random.Random
import kotlin.time.Duration
import kotlinx.coroutines.delay

/**
 * A line that types itself onto the page, a block cursor riding the frontier.
 *
 * The whole string is laid out from the first frame and only the ink moves — the untyped tail is
 * transparent — so nothing reflows as characters land, and the accessibility tree holds the
 * complete line the entire time. The cursor is drawn onto the cell about to be filled rather than
 * typed as a character, which keeps it independent of the glyphs the face happens to carry.
 *
 * The pace is set against a total budget, so a long line types faster instead of making the page
 * wait for it. Per-character jitter and a beat after punctuation keep it off a metronome. With
 * animations off, and in previews, the line is already whole on the first frame.
 *
 * The frontier is the only thing kept, and it is kept under the words it belongs to: a line
 * scrolled out of a list and back is still written, and words that replace it start from nothing.
 *
 * A [startDelay] holds the line back, cursor and all, so a block of them can be made to type top to
 * bottom by the page that lays them out rather than by anything they know about each other. It is
 * spent once. A line that has already written something and is now writing something else is not
 * joining that queue again — it answers straight away, rather than sitting blank for its old turn.
 */
@Composable
fun NoirTypedText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    softWrap: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    startDelay: Duration = Duration.ZERO,
) {
    NoirTypedText(
        text = buildAnnotatedString { append(text) },
        modifier = modifier,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        style = style,
        color = color,
        startDelay = startDelay,
    )
}

@Composable
fun NoirTypedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    softWrap: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    startDelay: Duration = Duration.ZERO,
) {
    val typing = animationsEnabled()
    val cursor = MaterialTheme.colorScheme.primary

    // Saved under a slot named for the words themselves. A restored value is handed back whatever a
    // rememberSaveable is keyed on, so the words have to name the slot rather than only key it:
    // that way a line scrolled out and back finds its own progress, and a line that replaced it
    // cannot find any. The frontier then only ever indexes the string it was saved with.
    var typed by key(text.text) {
        rememberSaveable(typing) { mutableIntStateOf(if (typing) 0 else text.length) }
    }
    var started by remember(text.text) { mutableStateOf(false) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Deliberately outside the key above, so it survives the words changing.
    var everTyped by remember { mutableStateOf(false) }
    val animateChildren = LocalScreenTransitionScope.current?.animateChildren == true

    LaunchedEffect(text.text, typing) {
        // Written already: recycled back into view, or animations are off.
        if (typed >= text.length) return@LaunchedEffect

        // The wait is a place in a queue, and a queue only forms once. It is how a block of these
        // is made to type top to bottom as it is first drawn; words that replace words already
        // written are answering something, and holding the line blank while they wait reads as the
        // page having lost its train of thought.
        if (!everTyped && animateChildren) delay(startDelay)
        everTyped = true

        started = true
        while (typed < text.length) {
            delay(beat(text.text, typed))
            typed++
        }
    }

    Text(
        text = buildAnnotatedString {
            append(text)
            // Laid over the tail rather than wrapped around it: for overlapping spans the later one
            // takes the attributes it sets, so text that colours itself cannot paint back over this.
            addStyle(SpanStyle(color = Color.Transparent), typed, text.length)
        },
        textAlign = textAlign,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        onTextLayout = { layout = it },
        modifier = modifier.drawBehind {
            // One test covers a line still waiting its turn and a line already written: in neither
            // is there a cell for the cursor to sit in.
            val cell = layout
                ?.takeIf { started && typed < it.layoutInput.text.length }
                ?.getBoundingBox(typed)
                ?: return@drawBehind
            drawRect(color = cursor, topLeft = cell.topLeft, size = cell.size)
        },
    )
}

/** How long the cursor rests before filling the cell at [at]. */
private fun beat(text: String, at: Int): Long {
    val even = (BudgetMillis / text.length).coerceIn(MinBeatMillis, MaxBeatMillis)
    val clause = if (at > 0 && text[at - 1] in ClauseEnders) ClauseMillis else 0f
    return ((even * Random.nextDouble(JitterFloor, JitterCeiling)).toLong() + clause).toLong()
}

/** A factor applied equally to all params of animation to scale animation speed */
private const val TimingFactor = 1.4f

/** What a line of average length costs, before jitter and clause rests. */
private const val BudgetMillis = 420 * TimingFactor

/** The bounds the budget is held between: fast enough to stay snappy, slow enough to read as typed. */
private const val MinBeatMillis = 10 * TimingFactor
private const val MaxBeatMillis = 32 * TimingFactor

/** An even beat reads as a machine — every keystroke lands somewhere in this band instead. */
private const val JitterFloor = 0.7 * TimingFactor
private const val JitterCeiling = 1.4 * TimingFactor

/** A typist slows at the end of a clause. */
private const val ClauseMillis = 20 * TimingFactor
private const val ClauseEnders = ",.;:!?"
