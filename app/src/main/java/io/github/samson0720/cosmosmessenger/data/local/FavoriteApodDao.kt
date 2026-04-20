package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteApodDao {

    // Returns the inserted rowId, or -1L when IGNORE skipped a duplicate
    // (PK collision on `date`). Callers can map -1L to "already favorited"
    // without an extra SELECT round-trip.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteApodEntity): Long

    @Query("SELECT * FROM favorite_apod ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteApodEntity>>

    @Query("DELETE FROM favorite_apod WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
