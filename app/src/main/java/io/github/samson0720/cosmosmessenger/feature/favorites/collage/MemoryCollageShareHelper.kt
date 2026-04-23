package io.github.samson0720.cosmosmessenger.feature.favorites.collage

import android.content.Context
import android.content.Intent
import io.github.samson0720.cosmosmessenger.R

object MemoryCollageShareHelper {

    fun share(context: Context, collage: FavoriteCollage) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, collage.shareUri)
            putExtra(Intent.EXTRA_TEXT, collage.shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.collage_share_title)),
        )
    }
}
