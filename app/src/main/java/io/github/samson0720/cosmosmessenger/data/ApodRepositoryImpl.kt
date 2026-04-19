package io.github.samson0720.cosmosmessenger.data

import android.util.Log
import io.github.samson0720.cosmosmessenger.BuildConfig
import io.github.samson0720.cosmosmessenger.data.mapper.toDomain
import io.github.samson0720.cosmosmessenger.data.remote.ApodService
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "ApodRepo"

class ApodRepositoryImpl(
    private val service: ApodService,
    private val apiKey: String,
) : ApodRepository {

    override suspend fun getApod(date: LocalDate?): Result<Apod> = try {
        val dateParam = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dto = service.getApod(apiKey = apiKey, date = dateParam)
        Result.success(dto.toDomain())
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
        Result.failure(ApodException.Network)
    } catch (e: CancellationException) {
        // Keep structured concurrency intact — never convert cancellation
        // into an error reply.
        throw e
    } catch (e: Throwable) {
        if (BuildConfig.DEBUG) Log.w(TAG, "Unknown ${e.javaClass.simpleName}")
        Result.failure(ApodException.Unknown(e))
    }
}
