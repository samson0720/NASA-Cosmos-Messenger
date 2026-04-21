package io.github.samson0720.cosmosmessenger.domain.model

import java.time.LocalDate

enum class ApodMediaType { IMAGE, VIDEO, OTHER }

enum class ApodSource { NETWORK, CACHE }

data class Apod(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val mediaType: ApodMediaType,
    val url: String,
    val hdUrl: String?,
    val source: ApodSource = ApodSource.NETWORK,
)
