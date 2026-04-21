package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.local.FavoriteApodEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteApodMapperTest {

    private val savedAt = Instant.parse("2026-04-21T00:00:00Z")

    @Test
    fun toFavoriteEntity_mapsDomainToEntity() {
        val apod = sampleApod(mediaType = ApodMediaType.IMAGE, hdUrl = "https://example.com/hd.jpg")

        val entity = apod.toFavoriteEntity(savedAt)

        assertEquals("2024-01-02", entity.date)
        assertEquals("Sample title", entity.title)
        assertEquals("Sample explanation", entity.explanation)
        assertEquals("IMAGE", entity.mediaType)
        assertEquals("https://example.com/image.jpg", entity.url)
        assertEquals("https://example.com/hd.jpg", entity.hdUrl)
        assertEquals(savedAt.toEpochMilli(), entity.savedAt)
    }

    @Test
    fun toDomain_mapsEntityToDomain() {
        val entity = sampleEntity(mediaType = "VIDEO")

        val favorite = entity.toDomain()

        assertEquals(LocalDate.of(2024, 1, 2), favorite.apod.date)
        assertEquals("Sample title", favorite.apod.title)
        assertEquals("Sample explanation", favorite.apod.explanation)
        assertEquals(ApodMediaType.VIDEO, favorite.apod.mediaType)
        assertEquals("https://example.com/image.jpg", favorite.apod.url)
        assertEquals("https://example.com/hd.jpg", favorite.apod.hdUrl)
        assertEquals(savedAt, favorite.savedAt)
    }

    @Test
    fun toDomain_unknownMediaType_fallsBackToOther() {
        val entity = sampleEntity(mediaType = "LEGACY")

        val favorite = entity.toDomain()

        assertEquals(ApodMediaType.OTHER, favorite.apod.mediaType)
    }

    private fun sampleApod(
        mediaType: ApodMediaType = ApodMediaType.IMAGE,
        hdUrl: String? = "https://example.com/hd.jpg",
    ): Apod = Apod(
        date = LocalDate.of(2024, 1, 2),
        title = "Sample title",
        explanation = "Sample explanation",
        mediaType = mediaType,
        url = "https://example.com/image.jpg",
        hdUrl = hdUrl,
    )

    private fun sampleEntity(mediaType: String): FavoriteApodEntity = FavoriteApodEntity(
        date = "2024-01-02",
        title = "Sample title",
        explanation = "Sample explanation",
        mediaType = mediaType,
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/hd.jpg",
        savedAt = savedAt.toEpochMilli(),
    )
}
