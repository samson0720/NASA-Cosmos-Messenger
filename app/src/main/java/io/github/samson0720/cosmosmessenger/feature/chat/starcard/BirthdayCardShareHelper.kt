package io.github.samson0720.cosmosmessenger.feature.chat.starcard

import android.content.Context
import android.content.Intent
import io.github.samson0720.cosmosmessenger.R

object BirthdayCardShareHelper {

    fun share(context: Context, card: BirthdayStarCard) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, card.shareUri)
            putExtra(Intent.EXTRA_TEXT, card.shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.birthday_card_share_title)),
        )
    }
}
