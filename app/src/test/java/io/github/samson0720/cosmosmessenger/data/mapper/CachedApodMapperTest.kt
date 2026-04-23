package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.local.CachedApodEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.ApodSource
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CachedApodMapperTest {

    private val cachedAt = Instant.parse("2026-04-21T00:00:00Z")

    @Test
    fun toCachedEntity_mapsDomainToEntity() {
        val apod = sampleApod(
            mediaType = ApodMediaType.IMAGE,
            hdUrl = "https://example.com/hd.jpg",
        )

        val entity = apod.toCachedEntity(cachedAt)

        assertEquals("2024-01-02", entity.date)
        assertEquals("Sample title", entity.title)
        assertEquals("Sample explanation", entity.explanation)
        assertEquals("IMAGE", entity.mediaType)
        assertEquals("https://example.com/image.jpg", entity.url)
        assertEquals("https://example.com/hd.jpg", entity.hdUrl)
        assertEquals(cachedAt.toEpochMilli(), entity.cachedAt)
    }

    @Test
    fun toCachedEntity_nullHdUrl_keepsNull() {
        val apod = sampleApod(hdUrl = null)

        val entity = apod.toCachedEntity(cachedAt)

        assertNull(entity.hdUrl)
    }

    @Test
    fun toDomain_mapsEntityToCachedDomain() {
        val entity = sampleEntity(mediaType = "VIDEO")

        val apod = entity.toDomain()

        assertEquals(LocalDate.of(2024, 1, 2), apod.date)
        assertEquals("Sample title", apod.title)
        assertEquals("Sample explanation", apod.explanation)
        assertEquals(ApodMediaType.VIDEO, apod.mediaType)
        assertEquals("https://example.com/image.jpg", apod.url)
        assertEquals("https://example.com/hd.jpg", apod.hdUrl)
        assertEquals(ApodSource.CACHE, apod.source)
    }

    @Test
    fun toDomain_unknownMediaType_fallsBackToOther() {
        val entity = sampleEntity(mediaType = "LEGACY")

        val apod = entity.toDomain()

        assertEquals(ApodMediaType.OTHER, apod.mediaType)
        assertEquals(ApodSource.CACHE, apod.source)
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

    private fun sampleEntity(mediaType: String): CachedApodEntity = CachedApodEntity(
        date = "2024-01-02",
        title = "Sample title",
        explanation = "Sample explanation",
        mediaType = mediaType,
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/hd.jpg",
        cachedAt = cachedAt.toEpochMilli(),
    )
}
