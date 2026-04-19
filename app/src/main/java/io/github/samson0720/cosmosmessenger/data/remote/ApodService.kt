package io.github.samson0720.cosmosmessenger.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApodService {

    @GET("planetary/apod")
    suspend fun getApod(
        @Query("api_key") apiKey: String,
        @Query("date") date: String? = null,
    ): ApodDto
}
