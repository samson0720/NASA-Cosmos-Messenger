package io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame

import android.graphics.Color
import androidx.annotation.ColorInt
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.ZodiacSign

internal data class ZodiacStarPoint(
    val x: Float,
    val y: Float,
    val magnitude: Int,
)

internal data class TemplateSpec(
    val imageCenterX: Float,
    val imageCenterY: Float,
    val imageRadius: Float,
    val titleY: Float = 1234f,
    val dateY: Float = 1324f,
    val brandY: Float = 1404f,
    @ColorInt val ringColor: Int,
    @ColorInt val secondaryRingColor: Int = Color.rgb(98, 61, 47),
)

internal data class ZodiacFrameSpec(
    val sign: ZodiacSign,
    val template: TemplateSpec,
    val stars: List<ZodiacStarPoint>,
    val lines: List<Pair<Int, Int>>,
)

internal object ZodiacFrameSpecs {

    fun forSign(sign: ZodiacSign): ZodiacFrameSpec = specs.getValue(sign)

    val all: List<ZodiacFrameSpec>
        get() = ZodiacSign.entries.map(::forSign)

    private val specs = listOf(
        ZodiacFrameSpec(
            sign = ZodiacSign.Aries,
            template = TemplateSpec(
                imageCenterX = 508f,
                imageCenterY = 707f,
                imageRadius = 289f,
                ringColor = Color.rgb(144, 105, 35),
            ),
            stars = listOf(point(22, 68, 1), point(38, 54, 2), point(58, 44, 1), point(78, 38, 2)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Taurus,
            template = TemplateSpec(
                imageCenterX = 511f,
                imageCenterY = 705f,
                imageRadius = 285f,
                ringColor = Color.rgb(71, 84, 47),
            ),
            stars = listOf(point(20, 30, 1), point(35, 42, 2), point(50, 50, 2), point(65, 44, 1), point(80, 34, 1), point(44, 68, 2), point(58, 72, 1)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 2 to 5, 5 to 6),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Gemini,
            template = TemplateSpec(
                imageCenterX = 509f,
                imageCenterY = 670f,
                imageRadius = 288f,
                ringColor = Color.rgb(148, 70, 58),
            ),
            stars = listOf(point(30, 22, 2), point(30, 42, 1), point(32, 62, 2), point(70, 22, 2), point(70, 42, 1), point(68, 62, 2), point(50, 42, 1)),
            lines = listOf(0 to 1, 1 to 2, 3 to 4, 4 to 5, 1 to 6, 6 to 4),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Cancer,
            template = TemplateSpec(
                imageCenterX = 511f,
                imageCenterY = 632f,
                imageRadius = 267f,
                ringColor = Color.rgb(73, 108, 108),
            ),
            stars = listOf(point(50, 22, 2), point(35, 38, 1), point(65, 38, 1), point(50, 52, 2), point(30, 68, 1), point(70, 68, 1)),
            lines = listOf(0 to 3, 1 to 3, 2 to 3, 3 to 4, 3 to 5),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Leo,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 673f,
                imageRadius = 256f,
                ringColor = Color.rgb(166, 115, 40),
            ),
            stars = listOf(point(18, 36, 2), point(30, 22, 1), point(42, 30, 2), point(38, 50, 1), point(28, 58, 1), point(58, 58, 2), point(78, 68, 1)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 0, 3 to 5, 5 to 6),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Virgo,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 670f,
                imageRadius = 256f,
                ringColor = Color.rgb(74, 87, 48),
            ),
            stars = listOf(point(22, 28, 1), point(38, 38, 2), point(52, 32, 1), point(50, 52, 2), point(68, 48, 1), point(78, 68, 1), point(35, 68, 1)),
            lines = listOf(0 to 1, 1 to 2, 1 to 3, 3 to 4, 4 to 5, 3 to 6),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Libra,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 670f,
                imageRadius = 288f,
                ringColor = Color.rgb(76, 86, 82),
            ),
            stars = listOf(point(50, 22, 2), point(30, 40, 1), point(70, 40, 1), point(22, 62, 2), point(50, 58, 1), point(78, 62, 2)),
            lines = listOf(0 to 1, 0 to 2, 1 to 3, 1 to 4, 2 to 4, 2 to 5),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Scorpio,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 670f,
                imageRadius = 288f,
                ringColor = Color.rgb(92, 55, 65),
            ),
            stars = listOf(point(18, 28, 1), point(32, 30, 2), point(46, 36, 1), point(50, 52, 2), point(46, 68, 1), point(62, 72, 1), point(76, 62, 2), point(72, 46, 1)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 7),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Sagittarius,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 670f,
                imageRadius = 288f,
                ringColor = Color.rgb(144, 93, 42),
            ),
            stars = listOf(point(20, 68, 1), point(34, 52, 2), point(50, 44, 1), point(66, 32, 2), point(80, 22, 1), point(60, 60, 1), point(44, 66, 1)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 2 to 5, 5 to 6),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Capricorn,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 670f,
                imageRadius = 288f,
                ringColor = Color.rgb(78, 83, 57),
            ),
            stars = listOf(point(18, 40, 1), point(30, 32, 2), point(44, 38, 1), point(58, 50, 2), point(72, 58, 1), point(82, 44, 1), point(50, 70, 1)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 3 to 6),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Aquarius,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 670f,
                imageRadius = 288f,
                ringColor = Color.rgb(65, 94, 116),
            ),
            stars = listOf(point(20, 28, 1), point(34, 38, 2), point(48, 32, 1), point(60, 44, 2), point(74, 38, 1), point(42, 58, 1), point(56, 68, 2)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 3 to 5, 5 to 6),
        ),
        ZodiacFrameSpec(
            sign = ZodiacSign.Pisces,
            template = TemplateSpec(
                imageCenterX = 512f,
                imageCenterY = 670f,
                imageRadius = 288f,
                ringColor = Color.rgb(94, 78, 126),
            ),
            stars = listOf(point(22, 30, 1), point(36, 38, 2), point(48, 46, 1), point(42, 62, 2), point(58, 62, 1), point(68, 50, 2), point(78, 34, 1)),
            lines = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 2 to 5),
        ),
    ).associateBy { it.sign }

    private fun point(x: Int, y: Int, magnitude: Int): ZodiacStarPoint =
        ZodiacStarPoint(x = x.toFloat(), y = y.toFloat(), magnitude = magnitude)
}
