package io.github.samson0720.cosmosmessenger.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.feature.chat.model.ApodCard
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatContent
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import io.github.samson0720.cosmosmessenger.ui.theme.CosmosMessengerTheme
import java.time.LocalDate

@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        uiState = uiState,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::onSendClick,
        onApodLongPress = viewModel::onApodLongPress,
        onFeedbackShown = viewModel::consumeFeedback,
        modifier = modifier,
    )
}

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onApodLongPress: (ChatMessage) -> Unit,
    onFeedbackShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    // Translate the ViewModel's typed FavoriteFeedback into a localized
    // snackbar string here, at the UI boundary, so the ViewModel itself
    // stays free of UI strings for this path.
    val feedback = uiState.feedback
    val feedbackMessage: String? = when (feedback) {
        FavoriteFeedback.Saved -> stringResource(R.string.fav_saved)
        FavoriteFeedback.AlreadyExists -> stringResource(R.string.fav_already)
        FavoriteFeedback.SaveFailed -> stringResource(R.string.fav_save_failed)
        null -> null
    }
    LaunchedEffect(feedback) {
        if (feedback != null && feedbackMessage != null) {
            // finally {} clears feedback even if the user switches tabs
            // while showSnackbar is still suspended; ChatViewModel is
            // retained across tabs, so without this the stale message
            // would replay on return.
            try {
                snackbarHostState.showSnackbar(feedbackMessage)
            } finally {
                onFeedbackShown()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = uiState.messages, key = { it.id }) { message ->
                MessageRow(message = message, onApodLongPress = onApodLongPress)
            }
        }
        // Inline above the divider: takes zero height when no snackbar is
        // visible, so the resting layout is unchanged.
        SnackbarHost(hostState = snackbarHostState)
        HorizontalDivider()
        ChatInputBar(
            inputText = uiState.inputText,
            sendEnabled = uiState.inputText.isNotBlank() && !uiState.isSending,
            onInputChange = onInputChange,
            onSendClick = onSendClick,
        )
    }
}

@Composable
private fun MessageRow(
    message: ChatMessage,
    onApodLongPress: (ChatMessage) -> Unit,
) {
    val isUser = message.sender == Sender.User
    // Nova cards need a bit more room for the image; user bubbles stay compact.
    val maxWidth = if (isUser) 280.dp else 300.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Bubble(
            message = message,
            isUser = isUser,
            onApodLongPress = onApodLongPress,
            modifier = Modifier.widthIn(max = maxWidth),
        )
    }
}

@Composable
private fun Bubble(
    message: ChatMessage,
    isUser: Boolean,
    onApodLongPress: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    val container =
        if (isUser) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    val content =
        if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant

    val isApod = message.content is ChatContent.ApodImage ||
        message.content is ChatContent.ApodVideo
    val haptic = LocalHapticFeedback.current
    // Long press is bound only on APOD bubbles. detectTapGestures (rather
    // than combinedClickable with a no-op onClick) keeps normal taps inert
    // and avoids a misleading ripple that would imply tap-interactivity.
    val gestureModifier = if (isApod) {
        Modifier.pointerInput(message.id) {
            detectTapGestures(
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onApodLongPress(message)
                },
            )
        }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(gestureModifier),
        shape = shape,
        color = container,
        contentColor = content,
        tonalElevation = 1.dp,
    ) {
        when (val c = message.content) {
            is ChatContent.Text -> Text(
                text = c.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
            is ChatContent.ApodImage -> ApodImageCard(card = c.card)
            is ChatContent.ApodVideo -> ApodVideoCard(card = c.card)
        }
    }
}

@Composable
private fun ApodImageCard(card: ApodCard) {
    // Full-bleed hero image: the surrounding Bubble Surface shape already
    // clips the rounded corners, so no extra clip/shape is needed here.
    Column {
        if (card.imageUrl != null) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
            )
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            ApodHeader(card = card)
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.explanation,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ApodVideoCard(card: ApodCard) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.padding(12.dp)) {
        ApodHeader(card = card)
        Spacer(Modifier.height(6.dp))
        Text(
            text = card.explanation,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.apod_video_watch),
            style = MaterialTheme.typography.labelLarge.copy(
                textDecoration = TextDecoration.Underline,
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { uriHandler.openUri(card.sourceUrl) },
        )
    }
}

@Composable
private fun ApodHeader(card: ApodCard) {
    Text(
        text = card.title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    )
    Text(
        text = card.displayDate,
        style = MaterialTheme.typography.labelSmall,
        // Secondary metadata: soften against the bubble's contentColor.
        color = LocalContentColor.current.copy(alpha = 0.65f),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    inputText: String,
    sendEnabled: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // BasicTextField + OutlinedTextFieldDefaults.DecorationBox lets us
        // shrink past OutlinedTextField's built-in 56dp min height while
        // keeping the Material3 outlined look.
        val interactionSource = remember { MutableInteractionSource() }
        val colors = OutlinedTextFieldDefaults.colors()
        BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
            singleLine = true,
            maxLines = 1,
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = inputText,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = colors,
                        )
                    },
                )
            },
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = onSendClick,
            enabled = sendEnabled,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.chat_send),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    val previewApod = Apod(
        date = LocalDate.of(2024, 1, 1),
        title = "Andromeda Galaxy",
        explanation = "The Andromeda Galaxy is the closest spiral galaxy to our own Milky Way.",
        mediaType = ApodMediaType.IMAGE,
        url = "https://apod.nasa.gov/apod/image/2401/example.jpg",
        hdUrl = null,
    )
    CosmosMessengerTheme {
        ChatScreen(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        sender = Sender.Nova,
                        content = ChatContent.Text("你好，我是 Nova。"),
                    ),
                    ChatMessage(
                        id = "2",
                        sender = Sender.User,
                        content = ChatContent.Text("2024/01/01"),
                    ),
                    ChatMessage(
                        id = "3",
                        sender = Sender.Nova,
                        content = ChatContent.ApodImage(
                            card = ApodCard(
                                title = "Andromeda Galaxy",
                                displayDate = "2024/01/01",
                                explanation = "The Andromeda Galaxy is the closest spiral galaxy to our own Milky Way, located roughly 2.5 million light-years away.",
                                imageUrl = null,
                                sourceUrl = "https://apod.nasa.gov/",
                            ),
                            payload = previewApod,
                        ),
                    ),
                ),
                inputText = "",
            ),
            onInputChange = {},
            onSendClick = {},
            onApodLongPress = {},
            onFeedbackShown = {},
        )
    }
}
