package io.github.samson0720.cosmosmessenger.data

import io.github.samson0720.cosmosmessenger.data.remote.ApodDto
import io.github.samson0720.cosmosmessenger.data.remote.ApodService
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApodRepositoryImplTest {

    @Test
    fun getApod_withoutDate_callsServiceWithNullDate() = runTest {
        val service = FakeApodService()
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(date = null)

        assertTrue(result.isSuccess)
        assertEquals(listOf(FakeApodService.Call("test-key", null)), service.calls)
    }

    @Test
    fun getApod_withDate_formatsDateAsIso() = runTest {
        val service = FakeApodService()
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertTrue(result.isSuccess)
        assertEquals(listOf(FakeApodService.Call("test-key", "2024-01-02")), service.calls)
    }

    @Test
    fun getApod_success_mapsDtoToDomain() = runTest {
        val service = FakeApodService()
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val apod = repository.getApod(LocalDate.of(2024, 1, 2)).getOrThrow()

        assertEquals(LocalDate.of(2024, 1, 2), apod.date)
        assertEquals("Sample title", apod.title)
        assertEquals("Sample explanation", apod.explanation)
        assertEquals(ApodMediaType.IMAGE, apod.mediaType)
        assertEquals("https://example.com/image.jpg", apod.url)
        assertEquals("https://example.com/hd.jpg", apod.hdUrl)
    }

    @Test
    fun getApod_http429_returnsRateLimited() = runTest {
        val service = FakeApodService {
            throw httpException(429)
        }
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertSame(ApodException.RateLimited, result.exceptionOrNull())
    }

    @Test
    fun getApod_http404_returnsNotFound() = runTest {
        val service = FakeApodService {
            throw httpException(404)
        }
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertSame(ApodException.NotFound, result.exceptionOrNull())
    }

    @Test
    fun getApod_ioException_returnsNetwork() = runTest {
        val service = FakeApodService {
            throw IOException("network")
        }
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertSame(ApodException.Network, result.exceptionOrNull())
    }

    @Test
    fun getApod_transientIOException_retriesOnce() = runTest {
        var attempt = 0
        val service = FakeApodService {
            if (attempt++ == 0) throw SocketTimeoutException("timeout")
            sampleDto()
        }
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertTrue(result.isSuccess)
        assertEquals(2, service.calls.size)
    }

    @Test
    fun getApod_unknownHost_doesNotRetry() = runTest {
        val service = FakeApodService {
            throw UnknownHostException("offline")
        }
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertSame(ApodException.Network, result.exceptionOrNull())
        assertEquals(1, service.calls.size)
    }

    @Test
    fun getApod_transientHttp503_retriesOnce() = runTest {
        var attempt = 0
        val service = FakeApodService {
            if (attempt++ == 0) throw httpException(503)
            sampleDto()
        }
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertTrue(result.isSuccess)
        assertEquals(2, service.calls.size)
    }

    @Test
    fun getApod_retrySecondFailure_returnsMappedSecondFailure() = runTest {
        var attempt = 0
        val service = FakeApodService {
            if (attempt++ == 0) throw SocketTimeoutException("timeout")
            throw httpException(429)
        }
        val repository = ApodRepositoryImpl(service = service, apiKey = "test-key")

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertSame(ApodException.RateLimited, result.exceptionOrNull())
        assertEquals(2, service.calls.size)
    }

    private class FakeApodService(
        private val handler: suspend () -> ApodDto = { sampleDto() },
    ) : ApodService {

        data class Call(val apiKey: String, val date: String?)

        val calls = mutableListOf<Call>()

        override suspend fun getApod(apiKey: String, date: String?): ApodDto {
            calls += Call(apiKey, date)
            return handler()
        }
    }

    private companion object {
        fun sampleDto(): ApodDto = ApodDto(
            date = "2024-01-02",
            title = "Sample title",
            explanation = "Sample explanation",
            url = "https://example.com/image.jpg",
            hdurl = "https://example.com/hd.jpg",
            mediaType = "image",
            copyright = "NASA",
        )

        fun httpException(code: Int): HttpException {
            val body = "{}".toResponseBody("application/json".toMediaType())
            return HttpException(Response.error<ApodDto>(code, body))
        }
    }
}
