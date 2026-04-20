package io.github.samson0720.cosmosmessenger.data.local

import android.content.Context
import androidx.room.Room

// Lazy app-wide singleton, mirroring NetworkModule. Avoids pulling in a DI
// framework just to hand out one Room instance.
object DatabaseModule {

    private const val DB_NAME = "cosmos_messenger.db"

    @Volatile private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DB_NAME,
            ).build().also { instance = it }
        }
}
