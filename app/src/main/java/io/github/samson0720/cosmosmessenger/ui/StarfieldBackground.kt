package io.github.samson0720.cosmosmessenger.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Full-bleed decorative starfield. Layers from back to front:
 *   1. Galaxy band — a faint diagonal haze across the mid-right.
 *   2. Four corner nebulae — soft radial glows in alternating violet / cyan.
 *   3. ~100 twinkling stars.
 *   4. Occasional travelling meteor.
 * Decoration only: not interactive, no semantics.
 */
@Composable
fun StarfieldBackground(modifier: Modifier = Modifier) {
    val stars = remember { generateStars(count = 100) }
    val nebulae = remember { generateNebulae() }

    // Single monotonically-increasing clock drives all 100 stars. Reading it inside the
    // Canvas lambda (draw phase) triggers redraws without composition churn, and each
    // star's per-frame alpha is computed from (clock, duration, phase) — avoiding 100
    // independent animateFloat subscriptions.
    val clockTransition = rememberInfiniteTransition(label = "starfield-clock")
    val clockSeconds = clockTransition.animateFloat(
        initialValue = 0f,
        targetValue = STAR_CLOCK_CYCLE_SECONDS,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (STAR_CLOCK_CYCLE_SECONDS * 1000).toInt(),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "starfield-clock-value",
    )

    val meteorProgress = remember { Animatable(0f) }
    var meteor by remember { mutableStateOf<Meteor?>(null) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(Random.nextLong(MeteorMinIntervalMs, MeteorMaxIntervalMs))
            val shot = Meteor(
                originFracX = Random.nextFloat() * 0.6f,
                originFracY = Random.nextFloat() * 0.4f,
                travelDistanceDp = (300 + Random.nextInt(101)).dp,   // 300..400 dp
                tailLengthDp = (80 + Random.nextInt(41)).dp,          // 80..120 dp
            )
            meteor = shot
            meteorProgress.snapTo(0f)
            meteorProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = Random.nextInt(600, 901),        // 600..900 ms
                    easing = LinearEasing,
                ),
            )
            meteor = null
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // 1) Galaxy band — a full-canvas drawRect filled with a linearGradient whose
        // axis runs perpendicular to the band. Clamp mode keeps it transparent outside
        // the band, and the gradient's soft stops mean there are no visible edges.
        // Two overlapping bands: a wider haze (120dp) and a narrow brighter core (40dp).
        val axisStart = Offset(0f, size.height * 0.8f)
        val axisEnd = Offset(size.width, size.height * 0.2f)
        val bandCenter = Offset(
            x = (axisStart.x + axisEnd.x) * 0.5f,
            y = (axisStart.y + axisEnd.y) * 0.5f,
        )
        val axisDx = axisEnd.x - axisStart.x
        val axisDy = axisEnd.y - axisStart.y
        val axisLen = sqrt(axisDx * axisDx + axisDy * axisDy)
        // Unit perpendicular to the axis (axis rotated 90°).
        val perpX = axisDy / axisLen
        val perpY = -axisDx / axisLen

        fun bandGradient(halfWidthPx: Float, coreColor: Color): Brush = Brush.linearGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                0.5f to coreColor,
                1f to Color.Transparent,
            ),
            start = Offset(
                bandCenter.x - perpX * halfWidthPx,
                bandCenter.y - perpY * halfWidthPx,
            ),
            end = Offset(
                bandCenter.x + perpX * halfWidthPx,
                bandCenter.y + perpY * halfWidthPx,
            ),
        )

        drawRect(
            brush = bandGradient(halfWidthPx = 60.dp.toPx(), coreColor = Color(0x08FFFFFF)),
            topLeft = Offset.Zero,
            size = size,
        )
        drawRect(
            brush = bandGradient(halfWidthPx = 20.dp.toPx(), coreColor = Color(0x0EFFFFFF)),
            topLeft = Offset.Zero,
            size = size,
        )

        // 2) Four corner nebulae — soft radial falloff stands in for a Gaussian blur and
        // is rendering-correct on every backend (BlurMaskFilter is unreliable on HW canvas).
        nebulae.forEach { n ->
            val center = Offset(n.fracX * size.width, n.fracY * size.height)
            val radiusPx = n.radiusDp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(n.color.copy(alpha = n.alpha), Color.Transparent),
                    center = center,
                    radius = radiusPx,
                ),
                radius = radiusPx,
                center = center,
            )
        }

        // 3) Stars. Alpha mapped to [0.2, 1.0] via (sin + 1) / 2 → [0, 1] then rescaled.
        val now = clockSeconds.value
        stars.forEach { star ->
            val phase = (now / star.durationSeconds + star.phaseOffset) * TWO_PI
            val normalized = (sin(phase) + 1f) * 0.5f
            val alpha = 0.2f + normalized * 0.8f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.sizeDp.toPx() / 2f,
                center = Offset(star.fracX * size.width, star.fracY * size.height),
            )
        }

        // 4) Meteor — head slides from origin along (cos35°, sin35°); tail lags behind by
        // a fixed dp length. Alpha piecewise-linear against progress so a shot starts soft,
        // holds bright across the arc, and dims before disappearing.
        meteor?.let { m ->
            val p = meteorProgress.value
            val alpha = when {
                p < 0.1f -> p / 0.1f
                p < 0.9f -> 1f
                else -> (1f - p) / 0.1f
            }.coerceIn(0f, 1f)
            val dirX = cos(MeteorAngleRadians)
            val dirY = sin(MeteorAngleRadians)
            val travelPx = m.travelDistanceDp.toPx()
            val tailPx = m.tailLengthDp.toPx()
            val headX = m.originFracX * size.width + p * travelPx * dirX
            val headY = m.originFracY * size.height + p * travelPx * dirY
            val tailStartX = headX - tailPx * dirX
            val tailStartY = headY - tailPx * dirY
            val headOffset = Offset(headX, headY)
            val tailOffset = Offset(tailStartX, tailStartY)
            drawLine(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color.White.copy(alpha = 0f),
                        1f to Color.White.copy(alpha = 0.9f * alpha),
                    ),
                    start = tailOffset,
                    end = headOffset,
                ),
                start = tailOffset,
                end = headOffset,
                strokeWidth = 1.5.dp.toPx(),
            )
        }
    }
}

