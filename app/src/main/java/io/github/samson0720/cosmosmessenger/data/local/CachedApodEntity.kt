package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Repository-owned APOD cache. This is intentionally separate from favorites:
// favorites are explicit user intent, while cache rows are automatic offline
// support for previously loaded dates.
@Entity(tableName = "apod_cache")
data class CachedApodEntity(
    @PrimaryKey val date: String,
    val title: String,
    val explanation: String,
    val mediaType: String,
    val url: String,
    val hdUrl: String?,
    val cachedAt: Long,
)
