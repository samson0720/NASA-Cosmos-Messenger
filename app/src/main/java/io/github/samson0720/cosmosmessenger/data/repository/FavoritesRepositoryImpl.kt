package io.github.samson0720.cosmosmessenger.data.repository

import io.github.samson0720.cosmosmessenger.data.local.FavoriteApodDao
import io.github.samson0720.cosmosmessenger.data.mapper.toDomain
import io.github.samson0720.cosmosmessenger.data.mapper.toFavoriteEntity
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.FavoriteApod
import io.github.samson0720.cosmosmessenger.domain.repository.FavoritesRepository
import io.github.samson0720.cosmosmessenger.domain.repository.SaveResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate

class FavoritesRepositoryImpl(
    private val dao: FavoriteApodDao,
) : FavoritesRepository {

    override fun observeAll(): Flow<List<FavoriteApod>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun save(apod: Apod): SaveResult {
        // OnConflictStrategy.IGNORE returns -1L when the row already
        // exists (PK collision on `date`). That is our duplicate signal.
        val rowId = dao.insert(apod.toFavoriteEntity(savedAt = Instant.now()))
        return if (rowId == -1L) SaveResult.AlreadyExists else SaveResult.Saved
    }

    override suspend fun delete(date: LocalDate) {
        dao.deleteByDate(date.toString())
    }
}
