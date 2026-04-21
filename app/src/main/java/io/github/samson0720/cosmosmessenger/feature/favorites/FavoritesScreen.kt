package io.github.samson0720.cosmosmessenger.feature.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.ui.CosmosTopBar
import io.github.samson0720.cosmosmessenger.ui.theme.CosmosMessengerTheme
import java.time.LocalDate

@Composable
fun FavoritesRoute(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        uiState = uiState,
        onDeleteClick = viewModel::onDeleteClick,
        onSnackbarShown = viewModel::consumeSnackbar,
        modifier = modifier,
    )
}

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onDeleteClick: (LocalDate) -> Unit,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = uiState.snackbarMessage

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
        CosmosTopBar(title = stringResource(R.string.tab_favorites))
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
                    onDeleteClick = onDeleteClick,
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun FavoritesGrid(
    items: List<FavoriteApodUiItem>,
    deletingDate: LocalDate?,
    onDeleteClick: (LocalDate) -> Unit,
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
                onDeleteClick = onDeleteClick,
                onClick = { uriHandler.openUri(item.sourceUrl) },
            )
        }
    }
}

@Composable
private fun FavoriteCard(
    item: FavoriteApodUiItem,
    isDeleting: Boolean,
    onDeleteClick: (LocalDate) -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDeleting, onClick = onClick),
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
                            .height(140.dp),
                    )
                }
                DeleteBadge(
                    enabled = !isDeleting,
                    onClick = { onDeleteClick(item.date) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color.White,
                    maxLines = 2,
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
            onSnackbarShown = {},
        )
    }
}
