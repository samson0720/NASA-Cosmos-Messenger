package io.github.samson0720.cosmosmessenger.data

import io.github.samson0720.cosmosmessenger.data.remote.NovaGuideService
import io.github.samson0720.cosmosmessenger.data.remote.toDomainOrNull
import io.github.samson0720.cosmosmessenger.data.remote.toNovaGuideRequestDto
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.NovaGuide
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class NovaGuideRepositoryImpl(
    private val service: NovaGuideService,
    endpoint: String,
) : NovaGuideRepository {

    private val endpoint = endpoint.trim()

    override suspend fun explain(apod: Apod): Result<NovaGuide> {
        if (endpoint.isEmpty()) {
            return Result.failure(NovaGuideException.NotConfigured)
        }

        return try {
            val guide = service.explain(endpoint, apod.toNovaGuideRequestDto())
                .toDomainOrNull()
                ?: return Result.failure(NovaGuideException.InvalidResponse)
            Result.success(guide)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(NovaGuideException.Network)
        } catch (e: HttpException) {
            Result.failure(NovaGuideException.ServiceUnavailable)
        } catch (e: IllegalArgumentException) {
            Result.failure(NovaGuideException.NotConfigured)
        } catch (e: RuntimeException) {
            Result.failure(NovaGuideException.InvalidResponse)
        }
    }
}
