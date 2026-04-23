package io.github.samson0720.cosmosmessenger.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CachedApodDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CachedApodDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.cachedApodDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_newCachedApod_canBeReadByDate() = runTest {
        val entity = cachedEntity(date = "2024-01-02")

        dao.upsert(entity)

        assertEquals(entity, dao.getByDate("2024-01-02"))
    }

    @Test
    fun upsert_existingDate_replacesPreviousCachedApod() = runTest {
        val original = cachedEntity(
            date = "2024-01-02",
            title = "Original title",
            cachedAt = 100L,
        )
        val replacement = cachedEntity(
            date = "2024-01-02",
            title = "Replacement title",
            cachedAt = 200L,
        )

        dao.upsert(original)
        dao.upsert(replacement)

        assertEquals(replacement, dao.getByDate("2024-01-02"))
    }

    @Test
    fun getByDate_missingDate_returnsNull() = runTest {
        dao.upsert(cachedEntity(date = "2024-01-02"))

        assertNull(dao.getByDate("2024-01-03"))
    }

    @Test
    fun getByDate_keepsCacheSeparateFromFavoritesTable() = runTest {
        val favorite = favoriteEntity(date = "2024-01-02")
        database.favoriteApodDao().insert(favorite)

        assertNull(dao.getByDate("2024-01-02"))
        assertEquals(listOf(favorite), database.favoriteApodDao().observeAll().first())
    }

    private fun cachedEntity(
        date: String,
        title: String = "Sample title",
        explanation: String = "Sample explanation",
        mediaType: String = "IMAGE",
        url: String = "https://example.com/image.jpg",
        hdUrl: String? = "https://example.com/hd.jpg",
        cachedAt: Long = 100L,
    ): CachedApodEntity = CachedApodEntity(
        date = date,
        title = title,
        explanation = explanation,
        mediaType = mediaType,
        url = url,
        hdUrl = hdUrl,
        cachedAt = cachedAt,
    )

    private fun favoriteEntity(
        date: String,
        title: String = "Favorite title",
        explanation: String = "Favorite explanation",
        mediaType: String = "IMAGE",
        url: String = "https://example.com/favorite.jpg",
        hdUrl: String? = "https://example.com/favorite-hd.jpg",
        savedAt: Long = 200L,
    ): FavoriteApodEntity = FavoriteApodEntity(
        date = date,
        title = title,
        explanation = explanation,
        mediaType = mediaType,
        url = url,
        hdUrl = hdUrl,
        savedAt = savedAt,
    )
}
