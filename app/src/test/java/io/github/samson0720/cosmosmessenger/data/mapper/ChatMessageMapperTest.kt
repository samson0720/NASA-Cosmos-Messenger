package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.local.ChatMessageEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.ApodSource
import io.github.samson0720.cosmosmessenger.feature.chat.model.ApodCard
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatContent
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMessageMapperTest {

    @Test
    fun toEntity_textMessage_mapsPrimitiveFields() {
        val message = ChatMessage(
            id = "message-1",
            sender = Sender.User,
            content = ChatContent.Text("hello nova"),
        )

        val entity = message.toEntity(sortOrder = 3)

        assertEquals("message-1", entity.id)
        assertEquals(3, entity.sortOrder)
        assertEquals("User", entity.sender)
        assertEquals("TEXT", entity.contentType)
        assertEquals("hello nova", entity.text)
        assertNull(entity.apodDate)
    }

    @Test
    fun toDomain_textEntity_mapsBackToTextMessage() {
        val entity = textEntity(id = "message-1", sender = "Nova", text = "hi")

        val message = entity.toDomain()

        assertEquals(
            ChatMessage(
                id = "message-1",
                sender = Sender.Nova,
                content = ChatContent.Text("hi"),
            ),
            message,
        )
    }

    @Test
    fun apodImage_roundTripsDomainPayloadAndRebuildsDisplayCard() {
        val apod = sampleApod(source = ApodSource.CACHE)
        val message = ChatMessage(
            id = "apod-1",
            sender = Sender.Nova,
            content = ChatContent.ApodImage(
                card = sampleCard(apod),
                payload = apod,
            ),
        )

        val entity = message.toEntity(sortOrder = 1)
        val restored = entity.toDomain()

        assertEquals("APOD_IMAGE", entity.contentType)
        assertEquals("2024-01-02", entity.apodDate)
        assertTrue(restored?.content is ChatContent.ApodImage)
        val content = restored?.content as ChatContent.ApodImage
        assertEquals(apod, content.payload)
        assertEquals("2024/01/02", content.card.displayDate)
        assertEquals("https://example.com/image.jpg", content.card.imageUrl)
        assertEquals("https://example.com/hd.jpg", content.card.sourceUrl)
        assertTrue(content.card.isFromCache)
    }

    @Test
    fun apodVideo_roundTripsWithNullImageUrl() {
        val apod = sampleApod(mediaType = ApodMediaType.VIDEO, hdUrl = null)
        val message = ChatMessage(
            id = "video-1",
            sender = Sender.Nova,
            content = ChatContent.ApodVideo(
                card = sampleCard(apod),
                payload = apod,
            ),
        )

        val restored = message.toEntity(sortOrder = 1).toDomain()

        assertTrue(restored?.content is ChatContent.ApodVideo)
        val content = restored?.content as ChatContent.ApodVideo
        assertEquals(ApodMediaType.VIDEO, content.payload.mediaType)
        assertNull(content.card.imageUrl)
        assertEquals("https://example.com/image.jpg", content.card.sourceUrl)
    }

    @Test
    fun toDomain_invalidSender_returnsNull() {
        val entity = textEntity(id = "broken", sender = "LegacySender", text = "hi")

        assertNull(entity.toDomain())
    }

    @Test
    fun toDomain_malformedApodDate_returnsNull() {
        val entity = apodEntity(apodDate = "not-a-date")

        assertNull(entity.toDomain())
    }

    private fun sampleApod(
        mediaType: ApodMediaType = ApodMediaType.IMAGE,
        hdUrl: String? = "https://example.com/hd.jpg",
        source: ApodSource = ApodSource.NETWORK,
    ): Apod = Apod(
        date = LocalDate.of(2024, 1, 2),
        title = "Sample title",
        explanation = "Sample explanation",
        mediaType = mediaType,
        url = "https://example.com/image.jpg",
        hdUrl = hdUrl,
        source = source,
    )

    private fun sampleCard(apod: Apod): ApodCard = ApodCard(
        title = apod.title,
        displayDate = "2024/01/02",
        explanation = apod.explanation,
        imageUrl = if (apod.mediaType == ApodMediaType.IMAGE) apod.url else null,
        sourceUrl = apod.hdUrl ?: apod.url,
        isFromCache = apod.source == ApodSource.CACHE,
    )

    private fun textEntity(
        id: String,
        sender: String,
        text: String,
    ): ChatMessageEntity = ChatMessageEntity(
        id = id,
        sortOrder = 0,
        sender = sender,
        contentType = "TEXT",
        text = text,
        apodDate = null,
        apodTitle = null,
        apodExplanation = null,
        apodMediaType = null,
        apodUrl = null,
        apodHdUrl = null,
        apodSource = null,
    )

    private fun apodEntity(apodDate: String): ChatMessageEntity = ChatMessageEntity(
        id = "apod",
        sortOrder = 0,
        sender = "Nova",
        contentType = "APOD_IMAGE",
        text = null,
        apodDate = apodDate,
        apodTitle = "Sample title",
        apodExplanation = "Sample explanation",
        apodMediaType = "IMAGE",
        apodUrl = "https://example.com/image.jpg",
        apodHdUrl = "https://example.com/hd.jpg",
        apodSource = "NETWORK",
    )
}
