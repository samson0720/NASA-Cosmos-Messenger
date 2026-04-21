package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedApodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedApodEntity)

    @Query("SELECT * FROM apod_cache WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): CachedApodEntity?
}
