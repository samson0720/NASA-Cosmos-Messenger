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
import io.github.samson0720.cosmosmessenger.domain.model.Apod
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
    val selectedCollageDates: Set<LocalDate> = emptySet(),
    val snackbarMessage: String? = null,
) {
    val isCollageSelectionMode: Boolean
        get() = selectedCollageDates.isNotEmpty()

    val selectedCollageCount: Int
        get() = selectedCollageDates.size

    val canCreateCollage: Boolean
        get() = selectedCollageDates.size == MaxCollageSelection

    val selectedCollageItems: List<FavoriteApodUiItem>
        get() = items.filter { it.date in selectedCollageDates }

    companion object {
        const val MaxCollageSelection = 3
    }
}

data class FavoriteApodUiItem(
    val apod: Apod,
    val date: LocalDate,
    val displayDate: String,
    val savedAtText: String,
    val title: String,
    val explanation: String,
    val mediaType: ApodMediaType,
    val imageUrl: String?,
    val sourceUrl: String,
) {
    val isCollageEligible: Boolean
        get() = mediaType == ApodMediaType.IMAGE
}

fun interface FavoritesStringProvider {
    fun getString(resId: Int): String
}

private class AndroidFavoritesStringProvider(
    private val application: Application,
) : FavoritesStringProvider {
    override fun getString(resId: Int): String = application.getString(resId)
}

class FavoritesViewModel(
    application: Application,
    private val repository: FavoritesRepository,
    private val stringProvider: FavoritesStringProvider = AndroidFavoritesStringProvider(application),
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
                    val items = favorites.map { favorite -> favorite.toUiItem() }
                    _uiState.update {
                        val validSelectedDates = it.selectedCollageDates
                            .filter { date -> items.any { item -> item.date == date && item.isCollageEligible } }
                            .toSet()
                        it.copy(
                            items = items,
                            isLoading = false,
                            selectedCollageDates = validSelectedDates,
                        )
                    }
                }
        }
    }

    fun onDeleteClick(date: LocalDate) {
        if (_uiState.value.deletingDate != null || _uiState.value.isCollageSelectionMode) return

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

    fun onFavoriteLongPress(date: LocalDate) {
        val item = _uiState.value.items.firstOrNull { it.date == date } ?: return
        if (!item.isCollageEligible) {
            _uiState.update {
                it.copy(snackbarMessage = appString(R.string.collage_image_only))
            }
            return
        }
        _uiState.update {
            it.copy(selectedCollageDates = setOf(date))
        }
    }

    fun onFavoriteClickInSelection(date: LocalDate) {
        val state = _uiState.value
        if (!state.isCollageSelectionMode) return

        val item = state.items.firstOrNull { it.date == date } ?: return
        if (!item.isCollageEligible) {
            _uiState.update {
                it.copy(snackbarMessage = appString(R.string.collage_image_only))
            }
            return
        }

        _uiState.update {
            val selected = it.selectedCollageDates
            val nextSelected = when {
                date in selected -> selected - date
                selected.size >= FavoritesUiState.MaxCollageSelection -> selected
                else -> selected + date
            }
            it.copy(
                selectedCollageDates = nextSelected,
                snackbarMessage = if (
                    date !in selected &&
                    selected.size >= FavoritesUiState.MaxCollageSelection
                ) {
                    appString(R.string.collage_max_selected)
                } else {
                    it.snackbarMessage
                },
            )
        }
    }

    fun cancelCollageSelection() {
        _uiState.update { it.copy(selectedCollageDates = emptySet()) }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun FavoriteApod.toUiItem(): FavoriteApodUiItem {
        val apodItem = this.apod
        return FavoriteApodUiItem(
            apod = apodItem,
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
        stringProvider.getString(resId)

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
