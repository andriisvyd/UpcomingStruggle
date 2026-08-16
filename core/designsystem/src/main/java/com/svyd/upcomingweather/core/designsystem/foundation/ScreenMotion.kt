package com.svyd.upcomingweather.core.designsystem.foundation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import kotlin.math.hypot
import kotlin.math.max

/**
 * The two scopes a screen change runs in, put where the pieces that move can reach them.
 *
 * They are set by the navigation host and read by whatever draws — a glyph deep inside a component
 * cannot be handed them as arguments without every layout between growing a parameter it does not
 * otherwise want, and the previews of those layouts growing one too. Both are null wherever no
 * screen change is running, which is what makes every helper below a no-op in a preview.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalScreenTransitionScope = compositionLocalOf<ScreenTransitionScope?> { null }

data class ScreenTransitionScope(
    val scope: AnimatedVisibilityScope,
    val animateChildren: Boolean,
)

/**
 * A journey one element makes between two screens: what names it as the same element on both sides,
 * and the pace it travels at.
 *
 * The curve is not part of it. Every such journey here runs from lower left to upper right — a mark
 * somewhere in the page rising to the place that names it — so the arc is settled once, and only
 * how long it takes is a choice the two ends make together.
 */
@Immutable
data class ScreenTravel(
    val key: Any,
    val durationMillis: Int = TravelMillis,
    val easing: Easing = FastOutSlowInEasing,
)

/**
 * Marks this as the same thing on both sides of a screen change, under the name [travel] carries,
 * so it travels to its new place instead of one copy leaving and another arriving.
 *
 * It travels along an arc rather than a straight line. A mark crossing the page corner to corner in
 * a straight line reads as a thing being moved; the same mark on a curve reads as a thing being
 * put down, which is what is happening — the set stops turning over and the one that came up is
 * set in its place.
 *
 * It accelerates the whole way. The mark has been at rest and is leaving, so the slow part belongs
 * at the start; easing it out at the far end would make it hesitate over the place it is going.
 *
 * A name with nothing to match on the far side is not an error: the element simply enters and exits
 * on its own terms, which is what happens when the screen it was going to lands on a state that
 * does not draw it. A null [travel] says the same thing more plainly — this element is not going
 * anywhere, which is every preview and every screen not currently changing.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalAnimationSpecApi::class)
@Composable
fun Modifier.travelsBetweenScreens(travel: ScreenTravel?): Modifier {
    val moving = animationsEnabled()
    val shared = LocalSharedTransitionScope.current
    val screen = LocalScreenTransitionScope.current
    if (!moving || travel == null || shared == null || screen == null) return this
    return with(shared) {
        this@travelsBetweenScreens
            .sharedElement(
                rememberSharedContentState(key = travel.key),
                screen.scope,
                boundsTransform = { from, to ->
                    keyframes {
                        durationMillis = travel.durationMillis
                        from at 0 using ArcMode.ArcBelow using travel.easing
                        to at travel.durationMillis
                    }
                },
            )
            // The ends are not always the same size — a mark set for a row and the same mark set
            // for a hero are three times apart. Laying out at the size it is going to and letting
            // the bounds carry the difference is what makes that a scale rather than a snap.
            .skipToLookaheadSize()
    }
}

/**
 * Opens as a circle grown from the top-end glyph slot, and closes into the top-start one.
 *
 * The circle starts where the bar's action sits, because that is where the reader's finger was: the
 * page is uncovered from under the thing that was tapped rather than pushed in from an edge nobody
 * touched. Everything outside the circle is left unpainted, so what is underneath is the page this
 * one was opened from.
 *
 * It closes into the other slot, where the way back is, and not back into the one it came out of.
 * Both slots hold the same thing — the reader's way through — and the page opening from one and
 * closing into the other is that way through drawn twice, out and back. Which slot is which is
 * settled by the bar rather than by what was tapped: every way off the page ends the same, and a
 * screen change that reported which row was chosen would be reporting the next screen's news.
 *
 * Applied by the navigation host to the screen it opens, not by the screen: how a page is arrived at
 * is the host's business, and a page that decided it for itself could not be drawn in a preview.
 */
@Composable
fun Modifier.opensAsCircle(): Modifier {
    val moving = animationsEnabled()
    val screen = LocalScreenTransitionScope.current?.scope
    if (!moving || screen == null) return this

    val fromTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() +
        NoirSpacing.appBarHeight / 2
    val fromSide = NoirSpacing.s + NoirSpacing.touchTarget / 2
    val open by screen.transition.animateFloat(
        transitionSpec = { tween(RevealMillis, easing = FastOutSlowInEasing) },
        label = "circularReveal",
    ) { state -> if (state == EnterExitState.Visible) 1f else 0f }
    // Settled the moment the page is on its way out, while the circle still covers the screen, so
    // the side it is drawn from changes where nothing can be seen to change.
    val leaving = screen.transition.targetState == EnterExitState.PostExit

    return drawWithCache {
        val side = if (leaving) fromSide.toPx() else size.width - fromSide.toPx()
        val origin = Offset(x = side, y = fromTop.toPx())
        // The corner furthest from the origin is the last thing the circle has to reach.
        val reach = hypot(
            max(origin.x, size.width - origin.x),
            max(origin.y, size.height - origin.y),
        )
        val circle = Path()
        onDrawWithContent {
            circle.rewind()
            circle.addOval(Rect(center = origin, radius = reach * open))
            clipPath(circle) { this@onDrawWithContent.drawContent() }
        }
    }
}

/** Arrives from below its own place, [order] steps down the stagger. */
@Composable
fun Modifier.arrivesFromBelow(order: Int): Modifier = arrives(order) { delay ->
    slideInVertically(animationSpec = slide(delay)) { height -> height }
}

/** Arrives from past the trailing edge, [order] steps down the stagger. */
@Composable
fun Modifier.arrivesFromEnd(order: Int): Modifier = arrives(order) { delay ->
    slideInHorizontally(animationSpec = slide(delay)) { width -> width }
}

/**
 * Only the arrival is animated. What leaves is gone the moment the screen behind it is drawn —
 * holding a departing page on screen to watch it go is the slowest thing an app can do.
 */
@Composable
private fun Modifier.arrives(order: Int, enter: (delayMillis: Int) -> EnterTransition): Modifier {
    val moving = animationsEnabled()
    val screen = LocalScreenTransitionScope.current?.scope
    val animateChildren = LocalScreenTransitionScope.current?.animateChildren == true
    if (!moving || screen == null || !animateChildren) return this
    return with(screen) {
        this@arrives.animateEnterExit(
            enter = enter(order * StaggerMillis),
            exit = ExitTransition.None,
        )
    }
}

private fun slide(delayMillis: Int) =
    tween<IntOffset>(SlideMillis, delayMillis = delayMillis, easing = LinearOutSlowInEasing)

/** Long enough to read as travel, short enough that nobody waits on it. */
const val TravelMillis = 520

/** One element's slide. */
private const val SlideMillis = 420

/** The step between one element starting and the next. A beat, not a queue. */
private const val StaggerMillis = 120

/** The circle crossing the page. Longer than a slide: it has a diagonal to cover. */
private const val RevealMillis = 400


