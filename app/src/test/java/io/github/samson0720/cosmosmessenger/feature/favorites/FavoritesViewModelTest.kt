package io.github.samson0720.cosmosmessenger.feature.favorites

import android.app.Application
import io.github.samson0720.cosmosmessenger.MainDispatcherRule
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.FavoriteApod
import io.github.samson0720.cosmosmessenger.domain.repository.FavoritesRepository
import io.github.samson0720.cosmosmessenger.domain.repository.SaveResult
import io.github.samson0720.cosmosmessenger.util.ApodDateParser
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialFavoritesFlow_updatesUiStateWithItems() {
        val favorite = sampleFavorite()
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = listOf(favorite)),
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.deletingDate)
        assertNull(state.snackbarMessage)
        assertEquals(1, state.items.size)

        val item = state.items.single()
        assertEquals(favorite.apod, item.apod)
        assertEquals(favorite.apod.date, item.date)
        assertEquals("2024/01/02", item.displayDate)
        assertEquals(
            ApodDateParser.formatForDisplay(
                favorite.savedAt.atZone(ZoneId.systemDefault()).toLocalDate(),
            ),
            item.savedAtText,
        )
        assertEquals(favorite.apod.title, item.title)
        assertEquals(favorite.apod.explanation, item.explanation)
        assertEquals(ApodMediaType.IMAGE, item.mediaType)
        assertEquals(favorite.apod.url, item.imageUrl)
        assertEquals(favorite.apod.hdUrl, item.sourceUrl)
    }

    @Test
    fun emptyFavoritesState_isRepresentedCorrectly() {
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = emptyList()),
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.items.isEmpty())
        assertNull(state.deletingDate)
        assertNull(state.snackbarMessage)
    }

    @Test
    fun onDeleteClick_success_setsRemovedSnackbarAndClearsDeletingDate() = runTest {
        val favorite = sampleFavorite()
        val repository = FakeFavoritesRepository(initialFavorites = listOf(favorite))
        val viewModel = newViewModel(repository = repository)

        viewModel.onDeleteClick(favorite.apod.date)

        val state = viewModel.uiState.value
        assertEquals(listOf(favorite.apod.date), repository.deleteCalls)
        assertNull(state.deletingDate)
        assertEquals(stringFor(R.string.fav_removed), state.snackbarMessage)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun onDeleteClick_failure_setsDeleteFailedSnackbarAndClearsDeletingDate() = runTest {
        val favorite = sampleFavorite()
        val repository = FakeFavoritesRepository(
            initialFavorites = listOf(favorite),
            deleteFailure = IllegalStateException("delete failed"),
        )
        val viewModel = newViewModel(repository = repository)

        viewModel.onDeleteClick(favorite.apod.date)

        val state = viewModel.uiState.value
        assertEquals(listOf(favorite.apod.date), repository.deleteCalls)
        assertNull(state.deletingDate)
        assertEquals(stringFor(R.string.fav_delete_failed), state.snackbarMessage)
        assertEquals(1, state.items.size)
    }

    @Test
    fun consumeSnackbar_clearsSnackbarMessage() = runTest {
        val favorite = sampleFavorite()
        val repository = FakeFavoritesRepository(initialFavorites = listOf(favorite))
        val viewModel = newViewModel(repository = repository)
        viewModel.onDeleteClick(favorite.apod.date)
        assertEquals(stringFor(R.string.fav_removed), viewModel.uiState.value.snackbarMessage)

        viewModel.consumeSnackbar()

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun onFavoriteLongPress_image_entersSelectionModeAndSelectsFirstItem() {
        val favorite = sampleFavorite(date = LocalDate.of(2024, 1, 2))
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = listOf(favorite)),
        )

        viewModel.onFavoriteLongPress(favorite.apod.date)

        val state = viewModel.uiState.value
        assertTrue(state.isCollageSelectionMode)
        assertEquals(setOf(favorite.apod.date), state.selectedCollageDates)
        assertEquals(1, state.selectedCollageCount)
        assertFalse(state.canCreateCollage)
    }

    @Test
    fun onFavoriteLongPress_video_doesNotEnterSelectionModeAndShowsHint() {
        val favorite = sampleFavorite(
            date = LocalDate.of(2024, 1, 2),
            mediaType = ApodMediaType.VIDEO,
        )
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = listOf(favorite)),
        )

        viewModel.onFavoriteLongPress(favorite.apod.date)

        val state = viewModel.uiState.value
        assertFalse(state.isCollageSelectionMode)
        assertTrue(state.selectedCollageDates.isEmpty())
        assertEquals(stringFor(R.string.collage_image_only), state.snackbarMessage)
    }

    @Test
    fun onFavoriteClickInSelection_togglesSelectedImage() {
        val first = sampleFavorite(date = LocalDate.of(2024, 1, 1))
        val second = sampleFavorite(date = LocalDate.of(2024, 1, 2))
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = listOf(first, second)),
        )
        viewModel.onFavoriteLongPress(first.apod.date)

        viewModel.onFavoriteClickInSelection(second.apod.date)
        assertEquals(
            setOf(first.apod.date, second.apod.date),
            viewModel.uiState.value.selectedCollageDates,
        )

        viewModel.onFavoriteClickInSelection(second.apod.date)
        assertEquals(setOf(first.apod.date), viewModel.uiState.value.selectedCollageDates)
    }

    @Test
    fun onFavoriteClickInSelection_blocksSelectingMoreThanThree() {
        val favorites = (1..4).map { day ->
            sampleFavorite(date = LocalDate.of(2024, 1, day))
        }
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = favorites),
        )
        viewModel.onFavoriteLongPress(favorites[0].apod.date)
        viewModel.onFavoriteClickInSelection(favorites[1].apod.date)
        viewModel.onFavoriteClickInSelection(favorites[2].apod.date)

        viewModel.onFavoriteClickInSelection(favorites[3].apod.date)

        val state = viewModel.uiState.value
        assertEquals(
            setOf(
                favorites[0].apod.date,
                favorites[1].apod.date,
                favorites[2].apod.date,
            ),
            state.selectedCollageDates,
        )
        assertEquals(stringFor(R.string.collage_max_selected), state.snackbarMessage)
    }

    @Test
    fun canCreateCollage_isEnabledOnlyAtThreeSelectedImages() {
        val favorites = (1..3).map { day ->
            sampleFavorite(date = LocalDate.of(2024, 1, day))
        }
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = favorites),
        )

        viewModel.onFavoriteLongPress(favorites[0].apod.date)
        assertFalse(viewModel.uiState.value.canCreateCollage)

        viewModel.onFavoriteClickInSelection(favorites[1].apod.date)
        assertFalse(viewModel.uiState.value.canCreateCollage)

        viewModel.onFavoriteClickInSelection(favorites[2].apod.date)
        assertTrue(viewModel.uiState.value.canCreateCollage)
        assertEquals(3, viewModel.uiState.value.selectedCollageItems.size)
    }

    @Test
    fun cancelCollageSelection_clearsSelectedItems() {
        val favorite = sampleFavorite(date = LocalDate.of(2024, 1, 2))
        val viewModel = newViewModel(
            repository = FakeFavoritesRepository(initialFavorites = listOf(favorite)),
        )
        viewModel.onFavoriteLongPress(favorite.apod.date)

        viewModel.cancelCollageSelection()

        val state = viewModel.uiState.value
        assertFalse(state.isCollageSelectionMode)
        assertTrue(state.selectedCollageDates.isEmpty())
    }

    private fun newViewModel(
        repository: FakeFavoritesRepository = FakeFavoritesRepository(),
    ): FavoritesViewModel = FavoritesViewModel(
        application = Application(),
        repository = repository,
        stringProvider = FavoritesStringProvider { resId -> stringFor(resId) },
    )

    private class FakeFavoritesRepository(
        initialFavorites: List<FavoriteApod> = emptyList(),
        private val deleteFailure: Throwable? = null,
    ) : FavoritesRepository {
        private val favorites = MutableStateFlow(initialFavorites)
        val deleteCalls = mutableListOf<LocalDate>()

        override fun observeAll(): Flow<List<FavoriteApod>> = favorites

        override suspend fun save(apod: Apod): SaveResult = SaveResult.Saved

        override suspend fun delete(date: LocalDate) {
            deleteCalls += date
            deleteFailure?.let { throw it }
            favorites.value = favorites.value.filterNot { it.apod.date == date }
        }
    }

    private companion object {
        fun sampleFavorite(
            date: LocalDate = LocalDate.of(2024, 1, 2),
            mediaType: ApodMediaType = ApodMediaType.IMAGE,
            hdUrl: String? = "https://example.com/hd.jpg",
        ): FavoriteApod = FavoriteApod(
            apod = Apod(
                date = date,
                title = "Sample title",
                explanation = "Sample explanation",
                mediaType = mediaType,
                url = "https://example.com/image.jpg",
                hdUrl = hdUrl,
            ),
            savedAt = Instant.parse("2026-04-21T12:00:00Z"),
        )

        fun stringFor(resId: Int): String = "string-$resId"
    }
}
