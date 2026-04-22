package io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.ColorInt

internal object StarCardCanvasPrimitives {

    fun withAlpha(@ColorInt color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    fun drawCroppedBitmap(canvas: Canvas, source: Bitmap, target: RectF) {
        val sourceRatio = source.width.toFloat() / source.height
        val targetRatio = target.width() / target.height()
        val src = if (sourceRatio > targetRatio) {
            val cropWidth = (source.height * targetRatio).toInt()
            val left = (source.width - cropWidth) / 2
            Rect(left, 0, left + cropWidth, source.height)
        } else {
            val cropHeight = (source.width / targetRatio).toInt()
            val top = (source.height - cropHeight) / 2
            Rect(0, top, source.width, top + cropHeight)
        }
        canvas.drawBitmap(
            source,
            src,
            Rect(target.left.toInt(), target.top.toInt(), target.right.toInt(), target.bottom.toInt()),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
    }

    fun drawStaticText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        size: Float,
        @ColorInt color: Int,
        typeface: Typeface,
        maxLines: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            this.typeface = typeface
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(android.text.TextUtils.TruncateAt.END)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    fun drawConstellation(
        canvas: Canvas,
        spec: ZodiacFrameSpec,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        @ColorInt color: Int,
        alpha: Int = 210,
    ) {
        val points = spec.stars.map { star ->
            ZodiacMappedPoint(
                x = centerX - width / 2f + (star.x / 100f) * width,
                y = centerY - height / 2f + (star.y / 100f) * height,
                radius = if (star.magnitude == 2) 5.5f else 3.5f,
            )
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            this.color = withAlpha(color, alpha / 2)
        }
        spec.lines.forEach { (start, end) ->
            val a = points.getOrNull(start)
            val b = points.getOrNull(end)
            if (a != null && b != null) {
                canvas.drawLine(a.x, a.y, b.x, b.y, linePaint)
            }
        }

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = withAlpha(color, alpha)
        }
        points.forEach { point ->
            canvas.drawCircle(point.x, point.y, point.radius, starPaint)
        }
    }

    private data class ZodiacMappedPoint(
        val x: Float,
        val y: Float,
        val radius: Float,
    )
}
