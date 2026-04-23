package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Favorites and offline cache stay in separate tables because they answer
// different product questions: explicit saves and repository fallback.
@Database(
    entities = [FavoriteApodEntity::class, CachedApodEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteApodDao(): FavoriteApodDao
    abstract fun cachedApodDao(): CachedApodDao
}
