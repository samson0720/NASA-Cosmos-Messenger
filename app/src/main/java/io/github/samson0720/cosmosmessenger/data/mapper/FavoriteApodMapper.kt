package io.github.samson0720.cosmosmessenger.data.mapper

import io.github.samson0720.cosmosmessenger.data.local.FavoriteApodEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.FavoriteApod
import java.time.Instant
import java.time.LocalDate

// Keep entity field types primitive (String, Long) so Room needs no
// TypeConverters. The domain ↔ entity translation lives here and only here.
fun Apod.toFavoriteEntity(savedAt: Instant): FavoriteApodEntity = FavoriteApodEntity(
    date = date.toString(),
    title = title,
    explanation = explanation,
    mediaType = mediaType.name,
    url = url,
    hdUrl = hdUrl,
    savedAt = savedAt.toEpochMilli(),
)

fun FavoriteApodEntity.toDomain(): FavoriteApod = FavoriteApod(
    apod = Apod(
        date = LocalDate.parse(date),
        title = title,
        explanation = explanation,
        // Defensive default: an unrecognized value (e.g. legacy row) maps
        // to OTHER instead of crashing the favorites list.
        mediaType = runCatching { ApodMediaType.valueOf(mediaType) }
            .getOrDefault(ApodMediaType.OTHER),
        url = url,
        hdUrl = hdUrl,
    ),
    savedAt = Instant.ofEpochMilli(savedAt),
)
