package io.github.samson0720.cosmosmessenger.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.feature.chat.model.ApodPayload
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import io.github.samson0720.cosmosmessenger.ui.theme.CosmosMessengerTheme

@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        uiState = uiState,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::onSendClick,
        modifier = modifier,
    )
}

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
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
                MessageRow(message = message)
            }
        }
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
private fun MessageRow(message: ChatMessage) {
    val isUser = message.sender == Sender.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Bubble(
            message = message,
            isUser = isUser,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}

@Composable
private fun Bubble(
    message: ChatMessage,
    isUser: Boolean,
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

    Surface(
        modifier = modifier,
        shape = shape,
        color = container,
        contentColor = content,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (message.apod != null) {
                ApodPlaceholder(payload = message.apod)
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// Minimal placeholder. A proper APOD card lands in a later step.
@Composable
private fun ApodPlaceholder(payload: ApodPayload) {
    Column {
        Text(text = payload.title, style = MaterialTheme.typography.titleSmall)
        Text(text = payload.date, style = MaterialTheme.typography.labelSmall)
    }
}

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
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.chat_input_hint)) },
            maxLines = 4,
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
    CosmosMessengerTheme {
        ChatScreen(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(id = "1", sender = Sender.Nova, text = "Hi, I'm Nova."),
                    ChatMessage(id = "2", sender = Sender.User, text = "Show me APOD for 2024-01-01"),
                    ChatMessage(id = "3", sender = Sender.Nova, text = "Here you go."),
                ),
                inputText = "",
            ),
            onInputChange = {},
            onSendClick = {},
        )
    }
}
