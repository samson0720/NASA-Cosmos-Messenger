package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.remote.ApodDto
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.ApodSource
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApodMapperTest {

    @Test
    fun toDomain_imageDto_mapsAllFields() {
        val dto = sampleDto(mediaType = "image", hdurl = "https://example.com/hd.jpg")

        val domain = dto.toDomain()

        assertEquals(LocalDate.of(2024, 1, 2), domain.date)
        assertEquals("Sample title", domain.title)
        assertEquals("Sample explanation", domain.explanation)
        assertEquals(ApodMediaType.IMAGE, domain.mediaType)
        assertEquals("https://example.com/image.jpg", domain.url)
        assertEquals("https://example.com/hd.jpg", domain.hdUrl)
        assertEquals(ApodSource.NETWORK, domain.source)
    }

    @Test
    fun toDomain_videoDto_mapsVideoMediaType() {
        val dto = sampleDto(mediaType = "video")

        val domain = dto.toDomain()

        assertEquals(ApodMediaType.VIDEO, domain.mediaType)
    }

    @Test
    fun toDomain_unknownMediaType_mapsOther() {
        val dto = sampleDto(mediaType = "audio")

        val domain = dto.toDomain()

        assertEquals(ApodMediaType.OTHER, domain.mediaType)
    }

    @Test
    fun toDomain_nullHdUrl_keepsNull() {
        val dto = sampleDto(hdurl = null)

        val domain = dto.toDomain()

        assertNull(domain.hdUrl)
    }

    private fun sampleDto(
        mediaType: String = "image",
        hdurl: String? = "https://example.com/hd.jpg",
    ): ApodDto = ApodDto(
        date = "2024-01-02",
        title = "Sample title",
        explanation = "Sample explanation",
        url = "https://example.com/image.jpg",
        hdurl = hdurl,
        mediaType = mediaType,
        copyright = "NASA",
    )
}
