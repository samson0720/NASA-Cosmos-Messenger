package io.github.samson0720.cosmosmessenger.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `apod_cache` (
                    `date` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `explanation` TEXT NOT NULL,
                    `mediaType` TEXT NOT NULL,
                    `url` TEXT NOT NULL,
                    `hdUrl` TEXT,
                    `cachedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`date`)
                )
                """.trimIndent(),
            )
        }
    }
}
