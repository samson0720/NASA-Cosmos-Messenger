package io.github.samson0720.cosmosmessenger.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface NovaGuideService {
    @POST
    suspend fun explain(
        @Url endpoint: String,
        @Body request: NovaGuideRequestDto,
    ): NovaGuideDto
}
