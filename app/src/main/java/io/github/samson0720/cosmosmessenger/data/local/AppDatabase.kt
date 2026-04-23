package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Favorites, offline cache, and chat history stay in separate tables because
// they answer different product questions: explicit saves, repository fallback,
// and UI session restoration.
@Database(
    entities = [FavoriteApodEntity::class, CachedApodEntity::class, ChatMessageEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteApodDao(): FavoriteApodDao
    abstract fun cachedApodDao(): CachedApodDao
    abstract fun chatMessageDao(): ChatMessageDao
}
