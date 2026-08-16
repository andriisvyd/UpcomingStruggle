package com.svyd.upcomingweather.core.designsystem.foundation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

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
const val TravelMillis = 490

/** One element's slide. */
private const val SlideMillis = 420

/** The step between one element starting and the next. A beat, not a queue. */
private const val StaggerMillis = 120
