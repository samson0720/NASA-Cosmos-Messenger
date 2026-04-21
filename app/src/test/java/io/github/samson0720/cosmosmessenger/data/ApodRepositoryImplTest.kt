package io.github.samson0720.cosmosmessenger.data

import io.github.samson0720.cosmosmessenger.data.remote.ApodDto
import io.github.samson0720.cosmosmessenger.data.remote.ApodService
import io.github.samson0720.cosmosmessenger.data.local.CachedApodDao
import io.github.samson0720.cosmosmessenger.data.local.CachedApodEntity
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
    fun getApod_success_cachesApodByDate() = runTest {
        val service = FakeApodService()
        val cache = FakeCachedApodDao()
        val repository = ApodRepositoryImpl(
            service = service,
            apiKey = "test-key",
            cacheDao = cache,
        )

        val result = repository.getApod(LocalDate.of(2024, 1, 2))

        assertTrue(result.isSuccess)
        val cached = cache.upserts.single()
        assertEquals("2024-01-02", cached.date)
        assertEquals("Sample title", cached.title)
        assertEquals("Sample explanation", cached.explanation)
        assertEquals("IMAGE", cached.mediaType)
        assertEquals("https://example.com/image.jpg", cached.url)
        assertEquals("https://example.com/hd.jpg", cached.hdUrl)
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
    fun getApod_unknownHostWithCachedDate_returnsCachedApod() = runTest {
        val service = FakeApodService {
            throw UnknownHostException("offline")
        }
        val cache = FakeCachedApodDao(
            initialRows = listOf(
                sampleCachedEntity(
                    date = "2024-01-02",
                    title = "Cached title",
                    explanation = "Cached explanation",
                ),
            ),
        )
        val repository = ApodRepositoryImpl(
            service = service,
            apiKey = "test-key",
            cacheDao = cache,
        )

        val apod = repository.getApod(LocalDate.of(2024, 1, 2)).getOrThrow()

        assertEquals(LocalDate.of(2024, 1, 2), apod.date)
        assertEquals("Cached title", apod.title)
        assertEquals("Cached explanation", apod.explanation)
        assertEquals(ApodMediaType.IMAGE, apod.mediaType)
        assertEquals("https://example.com/cached.jpg", apod.url)
        assertEquals("https://example.com/cached-hd.jpg", apod.hdUrl)
        assertEquals(1, service.calls.size)
        assertEquals(listOf("2024-01-02"), cache.lookups)
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

    private class FakeCachedApodDao(
        initialRows: List<CachedApodEntity> = emptyList(),
    ) : CachedApodDao {

        private val rows = initialRows.associateBy { it.date }.toMutableMap()
        val upserts = mutableListOf<CachedApodEntity>()
        val lookups = mutableListOf<String>()

        override suspend fun upsert(entity: CachedApodEntity) {
            upserts += entity
            rows[entity.date] = entity
        }

        override suspend fun getByDate(date: String): CachedApodEntity? {
            lookups += date
            return rows[date]
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

        fun sampleCachedEntity(
            date: String = "2024-01-02",
            title: String = "Cached title",
            explanation: String = "Cached explanation",
        ): CachedApodEntity = CachedApodEntity(
            date = date,
            title = title,
            explanation = explanation,
            mediaType = "IMAGE",
            url = "https://example.com/cached.jpg",
            hdUrl = "https://example.com/cached-hd.jpg",
            cachedAt = 1_700_000_000_000L,
        )
    }
}
