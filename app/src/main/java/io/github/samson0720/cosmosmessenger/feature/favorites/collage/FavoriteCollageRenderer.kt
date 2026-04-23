package io.github.samson0720.cosmosmessenger.feature.favorites.collage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class FavoriteCollage(
    val previewBitmap: Bitmap,
    val shareUri: Uri,
    val template: MemoryCollageTemplate,
    val shareText: String,
)

class FavoriteCollageRenderer(
    private val context: Context,
) {

    suspend fun render(
        apods: List<Apod>,
        template: MemoryCollageTemplate,
    ): FavoriteCollage = withContext(Dispatchers.IO) {
        require(apods.size == CollageImageCount) { "Favorite collage requires exactly 3 APOD items" }
        require(apods.all { it.mediaType == ApodMediaType.IMAGE }) {
            "Favorite collage supports image APOD items only"
        }

        val images = apods.map { loadBitmap(it.url) }
        val bitmap = drawCollage(images = images, template = template)
        val uri = writeCollage(bitmap, template)
        FavoriteCollage(
            previewBitmap = bitmap,
            shareUri = uri,
            template = template,
            shareText = context.getString(R.string.collage_share_text),
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

    private fun writeCollage(
        bitmap: Bitmap,
        template: MemoryCollageTemplate,
    ): Uri {
        val dir = File(context.cacheDir, "memory_collages").apply { mkdirs() }
        val file = File(
            dir,
            "favorite_collage_${template.name.lowercase()}_${System.currentTimeMillis()}.png",
        )
        file.outputStream().use { out ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                throw IOException("Unable to write favorite collage")
            }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    private fun drawCollage(
        images: List<Bitmap>,
        template: MemoryCollageTemplate,
    ): Bitmap {
        val templateBitmap = loadTemplate(template)
        val bitmap = Bitmap.createBitmap(
            templateBitmap.width,
            templateBitmap.height,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(templateBitmap, 0f, 0f, null)
        slotsFor(template).forEachIndexed { index, slot ->
            drawBitmapCropped(canvas, images[index], slot)
        }
        return bitmap
    }

    private fun drawBitmapCropped(
        canvas: Canvas,
        source: Bitmap,
        slot: ImageSlot,
    ) {
        val destination = RectF(
            -slot.width / 2f,
            -slot.height / 2f,
            slot.width / 2f,
            slot.height / 2f,
        )
        val scale = maxOf(
            destination.width() / source.width.toFloat(),
            destination.height() / source.height.toFloat(),
        )
        val cropWidth = destination.width() / scale
        val cropHeight = destination.height() / scale
        val left = ((source.width - cropWidth) / 2f).coerceAtLeast(0f)
        val top = ((source.height - cropHeight) / 2f).coerceAtLeast(0f)
        val src = Rect(
            left.toInt(),
            top.toInt(),
            (left + cropWidth).toInt().coerceAtMost(source.width),
            (top + cropHeight).toInt().coerceAtMost(source.height),
        )
        val clipPath = android.graphics.Path().apply {
            addRoundRect(destination, slot.cornerRadius, slot.cornerRadius, android.graphics.Path.Direction.CW)
        }
        val save = canvas.save()
        canvas.translate(slot.centerX, slot.centerY)
        canvas.rotate(slot.rotationDegrees)
        canvas.clipPath(clipPath)
        canvas.drawBitmap(source, src, destination, null)
        canvas.restoreToCount(save)
    }

    private fun loadTemplate(template: MemoryCollageTemplate): Bitmap {
        val resId = when (template) {
            MemoryCollageTemplate.PolaroidOrbit -> R.drawable.collage_template_polaroid_orbit
            MemoryCollageTemplate.MissionBoard -> R.drawable.collage_template_mission_board
            MemoryCollageTemplate.CelestialJournal -> R.drawable.collage_template_celestial_journal
        }
        return BitmapFactory.decodeResource(context.resources, resId)
            ?: throw IOException("Unable to decode collage template ${template.name}")
    }

    private fun slotsFor(template: MemoryCollageTemplate): List<ImageSlot> = when (template) {
        MemoryCollageTemplate.PolaroidOrbit -> listOf(
            ImageSlot(centerX = 372f, centerY = 353f, width = 472f, height = 464f, rotationDegrees = -5.2f),
            ImageSlot(centerX = 734f, centerY = 746f, width = 448f, height = 426f, rotationDegrees = 4.2f),
            ImageSlot(centerX = 422f, centerY = 1088f, width = 462f, height = 426f, rotationDegrees = -3.0f),
        )
        MemoryCollageTemplate.MissionBoard -> listOf(
            ImageSlot(centerX = 500f, centerY = 316f, width = 536f, height = 300f, cornerRadius = 18f),
            ImageSlot(centerX = 500f, centerY = 732f, width = 536f, height = 300f, cornerRadius = 18f),
            ImageSlot(centerX = 500f, centerY = 1134f, width = 536f, height = 300f, cornerRadius = 18f),
        )
        MemoryCollageTemplate.CelestialJournal -> listOf(
            ImageSlot(centerX = 562f, centerY = 298f, width = 564f, height = 354f, cornerRadius = 6f),
            ImageSlot(centerX = 536f, centerY = 720f, width = 604f, height = 346f, cornerRadius = 6f),
            ImageSlot(centerX = 544f, centerY = 1116f, width = 538f, height = 330f, cornerRadius = 6f),
        )
    }

    private data class ImageSlot(
        val centerX: Float,
        val centerY: Float,
        val width: Float,
        val height: Float,
        val rotationDegrees: Float = 0f,
        val cornerRadius: Float = 0f,
    )

    private companion object {
        const val CollageImageCount = 3
    }
}
