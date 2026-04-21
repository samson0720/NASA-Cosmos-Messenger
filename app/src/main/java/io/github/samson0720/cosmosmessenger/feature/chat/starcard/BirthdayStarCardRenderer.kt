package io.github.samson0720.cosmosmessenger.feature.chat.starcard

import android.content.Context
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
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.util.ApodDateParser
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BirthdayStarCard(
    val previewBitmap: Bitmap,
    val shareUri: Uri,
    val accent: ZodiacAccent,
    val shareText: String,
)

class BirthdayStarCardRenderer(
    private val context: Context,
) {

    suspend fun render(apod: Apod): BirthdayStarCard = withContext(Dispatchers.IO) {
        val image = loadBitmap(apod.url)
        val accent = ZodiacAccentResolver.resolve(apod.date)
        val bitmap = drawCard(apod, image, accent)
        val uri = writeCard(bitmap, apod)
        BirthdayStarCard(
            previewBitmap = bitmap,
            shareUri = uri,
            accent = accent,
            shareText = "Your Birthday Sky - ${apod.title} (${ApodDateParser.formatForDisplay(apod.date)})",
        )
    }

    private suspend fun loadBitmap(url: String): Bitmap {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request)
        val drawable = result.drawable ?: throw IOException("Unable to load APOD image")
        return drawable.toBitmap()
    }

    private fun writeCard(bitmap: Bitmap, apod: Apod): Uri {
        val dir = File(context.cacheDir, "star_cards").apply { mkdirs() }
        val file = File(dir, "birthday_star_${apod.date}_${System.currentTimeMillis()}.png")
        file.outputStream().use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                throw IOException("Unable to write star card")
            }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    private fun drawCard(apod: Apod, source: Bitmap, accent: ZodiacAccent): Bitmap {
        val width = 1080
        val height = 1600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.rgb(8, 10, 24))
        drawBackground(canvas, width, height, accent.color)
        drawStarDust(canvas, width)
        drawHeroImage(canvas, source, width, accent.color)
        drawInfoPanel(canvas, width, height, accent.color)
        drawTextBlock(canvas, apod, accent, width, height)
        drawFrame(canvas, width, height, accent.color)

        return bitmap
    }

    private fun drawCroppedImage(canvas: Canvas, source: Bitmap, target: Rect) {
        val sourceRatio = source.width.toFloat() / source.height
        val targetRatio = target.width().toFloat() / target.height()
        val src = if (sourceRatio > targetRatio) {
            val cropWidth = (source.height * targetRatio).toInt()
            val left = (source.width - cropWidth) / 2
            Rect(left, 0, left + cropWidth, source.height)
        } else {
            val cropHeight = (source.width / targetRatio).toInt()
            val top = (source.height - cropHeight) / 2
            Rect(0, top, source.width, top + cropHeight)
        }
        canvas.drawBitmap(source, src, target, Paint(Paint.ANTI_ALIAS_FLAG))
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int, accentColor: Int) {
        val base = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                intArrayOf(
                    Color.rgb(13, 16, 36),
                    Color.rgb(6, 8, 22),
                    Color.rgb(10, 12, 30),
                ),
                floatArrayOf(0f, 0.56f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), base)

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(42, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        }
        canvas.drawCircle(width * 0.18f, 190f, 260f, glow)
        canvas.drawCircle(width * 0.86f, 1280f, 340f, glow)
    }

    private fun drawStarDust(canvas: Canvas, width: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(128, 255, 255, 255)
        }
        val points = listOf(
            108f to 128f,
            202f to 286f,
            338f to 154f,
            520f to 246f,
            702f to 114f,
            884f to 318f,
            972f to 182f,
            128f to 1310f,
            290f to 1440f,
            766f to 1388f,
            936f to 1182f,
        )
        points.forEachIndexed { index, (x, y) ->
            canvas.drawCircle(x.coerceAtMost(width - 72f), y, if (index % 3 == 0) 4f else 2.5f, paint)
        }
    }

    private fun drawHeroImage(canvas: Canvas, source: Bitmap, width: Int, accentColor: Int) {
        val hero = RectF(72f, 150f, width - 72f, 980f)
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(130, 0, 0, 0)
        }
        canvas.drawRoundRect(RectF(hero.left + 8f, hero.top + 12f, hero.right + 8f, hero.bottom + 12f), 36f, 36f, shadow)

        canvas.save()
        val path = Path().apply {
            addRoundRect(hero, 36f, 36f, Path.Direction.CW)
        }
        canvas.clipPath(path)
        drawCroppedImage(
            canvas = canvas,
            source = source,
            target = Rect(hero.left.toInt(), hero.top.toInt(), hero.right.toInt(), hero.bottom.toInt()),
        )
        drawHeroGradient(canvas, hero)
        canvas.restore()

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.argb(150, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        }
        canvas.drawRoundRect(hero, 36f, 36f, border)
    }

    private fun drawHeroGradient(canvas: Canvas, hero: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                hero.top + hero.height() * 0.55f,
                0f,
                hero.bottom,
                Color.TRANSPARENT,
                Color.argb(178, 8, 10, 24),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(hero.left, hero.top + hero.height() * 0.45f, hero.right, hero.bottom, paint)
    }

    private fun drawInfoPanel(canvas: Canvas, width: Int, height: Int, accentColor: Int) {
        val panel = RectF(72f, 1018f, width - 72f, height - 82f)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(225, 11, 14, 32)
        }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.argb(118, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        }
        canvas.drawRoundRect(panel, 34f, 34f, fill)
        canvas.drawRoundRect(panel, 34f, 34f, border)
    }

    private fun drawFrame(canvas: Canvas, width: Int, height: Int, accentColor: Int) {
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.argb(96, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        }
        canvas.drawRoundRect(RectF(34f, 34f, width - 34f, height - 34f), 34f, 34f, frame)
    }

    private fun drawTextBlock(canvas: Canvas, apod: Apod, accent: ZodiacAccent, width: Int, height: Int) {
        drawBrandPill(canvas, 92f, 86f, accent.color)
        drawBadge(canvas, 110f, 1060f, accent)
        drawStaticText(
            canvas = canvas,
            text = apod.title,
            x = 110f,
            y = 1160f,
            width = width - 220,
            size = 58f,
            color = Color.WHITE,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
            maxLines = 3,
        )
        drawStaticText(
            canvas = canvas,
            text = "${ApodDateParser.formatForDisplay(apod.date)}  |  NASA Astronomy Picture of the Day",
            x = 110f,
            y = 1360f,
            width = width - 220,
            size = 31f,
            color = Color.argb(220, 232, 236, 255),
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL),
            maxLines = 2,
        )
        drawStaticText(
            canvas = canvas,
            text = "Your Birthday Sky",
            x = 110f,
            y = height - 178f,
            width = width - 220,
            size = 46f,
            color = accent.color,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
            maxLines = 1,
        )
        drawStaticText(
            canvas = canvas,
            text = "NASA Cosmos Messenger",
            x = 110f,
            y = height - 116f,
            width = width - 220,
            size = 29f,
            color = Color.argb(190, 232, 236, 255),
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL),
            maxLines = 1,
        )
    }

    private fun drawBrandPill(canvas: Canvas, x: Float, y: Float, accentColor: Int) {
        val rect = RectF(x, y, x + 380f, y + 58f)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(130, 8, 10, 24)
        }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.argb(150, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        }
        canvas.drawRoundRect(rect, 29f, 29f, fill)
        canvas.drawRoundRect(rect, 29f, 29f, border)
        drawStaticText(
            canvas = canvas,
            text = "NASA COSMOS MESSENGER",
            x = x + 24f,
            y = y + 17f,
            width = 336,
            size = 22f,
            color = Color.argb(220, 255, 255, 255),
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
            maxLines = 1,
        )
    }

    private fun drawBadge(canvas: Canvas, x: Float, y: Float, accent: ZodiacAccent) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(66, Color.red(accent.color), Color.green(accent.color), Color.blue(accent.color))
        }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = accent.color
        }
        val rect = RectF(x, y, x + 628f, y + 70f)
        canvas.drawRoundRect(rect, 34f, 34f, paint)
        canvas.drawRoundRect(rect, 34f, 34f, border)
        drawStaticText(
            canvas = canvas,
            text = "${accent.label} - ${accent.subtitle}",
            x = x + 28f,
            y = y + 17f,
            width = 570,
            size = 25f,
            color = Color.WHITE,
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
            maxLines = 1,
        )
    }

    private fun drawStaticText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        size: Float,
        color: Int,
        typeface: Typeface,
        maxLines: Int,
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            this.typeface = typeface
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
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
}
