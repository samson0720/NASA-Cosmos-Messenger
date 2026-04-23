package io.github.samson0720.cosmosmessenger.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoriteApodDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavoriteApodDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.favoriteApodDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_newFavorite_emitsInsertedRow() = runTest {
        val entity = favoriteEntity(date = "2024-01-02")

        val rowId = dao.insert(entity)
        val rows = dao.observeAll().first()

        assertTrue(rowId > 0)
        assertEquals(listOf(entity), rows)
    }

    @Test
    fun insert_duplicateDate_returnsMinusOneAndKeepsOriginalRow() = runTest {
        val original = favoriteEntity(
            date = "2024-01-02",
            title = "Original title",
            savedAt = 200L,
        )
        val duplicate = favoriteEntity(
            date = "2024-01-02",
            title = "Duplicate title",
            savedAt = 300L,
        )

        val firstRowId = dao.insert(original)
        val duplicateRowId = dao.insert(duplicate)
        val rows = dao.observeAll().first()

        assertTrue(firstRowId > 0)
        assertEquals(-1L, duplicateRowId)
        assertEquals(listOf(original), rows)
    }

    @Test
    fun observeAll_ordersFavoritesBySavedAtDescending() = runTest {
        val older = favoriteEntity(date = "2024-01-01", savedAt = 100L)
        val newer = favoriteEntity(date = "2024-01-02", savedAt = 300L)
        val middle = favoriteEntity(date = "2024-01-03", savedAt = 200L)

        dao.insert(older)
        dao.insert(newer)
        dao.insert(middle)

        assertEquals(listOf(newer, middle, older), dao.observeAll().first())
    }

    @Test
    fun deleteByDate_removesOnlyMatchingFavorite() = runTest {
        val keep = favoriteEntity(date = "2024-01-01", savedAt = 100L)
        val remove = favoriteEntity(date = "2024-01-02", savedAt = 200L)
        dao.insert(keep)
        dao.insert(remove)

        dao.deleteByDate("2024-01-02")

        assertEquals(listOf(keep), dao.observeAll().first())
    }

    private fun favoriteEntity(
        date: String,
        title: String = "Sample title",
        explanation: String = "Sample explanation",
        mediaType: String = "IMAGE",
        url: String = "https://example.com/image.jpg",
        hdUrl: String? = "https://example.com/hd.jpg",
        savedAt: Long = 100L,
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
