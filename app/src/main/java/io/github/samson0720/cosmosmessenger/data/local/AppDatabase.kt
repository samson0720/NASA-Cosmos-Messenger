package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Favorites and (future) offline cache must remain structurally separate:
// they answer different product questions — favorites are explicit user
// intent, cache is transient repository state. Bonus 1 should add its own
// entity + DAO here, never widen `favorite_apod`.
@Database(
    entities = [FavoriteApodEntity::class, CachedApodEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteApodDao(): FavoriteApodDao
    abstract fun cachedApodDao(): CachedApodDao
}
