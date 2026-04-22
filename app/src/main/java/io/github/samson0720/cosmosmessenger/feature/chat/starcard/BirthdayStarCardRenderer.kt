package io.github.samson0720.cosmosmessenger.feature.chat.starcard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame.BirthdayStarCardFrameContent
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame.ZodiacFrameDrawer
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame.ZodiacTemplateResources
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
            shareText = context.getString(
                R.string.birthday_card_share_text,
                apod.title,
                ApodDateParser.formatForDisplay(apod.date),
            ),
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
        val width = 1024
        val height = 1536
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        ZodiacFrameDrawer.draw(
            canvas = canvas,
            width = width,
            height = height,
            sign = accent.sign,
            content = BirthdayStarCardFrameContent(
                sourceImage = source,
                templateImage = loadTemplate(accent.sign),
                title = apod.title,
                dateLine = ApodDateParser.formatForDisplay(apod.date).replace("/", " / "),
            ),
        )

        return bitmap
    }

    private fun loadTemplate(sign: ZodiacSign): Bitmap {
        val resId = ZodiacTemplateResources.resourceIdFor(sign)
        return BitmapFactory.decodeResource(context.resources, resId)
            ?: throw IOException("Unable to decode star card template for ${sign.name}")
    }
}
