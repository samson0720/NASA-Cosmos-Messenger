package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.local.CachedApodEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.ApodSource
import java.time.Instant
import java.time.LocalDate

fun Apod.toCachedEntity(cachedAt: Instant): CachedApodEntity = CachedApodEntity(
    date = date.toString(),
    title = title,
    explanation = explanation,
    mediaType = mediaType.name,
    url = url,
    hdUrl = hdUrl,
    cachedAt = cachedAt.toEpochMilli(),
)

fun CachedApodEntity.toDomain(): Apod = Apod(
    date = LocalDate.parse(date),
    title = title,
    explanation = explanation,
    mediaType = runCatching { ApodMediaType.valueOf(mediaType) }
        .getOrDefault(ApodMediaType.OTHER),
    url = url,
    hdUrl = hdUrl,
    source = ApodSource.CACHE,
)
