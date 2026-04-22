package io.github.samson0720.cosmosmessenger.feature.chat.starcard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.samson0720.cosmosmessenger.R

sealed interface BirthdayCardDialogState {
    data object Loading : BirthdayCardDialogState
    data class Ready(val card: BirthdayStarCard) : BirthdayCardDialogState
    data object Error : BirthdayCardDialogState
}

@Composable
fun BirthdayCardDialog(
    state: BirthdayCardDialogState,
    onDismiss: () -> Unit,
    onShare: (BirthdayStarCard) -> Unit,
) {
    when (state) {
        BirthdayCardDialogState.Loading -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.birthday_card_dialog_title)) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Text(stringResource(R.string.birthday_card_generating))
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )

        is BirthdayCardDialogState.Ready -> BirthdayCardPreviewDialog(
            card = state.card,
            onDismiss = onDismiss,
            onShare = onShare,
        )

        BirthdayCardDialogState.Error -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.birthday_card_dialog_title)) },
            text = { Text(stringResource(R.string.birthday_card_error)) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
    }
}

@Composable
private fun BirthdayCardPreviewDialog(
    card: BirthdayStarCard,
    onDismiss: () -> Unit,
    onShare: (BirthdayStarCard) -> Unit,
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
            val cardWidth = minOf(
                maxWidth,
                (maxHeight - 88.dp) * BirthdayCardAspectRatio,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Image(
                    bitmap = card.previewBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.birthday_card_preview_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(
                        width = cardWidth,
                        height = cardWidth / BirthdayCardAspectRatio,
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
                            Text(stringResource(R.string.birthday_card_close))
                        }
                        TextButton(onClick = { onShare(card) }) {
                            Text(stringResource(R.string.birthday_card_share))
                        }
                    }
                }
            }
        }
    }
}

private const val BirthdayCardAspectRatio = 1024f / 1536f
