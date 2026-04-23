package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.local.ChatMessageEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.ApodSource
import io.github.samson0720.cosmosmessenger.feature.chat.model.ApodCard
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatContent
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import io.github.samson0720.cosmosmessenger.util.ApodDateParser
import java.time.LocalDate

private const val CONTENT_TEXT = "TEXT"
private const val CONTENT_APOD_IMAGE = "APOD_IMAGE"
private const val CONTENT_APOD_VIDEO = "APOD_VIDEO"

fun ChatMessage.toEntity(sortOrder: Int): ChatMessageEntity = when (val c = content) {
    is ChatContent.Text -> ChatMessageEntity(
        id = id,
        sortOrder = sortOrder,
        sender = sender.name,
        contentType = CONTENT_TEXT,
        text = c.text,
        apodDate = null,
        apodTitle = null,
        apodExplanation = null,
        apodMediaType = null,
        apodUrl = null,
        apodHdUrl = null,
        apodSource = null,
    )
    is ChatContent.ApodImage -> toApodEntity(
        sortOrder = sortOrder,
        contentType = CONTENT_APOD_IMAGE,
        apod = c.payload,
    )
    is ChatContent.ApodVideo -> toApodEntity(
        sortOrder = sortOrder,
        contentType = CONTENT_APOD_VIDEO,
        apod = c.payload,
    )
}

fun ChatMessageEntity.toDomain(): ChatMessage? {
    val sender = runCatching { Sender.valueOf(sender) }.getOrNull() ?: return null
    val content = when (contentType) {
        CONTENT_TEXT -> ChatContent.Text(text.orEmpty())
        CONTENT_APOD_IMAGE -> {
            val apod = toApodOrNull() ?: return null
            ChatContent.ApodImage(card = apod.toCard(), payload = apod)
        }
        CONTENT_APOD_VIDEO -> {
            val apod = toApodOrNull() ?: return null
            ChatContent.ApodVideo(card = apod.toCard(), payload = apod)
        }
        else -> return null
    }

    return ChatMessage(id = id, sender = sender, content = content)
}

private fun ChatMessage.toApodEntity(
    sortOrder: Int,
    contentType: String,
    apod: Apod,
): ChatMessageEntity = ChatMessageEntity(
    id = id,
    sortOrder = sortOrder,
    sender = sender.name,
    contentType = contentType,
    text = null,
    apodDate = apod.date.toString(),
    apodTitle = apod.title,
    apodExplanation = apod.explanation,
    apodMediaType = apod.mediaType.name,
    apodUrl = apod.url,
    apodHdUrl = apod.hdUrl,
    apodSource = apod.source.name,
)

private fun ChatMessageEntity.toApodOrNull(): Apod? {
    val date = runCatching { LocalDate.parse(apodDate) }.getOrNull() ?: return null
    return Apod(
        date = date,
        title = apodTitle ?: return null,
        explanation = apodExplanation ?: return null,
        mediaType = runCatching { ApodMediaType.valueOf(apodMediaType.orEmpty()) }
            .getOrDefault(ApodMediaType.OTHER),
        url = apodUrl ?: return null,
        hdUrl = apodHdUrl,
        source = runCatching { ApodSource.valueOf(apodSource.orEmpty()) }
            .getOrDefault(ApodSource.NETWORK),
    )
}

private fun Apod.toCard(): ApodCard = ApodCard(
    title = title,
    displayDate = ApodDateParser.formatForDisplay(date),
    explanation = explanation,
    imageUrl = if (mediaType == ApodMediaType.IMAGE) url else null,
    sourceUrl = hdUrl ?: url,
    isFromCache = source == ApodSource.CACHE,
)
