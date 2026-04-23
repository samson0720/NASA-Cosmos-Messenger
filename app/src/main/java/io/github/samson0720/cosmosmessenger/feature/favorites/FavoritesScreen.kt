package io.github.samson0720.cosmosmessenger.feature.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayCardDialog
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayCardDialogState
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayCardShareHelper
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayStarCardRenderer
import io.github.samson0720.cosmosmessenger.feature.favorites.collage.FavoriteCollageRenderer
import io.github.samson0720.cosmosmessenger.feature.favorites.collage.MemoryCollageDialog
import io.github.samson0720.cosmosmessenger.feature.favorites.collage.MemoryCollageDialogState
import io.github.samson0720.cosmosmessenger.feature.favorites.collage.MemoryCollageShareHelper
import io.github.samson0720.cosmosmessenger.feature.favorites.collage.MemoryCollageTemplate
import io.github.samson0720.cosmosmessenger.ui.CosmosTopBar
import io.github.samson0720.cosmosmessenger.ui.theme.CosmosMessengerTheme
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun FavoritesRoute(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        uiState = uiState,
        onDeleteClick = viewModel::onDeleteClick,
        onFavoriteLongPress = viewModel::onFavoriteLongPress,
        onFavoriteClickInSelection = viewModel::onFavoriteClickInSelection,
        onCancelCollageSelection = viewModel::cancelCollageSelection,
        onSnackbarShown = viewModel::consumeSnackbar,
        modifier = modifier,
    )
}

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onDeleteClick: (LocalDate) -> Unit,
    onFavoriteLongPress: (LocalDate) -> Unit,
    onFavoriteClickInSelection: (LocalDate) -> Unit,
    onCancelCollageSelection: () -> Unit,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = uiState.snackbarMessage
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val renderer = remember(context.applicationContext) {
        BirthdayStarCardRenderer(context.applicationContext)
    }
    val collageRenderer = remember(context.applicationContext) {
        FavoriteCollageRenderer(context.applicationContext)
    }
    var birthdayCardState by remember {
        mutableStateOf<BirthdayCardDialogState?>(null)
    }
    var collageDialogState by remember {
        mutableStateOf<MemoryCollageDialogState?>(null)
    }
    var collageApods by remember {
        mutableStateOf<List<Apod>>(emptyList())
    }
    val onBirthdayCardClick: (FavoriteApodUiItem) -> Unit = onBirthdayCardClick@ { item ->
        if (item.mediaType != ApodMediaType.IMAGE) return@onBirthdayCardClick
        birthdayCardState = BirthdayCardDialogState.Loading
        scope.launch {
            birthdayCardState = runCatching { renderer.render(item.apod) }
                .fold(
                    onSuccess = { BirthdayCardDialogState.Ready(it) },
                    onFailure = { BirthdayCardDialogState.Error },
                )
        }
    }
    val onCreateCollageClick: () -> Unit = {
        if (uiState.canCreateCollage) {
            collageApods = uiState.selectedCollageItems.map { it.apod }
            collageDialogState = MemoryCollageDialogState.TemplateSelection
        }
    }
    val onTemplateSelected: (MemoryCollageTemplate) -> Unit = { template ->
        val apods = collageApods
        if (apods.size == FavoritesUiState.MaxCollageSelection) {
            collageDialogState = MemoryCollageDialogState.Loading
            scope.launch {
                collageDialogState = runCatching { collageRenderer.render(apods, template) }
                    .fold(
                        onSuccess = { MemoryCollageDialogState.Ready(it) },
                        onFailure = { MemoryCollageDialogState.Error },
                    )
            }
        }
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            // finally {} clears the message even if the user switches tabs
            // while showSnackbar is still suspended; FavoritesViewModel is
            // retained across tabs, so without this the stale message
            // would replay on return.
            try {
                snackbarHostState.showSnackbar(snackbarMessage)
            } finally {
                onSnackbarShown()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.isCollageSelectionMode) {
            CollageSelectionTopBar(
                selectedCount = uiState.selectedCollageCount,
                canCreateCollage = uiState.canCreateCollage,
                onCancel = onCancelCollageSelection,
                onCreateCollageClick = onCreateCollageClick,
            )
        } else {
            CosmosTopBar(title = stringResource(R.string.tab_favorites))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                uiState.items.isEmpty() -> EmptyFavorites(
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> FavoritesGrid(
                    items = uiState.items,
                    deletingDate = uiState.deletingDate,
                    isCollageSelectionMode = uiState.isCollageSelectionMode,
                    selectedDates = uiState.selectedCollageDates,
                    onDeleteClick = onDeleteClick,
                    onFavoriteLongPress = onFavoriteLongPress,
                    onFavoriteClickInSelection = onFavoriteClickInSelection,
                    onBirthdayCardClick = onBirthdayCardClick,
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }

    birthdayCardState?.let { state ->
        BirthdayCardDialog(
            state = state,
            onDismiss = { birthdayCardState = null },
            onShare = { card -> BirthdayCardShareHelper.share(context, card) },
        )
    }

    collageDialogState?.let { state ->
        MemoryCollageDialog(
            state = state,
            onDismiss = { collageDialogState = null },
            onTemplateSelected = onTemplateSelected,
            onShare = { collage -> MemoryCollageShareHelper.share(context, collage) },
        )
    }
}

@Composable
private fun CollageSelectionTopBar(
    selectedCount: Int,
    canCreateCollage: Boolean,
    onCancel: () -> Unit,
    onCreateCollageClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.collage_selection_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
                Text(
                    text = stringResource(
                        R.string.collage_selection_count,
                        selectedCount,
                        FavoritesUiState.MaxCollageSelection,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
            TextButton(onClick = onCancel) {
                Text(stringResource(android.R.string.cancel))
            }
            Button(
                enabled = canCreateCollage,
                onClick = onCreateCollageClick,
            ) {
                Text(stringResource(R.string.collage_create_cta))
            }
        }
        androidx.compose.material3.HorizontalDivider(thickness = 0.5.dp, color = Color(0x1AFFFFFF))
    }
}

@Composable
private fun FavoritesGrid(
    items: List<FavoriteApodUiItem>,
    deletingDate: LocalDate?,
    isCollageSelectionMode: Boolean,
    selectedDates: Set<LocalDate>,
    onDeleteClick: (LocalDate) -> Unit,
    onFavoriteLongPress: (LocalDate) -> Unit,
    onFavoriteClickInSelection: (LocalDate) -> Unit,
    onBirthdayCardClick: (FavoriteApodUiItem) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = items, key = { it.date.toString() }) { item ->
            FavoriteCard(
                item = item,
                isDeleting = deletingDate == item.date,
                isCollageSelectionMode = isCollageSelectionMode,
                isSelectedForCollage = item.date in selectedDates,
                onDeleteClick = onDeleteClick,
                onBirthdayCardClick = { onBirthdayCardClick(item) },
                onFavoriteLongPress = { onFavoriteLongPress(item.date) },
                onFavoriteClickInSelection = { onFavoriteClickInSelection(item.date) },
                onClick = { uriHandler.openUri(item.sourceUrl) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteCard(
    item: FavoriteApodUiItem,
    isDeleting: Boolean,
    isCollageSelectionMode: Boolean,
    isSelectedForCollage: Boolean,
    onDeleteClick: (LocalDate) -> Unit,
    onBirthdayCardClick: () -> Unit,
    onFavoriteLongPress: () -> Unit,
    onFavoriteClickInSelection: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = !isDeleting,
                onClick = {
                    if (isCollageSelectionMode) onFavoriteClickInSelection() else onClick()
                },
                onLongClick = onFavoriteLongPress,
            ),
        shape = RoundedCornerShape(16.dp),
        color = FavoriteCardColor,
        contentColor = Color.White,
        border = BorderStroke(0.5.dp, FavoriteCardBorder),
    ) {
        Column {
            Box {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(122.dp),
                    )
                }
                if (isCollageSelectionMode) {
                    CollageSelectionOverlay(
                        isSelected = isSelectedForCollage,
                        isEligible = item.isCollageEligible,
                    )
                } else {
                    DeleteBadge(
                        enabled = !isDeleting,
                        onClick = { onDeleteClick(item.date) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = FavoriteCardDateColor,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCollageSelectionMode && item.mediaType == ApodMediaType.IMAGE) {
                    Spacer(Modifier.height(4.dp))
                    BirthdayCardAction(
                        enabled = !isDeleting,
                        onClick = onBirthdayCardClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollageSelectionOverlay(
    isSelected: Boolean,
    isEligible: Boolean,
) {
    val overlayColor = when {
        isSelected -> Color(0x6636D399)
        isEligible -> Color(0x33000000)
        else -> Color(0x99000000)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(122.dp)
            .background(overlayColor),
    ) {
        if (isSelected) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = CircleShape,
                color = Color(0xFF36D399),
                contentColor = Color(0xFF06111A),
            ) {
                Text(
                    text = stringResource(R.string.collage_selected_mark),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        } else if (!isEligible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
                shape = RoundedCornerShape(50),
                color = Color(0xCC000000),
                contentColor = Color.White,
            ) {
                Text(
                    text = stringResource(R.string.collage_video_not_supported),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun BirthdayCardAction(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = stringResource(R.string.birthday_card_action_short),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DeleteBadge(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A plain clickable Box (not FilledIconButton) so the badge can sit at a compact
    // 32dp — the Material IconButton tokens enforce a 40dp hit target that's too big
    // for a thumbnail overlay.
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(DeleteBadgeBackground)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = stringResource(R.string.fav_delete),
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun EmptyFavorites(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.favorites_empty_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.favorites_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}

private val FavoriteCardColor = Color(0xCC1E2547)
private val FavoriteCardBorder = Color(0x3074B9FF)
private val FavoriteCardDateColor = Color(0xFF74B9FF).copy(alpha = 0.8f)
private val DeleteBadgeBackground = Color(0x66000000)

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    CosmosMessengerTheme {
        FavoritesScreen(
            uiState = FavoritesUiState(
                items = listOf(
                    FavoriteApodUiItem(
                        apod = Apod(
                            date = LocalDate.of(2024, 1, 1),
                            title = "Andromeda Galaxy",
                            explanation = "The Andromeda Galaxy is the closest spiral galaxy to our own Milky Way.",
                            mediaType = ApodMediaType.IMAGE,
                            url = "https://apod.nasa.gov/",
                            hdUrl = null,
                        ),
                        date = LocalDate.of(2024, 1, 1),
                        displayDate = "2024/01/01",
                        savedAtText = "2026/04/20",
                        title = "Andromeda Galaxy",
                        explanation = "The Andromeda Galaxy is the closest spiral galaxy to our own Milky Way.",
                        mediaType = ApodMediaType.IMAGE,
                        imageUrl = null,
                        sourceUrl = "https://apod.nasa.gov/",
                    ),
                ),
                isLoading = false,
            ),
            onDeleteClick = {},
            onFavoriteLongPress = {},
            onFavoriteClickInSelection = {},
            onCancelCollageSelection = {},
            onSnackbarShown = {},
        )
    }
}
