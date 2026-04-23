package io.github.samson0720.cosmosmessenger.feature.favorites.collage

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.samson0720.cosmosmessenger.R

sealed interface MemoryCollageDialogState {
    data object TemplateSelection : MemoryCollageDialogState
    data object Loading : MemoryCollageDialogState
    data class Ready(val collage: FavoriteCollage) : MemoryCollageDialogState
    data object Error : MemoryCollageDialogState
}

@Composable
fun MemoryCollageDialog(
    state: MemoryCollageDialogState,
    onDismiss: () -> Unit,
    onTemplateSelected: (MemoryCollageTemplate) -> Unit,
    onShare: (FavoriteCollage) -> Unit,
) {
    when (state) {
        MemoryCollageDialogState.TemplateSelection -> TemplateSelectionDialog(
            onDismiss = onDismiss,
            onTemplateSelected = onTemplateSelected,
        )

        MemoryCollageDialogState.Loading -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.collage_dialog_title)) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Text(stringResource(R.string.collage_generating))
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )

        is MemoryCollageDialogState.Ready -> MemoryCollagePreviewDialog(
            collage = state.collage,
            onDismiss = onDismiss,
            onShare = onShare,
        )

        MemoryCollageDialogState.Error -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.collage_dialog_title)) },
            text = { Text(stringResource(R.string.collage_error)) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
    }
}

@Composable
private fun TemplateSelectionDialog(
    onDismiss: () -> Unit,
    onTemplateSelected: (MemoryCollageTemplate) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.collage_template_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TemplateChoiceCard(
                    title = stringResource(R.string.collage_template_polaroid_orbit),
                    body = stringResource(R.string.collage_template_polaroid_orbit_body),
                    accent = Color(0xFFECDCAA),
                    onClick = { onTemplateSelected(MemoryCollageTemplate.PolaroidOrbit) },
                )
                TemplateChoiceCard(
                    title = stringResource(R.string.collage_template_mission_board),
                    body = stringResource(R.string.collage_template_mission_board_body),
                    accent = Color(0xFF2D5F9A),
                    onClick = { onTemplateSelected(MemoryCollageTemplate.MissionBoard) },
                )
                TemplateChoiceCard(
                    title = stringResource(R.string.collage_template_celestial_journal),
                    body = stringResource(R.string.collage_template_celestial_journal_body),
                    accent = Color(0xFFD8B36A),
                    onClick = { onTemplateSelected(MemoryCollageTemplate.CelestialJournal) },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun TemplateChoiceCard(
    title: String,
    body: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 78.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(width = 42.dp, height = 68.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
                ) {}
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                )
            }
        }
    }
}

@Composable
private fun MemoryCollagePreviewDialog(
    collage: FavoriteCollage,
    onDismiss: () -> Unit,
    onShare: (FavoriteCollage) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val aspectRatio = collage.previewBitmap.width.toFloat() / collage.previewBitmap.height.toFloat()
            val cardWidth = minOf(
                maxWidth,
                (maxHeight - 88.dp) * aspectRatio,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Image(
                    bitmap = collage.previewBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.collage_preview_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(
                        width = cardWidth,
                        height = cardWidth / aspectRatio,
                    ),
                )
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.collage_close))
                        }
                        TextButton(onClick = { onShare(collage) }) {
                            Text(stringResource(R.string.collage_share))
                        }
                    }
                }
            }
        }
    }
}
