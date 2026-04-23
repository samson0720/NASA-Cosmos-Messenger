package io.github.samson0720.cosmosmessenger.data

import android.util.Log
import io.github.samson0720.cosmosmessenger.BuildConfig
import io.github.samson0720.cosmosmessenger.data.local.CachedApodDao
import io.github.samson0720.cosmosmessenger.data.mapper.toCachedEntity
import io.github.samson0720.cosmosmessenger.data.mapper.toDomain
import io.github.samson0720.cosmosmessenger.data.remote.ApodDto
import io.github.samson0720.cosmosmessenger.data.remote.ApodService
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

private const val TAG = "ApodRepo"

class ApodRepositoryImpl(
    private val service: ApodService,
    private val apiKey: String,
    private val cacheDao: CachedApodDao? = null,
) : ApodRepository {

    override suspend fun getApod(date: LocalDate?): Result<Apod> {
        cached(date)?.let { cachedApod ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "cache hit date=${cachedApod.date}")
            }
            return Result.success(cachedApod)
        }

        return try {
            val dateParam = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
            if (BuildConfig.DEBUG) Log.d(TAG, "request date=${dateParam ?: "today"}")
            val dto = fetchWithOneRetry(dateParam)
            val apod = dto.toDomain()
            cache(apod)
            Result.success(apod)
        } catch (e: HttpException) {
            if (BuildConfig.DEBUG) Log.w(TAG, "HTTP ${e.code()}")
            Result.failure(
                when (e.code()) {
                    429 -> ApodException.RateLimited
                    404 -> ApodException.NotFound
                    else -> ApodException.Unknown(e)
                },
            )
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) Log.w(TAG, "IO ${e.javaClass.simpleName}")
            cached(date)?.let { return Result.success(it) }
            Result.failure(ApodException.Network)
        } catch (e: CancellationException) {
            // Keep structured concurrency intact; never convert cancellation
            // into an error reply.
            throw e
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Unknown ${e.javaClass.simpleName}")
            Result.failure(ApodException.Unknown(e))
        }
    }

    /**
     * Runs the APOD call; on a single plausibly-transient failure, retries
     * exactly once. If both attempts fail, the second throwable propagates
     * to [getApod]'s existing catch ladder for normal error mapping.
     *
     * CancellationException is rethrown before any classification so
     * structured concurrency is preserved.
     */
    private suspend fun fetchWithOneRetry(dateParam: String?): ApodDto {
        return try {
            service.getApod(apiKey = apiKey, date = dateParam)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (!isTransient(e)) throw e
            if (BuildConfig.DEBUG) Log.w(TAG, "retry after ${e.javaClass.simpleName}")
            service.getApod(apiKey = apiKey, date = dateParam)
        }
    }

    // UnknownHostException is a subclass of IOException, so it must be
    // checked before the generic IOException branch; otherwise Kotlin's
    // `when` would classify it as transient. We intentionally fast-fail
    // DNS/no-network instead of retrying.
    private fun isTransient(e: Throwable): Boolean = when (e) {
        is UnknownHostException -> false
        is SocketTimeoutException -> true
        is HttpException -> e.code() in TRANSIENT_HTTP_CODES
        is IOException -> true
        else -> false
    }

    private suspend fun cache(apod: Apod) {
        val dao = cacheDao ?: return
        runCatching {
            dao.upsert(apod.toCachedEntity(cachedAt = Instant.now()))
        }.onFailure { e ->
            if (BuildConfig.DEBUG) Log.w(TAG, "cache write failed", e)
        }
    }

    private suspend fun cached(date: LocalDate?): Apod? {
        val dao = cacheDao ?: return null
        val lookupDate = date ?: LocalDate.now()
        return runCatching {
            dao.getByDate(lookupDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                ?.toDomain()
        }.onFailure { e ->
            if (BuildConfig.DEBUG) Log.w(TAG, "cache read failed", e)
        }.getOrNull()
    }

    companion object {
        private val TRANSIENT_HTTP_CODES = setOf(502, 503, 504)
    }
}
