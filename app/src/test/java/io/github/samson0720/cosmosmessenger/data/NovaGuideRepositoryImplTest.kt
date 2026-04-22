package io.github.samson0720.cosmosmessenger.data

import io.github.samson0720.cosmosmessenger.data.remote.NovaGuideDto
import io.github.samson0720.cosmosmessenger.data.remote.NovaGuideRequestDto
import io.github.samson0720.cosmosmessenger.data.remote.NovaGuideService
import io.github.samson0720.cosmosmessenger.data.remote.NovaGuideTermDto
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NovaGuideRepositoryImplTest {

    @Test
    fun explain_withoutEndpoint_returnsNotConfiguredAndSkipsService() = runTest {
        val service = FakeNovaGuideService()
        val repository = NovaGuideRepositoryImpl(service, endpoint = "")

        val result = repository.explain(sampleApod())

        assertTrue(result.isFailure)
        assertSame(NovaGuideException.NotConfigured, result.exceptionOrNull())
        assertEquals(emptyList<NovaGuideRequestDto>(), service.requests)
    }

    @Test
    fun explain_success_mapsTrimmedStructuredGuide() = runTest {
        val service = FakeNovaGuideService(
            response = NovaGuideDto(
                shortSummary = "  short  ",
                plainChinese = "  body  ",
                keyPoints = listOf(" one ", "", " two ", " three ", " extra "),
                terms = listOf(
                    NovaGuideTermDto(" solar wind ", " 太陽風 ", " charged particles "),
                    NovaGuideTermDto("", "略過", "invalid"),
                ),
                source = " NASA APOD explanation ",
            ),
        )
        val repository = NovaGuideRepositoryImpl(service, endpoint = "https://example.com/guide")

        val result = repository.explain(sampleApod())

        assertTrue(result.isSuccess)
        val guide = result.getOrThrow()
        assertEquals("short", guide.shortSummary)
        assertEquals("body", guide.plainChinese)
        assertEquals(listOf("one", "two", "three"), guide.keyPoints)
        assertEquals(1, guide.terms.size)
        assertEquals("solar wind", guide.terms.first().term)
        assertEquals("太陽風", guide.terms.first().zh)
        assertEquals("NASA APOD explanation", guide.source)
        assertEquals("https://example.com/guide", service.endpoints.single())
        assertEquals("2024-01-02", service.requests.single().date)
    }

    @Test
    fun explain_emptyRequiredField_returnsInvalidResponse() = runTest {
        val service = FakeNovaGuideService(
            response = NovaGuideDto(
                shortSummary = "",
                plainChinese = "body",
                keyPoints = listOf("point"),
                source = "NASA APOD explanation",
            ),
        )
        val repository = NovaGuideRepositoryImpl(service, endpoint = "https://example.com/guide")

        val result = repository.explain(sampleApod())

        assertTrue(result.isFailure)
        assertSame(NovaGuideException.InvalidResponse, result.exceptionOrNull())
    }

    @Test
    fun explain_ioFailure_returnsNetworkError() = runTest {
        val service = FakeNovaGuideService(failure = IOException("offline"))
        val repository = NovaGuideRepositoryImpl(service, endpoint = "https://example.com/guide")

        val result = repository.explain(sampleApod())

        assertTrue(result.isFailure)
        assertSame(NovaGuideException.Network, result.exceptionOrNull())
    }

    private class FakeNovaGuideService(
        private val response: NovaGuideDto = NovaGuideDto(
            shortSummary = "summary",
            plainChinese = "body",
            keyPoints = listOf("one", "two", "three"),
            source = "NASA APOD explanation",
        ),
        private val failure: Throwable? = null,
    ) : NovaGuideService {
        val endpoints = mutableListOf<String>()
        val requests = mutableListOf<NovaGuideRequestDto>()

        override suspend fun explain(
            endpoint: String,
            request: NovaGuideRequestDto,
        ): NovaGuideDto {
            endpoints += endpoint
            requests += request
            failure?.let { throw it }
            return response
        }
    }

    private fun sampleApod(): Apod = Apod(
        date = LocalDate.of(2024, 1, 2),
        title = "Sample APOD",
        explanation = "NASA explanation",
        mediaType = ApodMediaType.IMAGE,
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/hd.jpg",
    )
}
