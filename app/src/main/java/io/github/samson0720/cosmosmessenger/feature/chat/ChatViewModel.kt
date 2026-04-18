package io.github.samson0720.cosmosmessenger.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    id = newId(),
                    sender = Sender.Nova,
                    text = "Hi, I'm Nova. Ask me for any NASA APOD by date.",
                ),
            ),
        ),
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendClick() {
        val snapshot = _uiState.value
        val text = snapshot.inputText.trim()
        if (text.isEmpty() || snapshot.isSending) return

        val userMessage = ChatMessage(
            id = newId(),
            sender = Sender.User,
            text = text,
        )
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isSending = true,
            )
        }

        viewModelScope.launch {
            delay(FAKE_REPLY_DELAY_MS)
            val reply = ChatMessage(
                id = newId(),
                sender = Sender.Nova,
                text = "Got it. Real NASA APOD integration is coming next.",
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + reply,
                    isSending = false,
                )
            }
        }
    }

    private fun newId(): String = UUID.randomUUID().toString()

    private companion object {
        const val FAKE_REPLY_DELAY_MS = 600L
    }
}
