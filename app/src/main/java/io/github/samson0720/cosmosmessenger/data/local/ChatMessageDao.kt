package io.github.samson0720.cosmosmessenger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ChatMessageDao {

    @Query("SELECT * FROM chat_message ORDER BY sortOrder ASC")
    abstract suspend fun getAll(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(rows: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_message")
    abstract suspend fun clearAll()

    @Transaction
    open suspend fun replaceAll(rows: List<ChatMessageEntity>) {
        clearAll()
        if (rows.isNotEmpty()) {
            insertAll(rows)
        }
    }
}
