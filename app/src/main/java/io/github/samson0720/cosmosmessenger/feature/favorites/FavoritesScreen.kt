package io.github.samson0720.cosmosmessenger.feature.favorites

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

                else -> FavoritesList(
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
private fun FavoritesList(
    items: List<FavoriteApodUiItem>,
    deletingDate: LocalDate?,
    onDeleteClick: (LocalDate) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.favorites_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.favorites_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(items = items, key = { it.date.toString() }) { item ->
            FavoriteCard(
                item = item,
                isDeleting = deletingDate == item.date,
                onDeleteClick = onDeleteClick,
            )
        }
    }
}

@Composable
private fun FavoriteCard(
    item: FavoriteApodUiItem,
    isDeleting: Boolean,
    onDeleteClick: (LocalDate) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Column {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                )
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        Text(
                            text = item.displayDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalContentColor.current.copy(alpha = 0.7f),
                        )
                    }
                    TextButton(
                        onClick = { onDeleteClick(item.date) },
                        enabled = !isDeleting,
                    ) {
                        Text(stringResource(R.string.fav_delete))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.12f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.fav_saved_on, item.savedAtText),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(alpha = 0.65f),
                    )
                    TextButton(onClick = { uriHandler.openUri(item.sourceUrl) }) {
                        Text(
                            text = stringResource(
                                if (item.mediaType == ApodMediaType.IMAGE) {
                                    R.string.fav_open_image
                                } else {
                                    R.string.fav_open_video
                                },
                            ),
                        )
                    }
                }
            }
        }
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
        )
        Text(
            text = stringResource(R.string.favorites_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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
