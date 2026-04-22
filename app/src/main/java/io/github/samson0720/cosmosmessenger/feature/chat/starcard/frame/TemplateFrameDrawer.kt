package io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame.StarCardCanvasPrimitives.drawConstellation
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame.StarCardCanvasPrimitives.drawCroppedBitmap
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame.StarCardCanvasPrimitives.drawStaticText
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame.StarCardCanvasPrimitives.withAlpha

internal object TemplateFrameDrawer {

    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        content: BirthdayStarCardFrameContent,
        spec: ZodiacFrameSpec,
    ) {
        val templateSpec = spec.template

        canvas.drawBitmap(
            content.templateImage,
            null,
            Rect(0, 0, width, height),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )

        drawApodIntoCircle(canvas, content.sourceImage, templateSpec)
        drawConstellation(
            canvas = canvas,
            spec = spec,
            centerX = templateSpec.imageCenterX,
            centerY = templateSpec.imageCenterY,
            width = templateSpec.imageRadius * 0.82f,
            height = templateSpec.imageRadius * 0.82f,
            color = Color.WHITE,
            alpha = 168,
        )
        drawImageRing(canvas, templateSpec, spec)
        drawDynamicText(canvas, width, content, templateSpec)
    }

    private fun drawApodIntoCircle(
        canvas: Canvas,
        source: Bitmap,
        spec: TemplateSpec,
    ) {
        val radius = spec.imageRadius - 8f
        val target = RectF(
            spec.imageCenterX - radius,
            spec.imageCenterY - radius,
            spec.imageCenterX + radius,
            spec.imageCenterY + radius,
        )
        canvas.save()
        val clip = Path().apply {
            addCircle(spec.imageCenterX, spec.imageCenterY, radius, Path.Direction.CW)
        }
        canvas.clipPath(clip)
        drawCroppedBitmap(canvas, source, target)

        val shade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                target.top + target.height() * 0.54f,
                0f,
                target.bottom,
                Color.TRANSPARENT,
                Color.argb(128, 0, 0, 0),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(target, shade)
        canvas.restore()
    }

    private fun drawImageRing(canvas: Canvas, template: TemplateSpec, spec: ZodiacFrameSpec) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.2f
            color = template.ringColor
        }
        canvas.drawCircle(template.imageCenterX, template.imageCenterY, template.imageRadius - 6f, paint)
        paint.strokeWidth = 1.2f
        paint.color = withAlpha(spec.template.secondaryRingColor, 80)
        canvas.drawCircle(template.imageCenterX, template.imageCenterY, template.imageRadius + 8f, paint)
    }

    private fun drawDynamicText(
        canvas: Canvas,
        width: Int,
        content: BirthdayStarCardFrameContent,
        spec: TemplateSpec,
    ) {
        val left = 132f
        val textWidth = width - 264
        val area = RectF(left - 28f, spec.titleY - 28f, width - 104f, spec.brandY + 48f)
        val backing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                area.top,
                0f,
                area.bottom,
                Color.argb(0, 255, 255, 255),
                Color.argb(94, 250, 244, 218),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(area, backing)

        drawStaticText(
            canvas = canvas,
            text = content.title,
            x = left + 2f,
            y = spec.titleY + 2f,
            width = textWidth,
            size = 48f,
            color = Color.argb(72, 70, 58, 42),
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD),
            maxLines = 2,
            alignment = Layout.Alignment.ALIGN_CENTER,
        )
        drawStaticText(
            canvas = canvas,
            text = content.title,
            x = left,
            y = spec.titleY,
            width = textWidth,
            size = 48f,
            color = Color.rgb(98, 61, 47),
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD),
            maxLines = 2,
            alignment = Layout.Alignment.ALIGN_CENTER,
        )
        drawStaticText(
            canvas = canvas,
            text = content.dateLine,
            x = left,
            y = spec.dateY,
            width = textWidth,
            size = 31f,
            color = Color.rgb(98, 61, 47),
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD),
            maxLines = 1,
            alignment = Layout.Alignment.ALIGN_CENTER,
        )
    }
}
