package io.github.samson0720.cosmosmessenger.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icons.Cosmos — custom icon namespace for the app. Mirrors the
 * Icons.Filled / Icons.Outlined pattern so call sites read naturally.
 */
object Cosmos

val Icons.Cosmos: Cosmos get() = io.github.samson0720.cosmosmessenger.navigation.Cosmos

private var _saturn: ImageVector? = null

/**
 * Saturn — a filled disk with a stroked ring tilted ~20°. Source colors are black so the
 * parent Icon composable can tint it with the current content color (tab selected/unselected).
 */
val Cosmos.Saturn: ImageVector
    get() {
        _saturn?.let { return it }
        val image = ImageVector.Builder(
            name = "Cosmos.Saturn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // Ring — ellipse centered at (12,12) rx=10 ry=3, tilted.
            group(rotate = -20f, pivotX = 12f, pivotY = 12f) {
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.5f,
                    strokeLineCap = StrokeCap.Round,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(22f, 12f)
                    arcToRelative(10f, 3f, 0f, true, false, -20f, 0f)
                    arcToRelative(10f, 3f, 0f, true, false, 20f, 0f)
                    close()
                }
            }
            // Planet — filled circle centered at (12,12), radius 5.
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(17f, 12f)
                arcToRelative(5f, 5f, 0f, true, false, -10f, 0f)
                arcToRelative(5f, 5f, 0f, true, false, 10f, 0f)
                close()
            }
        }.build()
        _saturn = image
        return image
    }
