package io.github.samson0720.cosmosmessenger.domain.repository

import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.FavoriteApod
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FavoritesRepository {
    fun observeAll(): Flow<List<FavoriteApod>>
    suspend fun save(apod: Apod): SaveResult
    suspend fun delete(date: LocalDate)
}

sealed interface SaveResult {
    data object Saved : SaveResult
    data object AlreadyExists : SaveResult
}
