package io.github.samson0720.cosmosmessenger.feature.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.data.local.DatabaseModule
import io.github.samson0720.cosmosmessenger.data.repository.FavoritesRepositoryImpl
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.FavoriteApod
import io.github.samson0720.cosmosmessenger.domain.repository.FavoritesRepository
import io.github.samson0720.cosmosmessenger.util.ApodDateParser
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val items: List<FavoriteApodUiItem> = emptyList(),
    val isLoading: Boolean = true,
    val deletingDate: LocalDate? = null,
    val snackbarMessage: String? = null,
)

data class FavoriteApodUiItem(
    val date: LocalDate,
    val displayDate: String,
    val savedAtText: String,
    val title: String,
    val explanation: String,
    val mediaType: ApodMediaType,
    val imageUrl: String?,
    val sourceUrl: String,
)

class FavoritesViewModel(
    application: Application,
    private val repository: FavoritesRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll()
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            snackbarMessage = appString(R.string.fav_load_failed),
                        )
                    }
                }
                .collect { favorites ->
                    _uiState.update {
                        it.copy(
                            items = favorites.map { favorite -> favorite.toUiItem() },
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onDeleteClick(date: LocalDate) {
        if (_uiState.value.deletingDate != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(deletingDate = date) }
            runCatching { repository.delete(date) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            deletingDate = null,
                            snackbarMessage = appString(R.string.fav_removed),
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            deletingDate = null,
                            snackbarMessage = appString(R.string.fav_delete_failed),
                        )
                    }
                }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun FavoriteApod.toUiItem(): FavoriteApodUiItem {
        val apodItem = this.apod
        return FavoriteApodUiItem(
            date = apodItem.date,
            displayDate = ApodDateParser.formatForDisplay(apodItem.date),
            savedAtText = ApodDateParser.formatForDisplay(
                savedAt.atZone(ZoneId.systemDefault()).toLocalDate(),
            ),
            title = apodItem.title,
            explanation = apodItem.explanation,
            mediaType = apodItem.mediaType,
            imageUrl = if (apodItem.mediaType == ApodMediaType.IMAGE) apodItem.url else null,
            sourceUrl = apodItem.hdUrl ?: apodItem.url,
        )
    }

    private fun appString(resId: Int): String =
        getApplication<Application>().getString(resId)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as Application
                FavoritesViewModel(
                    application = app,
                    repository = FavoritesRepositoryImpl(
                        dao = DatabaseModule.get(app).favoriteApodDao(),
                    ),
                )
            }
        }
    }
}
