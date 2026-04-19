package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.remote.ApodDto
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import java.time.LocalDate

fun ApodDto.toDomain(): Apod = Apod(
    date = LocalDate.parse(date),
    title = title,
    explanation = explanation,
    mediaType = when (mediaType.lowercase()) {
        "image" -> ApodMediaType.IMAGE
        "video" -> ApodMediaType.VIDEO
        else -> ApodMediaType.OTHER
    },
    url = url,
    hdUrl = hdurl,
)
