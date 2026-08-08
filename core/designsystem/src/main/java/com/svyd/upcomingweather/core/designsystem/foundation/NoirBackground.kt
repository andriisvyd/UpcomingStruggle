package com.svyd.upcomingweather.core.designsystem.foundation

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import kotlin.random.Random

private const val GRAIN_TILE = 140

/**
 * A static fractal-ish noise tile. Generated once from a fixed seed, so it is the same grain on
 * every launch and every preview — and never animated.
 */
private val grainTile: ImageBitmap by lazy {
    val random = Random(seed = 1949)
    val pixels = IntArray(GRAIN_TILE * GRAIN_TILE) {
        val v = random.nextInt(from = 0, until = 256)
        (0xFF shl 24) or (v shl 16) or (v shl 8) or v
    }
    Bitmap.createBitmap(pixels, GRAIN_TILE, GRAIN_TILE, Bitmap.Config.ARGB_8888).asImageBitmap()
}

/** Lays the film grain over everything already drawn. */
fun Modifier.filmGrain(alpha: Float): Modifier = this.drawWithCache {
    val brush = ShaderBrush(ImageShader(grainTile, TileMode.Repeated, TileMode.Repeated))
    onDrawWithContent {
        drawContent()
        drawRect(brush = brush, alpha = alpha)
    }
}

/**
 * The root of every screen: the flat background color with the grain on top of the content.
 * There are no other containers in this app — everything else is typed straight onto this.
 *
 * Edge to edge, always, and it holds no insets: the page runs under the status and navigation
 * bars so the grain has no seam. Clearing those bars is the job of the pieces that have to —
 * [com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar] insets itself, and scroll
 * containers take [NoirInsetDefaults.scrollableContentPadding].
 */
@Composable
fun NoirBackground(
    modifier: Modifier = Modifier,
    drawGrain: Boolean = true,
    color: Color = MaterialTheme.colorScheme.background,
    content: @Composable BoxScope.() -> Unit,
) {
    val grainAlpha = NoirTheme.colors.grainAlpha
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .then(remember(grainAlpha) {
                if (drawGrain) Modifier.filmGrain(grainAlpha)
                else Modifier
            }),
    ) {
        // Text that states no colour of its own reads this instead of the black default.
        CompositionLocalProvider(LocalContentColor provides contentColorFor(color)) {
            content()
        }
    }
}
