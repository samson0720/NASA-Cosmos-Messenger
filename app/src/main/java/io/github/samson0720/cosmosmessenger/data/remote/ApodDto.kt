package io.github.samson0720.cosmosmessenger.data.remote

import com.squareup.moshi.Json

data class ApodDto(
    @Json(name = "date") val date: String,
    @Json(name = "title") val title: String,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "url") val url: String,
    @Json(name = "hdurl") val hdurl: String? = null,
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "copyright") val copyright: String? = null,
)
