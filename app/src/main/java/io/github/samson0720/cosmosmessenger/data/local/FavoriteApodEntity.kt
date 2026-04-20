package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// One APOD per calendar day, so the ISO date string is its natural identity.
// Using it as the PK lets duplicate-favorite handling fall out of
// OnConflictStrategy.IGNORE instead of UI-side guards.
@Entity(tableName = "favorite_apod")
data class FavoriteApodEntity(
    @PrimaryKey val date: String,
    val title: String,
    val explanation: String,
    val mediaType: String,
    val url: String,
    val hdUrl: String?,
    val savedAt: Long,
)
