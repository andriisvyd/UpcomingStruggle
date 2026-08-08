package com.svyd.upcomingweather.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Navigation and action tools, drawn Material Symbols Sharp style — 2 dp stroke,
 * butt caps, mitered joins. Weather is never an icon in this app; it is typed characters.
 */
object NoirIcons {

    val MyLocation: ImageVector by lazy {
        strokeIcon("MyLocation") {
            circle(12f, 12f, 6.5f)
            circle(12f, 12f, 1.4f)
            moveTo(12f, 2.2f); verticalLineToRelative(3.3f)
            moveTo(12f, 18.5f); verticalLineToRelative(3.3f)
            moveTo(2.2f, 12f); horizontalLineToRelative(3.3f)
            moveTo(18.5f, 12f); horizontalLineToRelative(3.3f)
        }
    }

    val Search: ImageVector by lazy {
        strokeIcon("Search") {
            circle(11f, 11f, 7f)
            moveTo(20.5f, 20.5f); lineTo(16.9f, 16.9f)
        }
    }

    val Back: ImageVector by lazy {
        strokeIcon("Back") {
            moveTo(19f, 12f); horizontalLineTo(5f)
            moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
        }
    }

    val Close: ImageVector by lazy {
        strokeIcon("Close") {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Recent: ImageVector by lazy {
        strokeIcon("Recent") {
            circle(12f, 12f, 8f)
            moveTo(12f, 7.5f); verticalLineTo(12f); lineTo(15f, 13.8f)
        }
    }

    val CloudOff: ImageVector by lazy {
        strokeIcon("CloudOff") {
            moveTo(18f, 10f)
            horizontalLineToRelative(-1.26f)
            arcTo(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 9f, y1 = 20f)
            horizontalLineToRelative(9f)
            arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 0f, dy1 = -10f)
            close()
            moveTo(4f, 3.5f); lineTo(20f, 20.5f)
        }
    }
}

private fun strokeIcon(name: String, strokes: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            pathBuilder = strokes,
        )
    }.build()

private fun PathBuilder.circle(cx: Float, cy: Float, radius: Float) {
    moveTo(cx - radius, cy)
    arcToRelative(radius, radius, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = radius * 2, dy1 = 0f)
    arcToRelative(radius, radius, 0f, isMoreThanHalf = true, isPositiveArc = true, dx1 = -radius * 2, dy1 = 0f)
    close()
}