private data class Star(
    val fracX: Float,
    val fracY: Float,
    val sizeDp: Dp,
    val durationSeconds: Float,
    val phaseOffset: Float,
)

private data class Nebula(
    val fracX: Float,
    val fracY: Float,
    val radiusDp: Dp,
    val color: Color,
    val alpha: Float,
)

private data class Meteor(
    val originFracX: Float,
    val originFracY: Float,
    val travelDistanceDp: Dp,
    val tailLengthDp: Dp,
)

private fun generateStars(count: Int): List<Star> {
    // Size distribution ~6:3:1 for small / medium / large
    val sizes = listOf(0.8.dp, 1.5.dp, 2.5.dp)
    val weights = intArrayOf(6, 3, 1)
    val pool = buildList {
        sizes.forEachIndexed { index, size -> repeat(weights[index]) { add(size) } }
    }
    return List(count) {
        Star(
            fracX = Random.nextFloat(),
            fracY = Random.nextFloat(),
            sizeDp = pool.random(),
            durationSeconds = 2f + Random.nextFloat() * 3f,
            phaseOffset = Random.nextFloat(),
        )
    }
}

private fun generateNebulae(): List<Nebula> {
    // Four corners, alternating violet / cyan. Radius 80..140 dp, alpha 0.06..0.12.
    val violet = Color(0xFF6C5CE7)
    val cyan = Color(0xFF74B9FF)
    return listOf(
        Nebula(fracX = 0.14f, fracY = 0.12f, radiusDp = randomRadiusDp(), color = violet, alpha = randomAlpha()),
        Nebula(fracX = 0.86f, fracY = 0.16f, radiusDp = randomRadiusDp(), color = cyan, alpha = randomAlpha()),
        Nebula(fracX = 0.18f, fracY = 0.86f, radiusDp = randomRadiusDp(), color = cyan, alpha = randomAlpha()),
        Nebula(fracX = 0.84f, fracY = 0.82f, radiusDp = randomRadiusDp(), color = violet, alpha = randomAlpha()),
    )
}

private fun randomRadiusDp(): Dp = (120 + Random.nextInt(61)).dp       // 120..180
private fun randomAlpha(): Float = 0.15f + Random.nextFloat() * 0.10f  // 0.15..0.25

private const val TWO_PI: Float = (2.0 * PI).toFloat()
private const val STAR_CLOCK_CYCLE_SECONDS: Float = 60f
private const val MeteorMinIntervalMs: Long = 2_000
private const val MeteorMaxIntervalMs: Long = 5_000
private val MeteorAngleRadians: Float = (35.0 * PI / 180.0).toFloat()
