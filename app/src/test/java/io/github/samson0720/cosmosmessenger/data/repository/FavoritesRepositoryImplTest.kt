package io.github.samson0720.cosmosmessenger.data.repository

import io.github.samson0720.cosmosmessenger.data.local.FavoriteApodDao
import io.github.samson0720.cosmosmessenger.data.local.FavoriteApodEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.repository.SaveResult
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesRepositoryImplTest {

    @Test
    fun observeAll_mapsDaoEntitiesToDomainFavorites() = runTest {
        val savedAt = Instant.parse("2026-04-21T12:00:00Z")
        val dao = FakeFavoriteApodDao(
            initialRows = listOf(
                favoriteEntity(
                    date = "2024-01-02",
                    mediaType = "VIDEO",
                    savedAt = savedAt.toEpochMilli(),
                ),
            ),
        )
        val repository = FavoritesRepositoryImpl(dao)

        val favorites = repository.observeAll().first()

        assertEquals(1, favorites.size)
        val favorite = favorites.single()
        assertEquals(LocalDate.of(2024, 1, 2), favorite.apod.date)
        assertEquals("Sample title", favorite.apod.title)
        assertEquals(ApodMediaType.VIDEO, favorite.apod.mediaType)
        assertEquals(savedAt, favorite.savedAt)
    }

    @Test
    fun save_insertedRow_returnsSavedAndStoresMappedEntity() = runTest {
        val dao = FakeFavoriteApodDao(insertResult = 7L)
        val repository = FavoritesRepositoryImpl(dao)
        val beforeSave = Instant.now().toEpochMilli()

        val result = repository.save(sampleApod())

        assertEquals(SaveResult.Saved, result)
        val inserted = dao.insertedRows.single()
        assertEquals("2024-01-02", inserted.date)
        assertEquals("Sample title", inserted.title)
        assertEquals("Sample explanation", inserted.explanation)
        assertEquals("IMAGE", inserted.mediaType)
        assertEquals("https://example.com/image.jpg", inserted.url)
        assertEquals("https://example.com/hd.jpg", inserted.hdUrl)
        assertTrue(inserted.savedAt >= beforeSave)
    }

    @Test
    fun save_duplicatePrimaryKey_returnsAlreadyExists() = runTest {
        val repository = FavoritesRepositoryImpl(FakeFavoriteApodDao(insertResult = -1L))

        val result = repository.save(sampleApod())

        assertEquals(SaveResult.AlreadyExists, result)
    }

    @Test
    fun delete_formatsLocalDateAndDelegatesToDao() = runTest {
        val dao = FakeFavoriteApodDao()
        val repository = FavoritesRepositoryImpl(dao)

        repository.delete(LocalDate.of(2024, 1, 2))

        assertEquals(listOf("2024-01-02"), dao.deletedDates)
    }

    private class FakeFavoriteApodDao(
        initialRows: List<FavoriteApodEntity> = emptyList(),
        private val insertResult: Long = 1L,
    ) : FavoriteApodDao {
        private val rows = MutableStateFlow(initialRows)
        val insertedRows = mutableListOf<FavoriteApodEntity>()
        val deletedDates = mutableListOf<String>()

        override suspend fun insert(entity: FavoriteApodEntity): Long {
            insertedRows += entity
            if (insertResult != -1L) {
                rows.value = listOf(entity) + rows.value.filterNot { it.date == entity.date }
            }
            return insertResult
        }

        override fun observeAll(): Flow<List<FavoriteApodEntity>> = rows

        override suspend fun deleteByDate(date: String) {
            deletedDates += date
            rows.value = rows.value.filterNot { it.date == date }
        }
    }

    private fun sampleApod(
        date: LocalDate = LocalDate.of(2024, 1, 2),
        mediaType: ApodMediaType = ApodMediaType.IMAGE,
    ): Apod = Apod(
        date = date,
        title = "Sample title",
        explanation = "Sample explanation",
        mediaType = mediaType,
        url = "https://example.com/image.jpg",
        hdUrl = "https://example.com/hd.jpg",
    )

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
