package io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame

import android.graphics.Bitmap
import android.graphics.Canvas
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.ZodiacSign

internal data class BirthdayStarCardFrameContent(
    val sourceImage: Bitmap,
    val templateImage: Bitmap,
    val title: String,
    val dateLine: String,
)

internal object ZodiacFrameDrawer {

    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        sign: ZodiacSign,
        content: BirthdayStarCardFrameContent,
    ) {
        TemplateFrameDrawer.draw(canvas, width, height, content, ZodiacFrameSpecs.forSign(sign))
    }
}
