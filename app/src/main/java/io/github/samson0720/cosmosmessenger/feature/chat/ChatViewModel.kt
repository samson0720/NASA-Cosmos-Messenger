package io.github.samson0720.cosmosmessenger.feature.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.samson0720.cosmosmessenger.BuildConfig
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.data.ApodException
import io.github.samson0720.cosmosmessenger.data.ApodRepository
import io.github.samson0720.cosmosmessenger.data.ApodRepositoryImpl
import io.github.samson0720.cosmosmessenger.data.local.DatabaseModule
import io.github.samson0720.cosmosmessenger.data.remote.NetworkModule
import io.github.samson0720.cosmosmessenger.data.repository.FavoritesRepositoryImpl
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.repository.FavoritesRepository
import io.github.samson0720.cosmosmessenger.domain.repository.SaveResult
import io.github.samson0720.cosmosmessenger.feature.chat.model.ApodCard
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatContent
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import io.github.samson0720.cosmosmessenger.util.ApodDateParser
import io.github.samson0720.cosmosmessenger.util.ApodDateParser.DateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Result of a favorite-save attempt that the chat screen wants to surface to
 * the user. Kept as a sealed interface (not a raw String) so the ViewModel
 * stays free of UI strings; the Composable layer maps each case to a
 * [stringResource][androidx.compose.ui.res.stringResource].
 */
sealed interface FavoriteFeedback {
    data object Saved : FavoriteFeedback
    data object AlreadyExists : FavoriteFeedback
    data object SaveFailed : FavoriteFeedback
}

fun interface ChatStringProvider {
    fun getString(resId: Int): String
}

private class AndroidChatStringProvider(
    private val application: Application,
) : ChatStringProvider {
    override fun getString(resId: Int): String = application.getString(resId)
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val feedback: FavoriteFeedback? = null,
)

class ChatViewModel(
    application: Application,
    private val repository: ApodRepository,
    private val favoritesRepository: FavoritesRepository,
    private val stringProvider: ChatStringProvider = AndroidChatStringProvider(application),
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    id = newId(),
                    sender = Sender.Nova,
                    content = ChatContent.Text(stringProvider.getString(R.string.nova_welcome)),
                ),
            ),
        ),
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onDatePicked(date: LocalDate) {
        if (_uiState.value.isSending) return

        _uiState.update {
            it.copy(inputText = date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        onSendClick()
    }

    fun onSendClick() {
        val snapshot = _uiState.value
        val text = snapshot.inputText.trim()
        if (text.isEmpty() || snapshot.isSending) return

        val userMessage = ChatMessage(
            id = newId(),
            sender = Sender.User,
            content = ChatContent.Text(text),
        )
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isSending = true,
            )
        }

        viewModelScope.launch {
            val reply = buildReplyFor(text)
            _uiState.update {
                it.copy(
                    messages = it.messages + reply,
                    isSending = false,
                )
            }
        }
    }

    /**
     * Long-press handler for APOD bubbles. Pulls the domain [Apod] payload
     * straight off the message — never reconstructs it from the display card —
     * and reports the outcome through [ChatUiState.feedback].
     */
    fun onApodLongPress(message: ChatMessage) {
        val apod = when (val c = message.content) {
            is ChatContent.ApodImage -> c.payload
            is ChatContent.ApodVideo -> c.payload
            is ChatContent.Text -> return
        }
        viewModelScope.launch {
            // runCatching keeps Room/IO exceptions from escaping the coroutine
            // and lets the UI surface a recoverable snackbar instead of crashing.
            val feedback = runCatching { favoritesRepository.save(apod) }
                .fold(
                    onSuccess = { result ->
                        when (result) {
                            SaveResult.Saved -> FavoriteFeedback.Saved
                            SaveResult.AlreadyExists -> FavoriteFeedback.AlreadyExists
                        }
                    },
                    onFailure = { FavoriteFeedback.SaveFailed },
                )
            _uiState.update { it.copy(feedback = feedback) }
        }
    }

    /** Called by the UI once it has shown the snackbar for [ChatUiState.feedback]. */
    fun consumeFeedback() {
        _uiState.update { it.copy(feedback = null) }
    }

    private suspend fun buildReplyFor(input: String): ChatMessage {
        val dateResult = ApodDateParser.extract(input)
        val target = when (dateResult) {
            is DateResult.Valid -> dateResult.date
            is DateResult.None -> null
            is DateResult.Malformed -> return novaText(R.string.nova_error_invalid_date)
            is DateResult.OutOfRange -> return novaText(
                if (dateResult.tooOld) R.string.nova_error_too_old
                else R.string.nova_error_future_date
            )
        }

        return repository.getApod(target).fold(
            onSuccess = { apod -> renderApod(apod) },
            onFailure = { error ->
                val resId = when (error) {
                    is ApodException.RateLimited -> R.string.nova_error_rate_limited
                    is ApodException.NotFound -> R.string.nova_error_not_found
                    else -> R.string.nova_error_network
                }
                novaText(resId)
            },
        )
    }

    private fun renderApod(apod: Apod): ChatMessage {
        val displayDate = ApodDateParser.formatForDisplay(apod.date)
        return when (apod.mediaType) {
            ApodMediaType.IMAGE -> ChatMessage(
                id = newId(),
                sender = Sender.Nova,
                // In-chat image uses the standard-resolution `url` to keep
                // loading snappy; hdUrl is reserved for the open-link action.
                content = ChatContent.ApodImage(
                    card = ApodCard(
                        title = apod.title,
                        displayDate = displayDate,
                        explanation = apod.explanation,
                        imageUrl = apod.url,
                        sourceUrl = apod.hdUrl ?: apod.url,
                    ),
                    payload = apod,
                ),
            )
            ApodMediaType.VIDEO, ApodMediaType.OTHER -> ChatMessage(
                id = newId(),
                sender = Sender.Nova,
                content = ChatContent.ApodVideo(
                    card = ApodCard(
                        title = apod.title,
                        displayDate = displayDate,
                        explanation = apod.explanation,
                        imageUrl = null,
                        sourceUrl = apod.url,
                    ),
                    payload = apod,
                ),
            )
        }
    }

    private fun novaText(resId: Int): ChatMessage = ChatMessage(
        id = newId(),
        sender = Sender.Nova,
        content = ChatContent.Text(stringProvider.getString(resId)),
    )

    private fun newId(): String = UUID.randomUUID().toString()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as Application
                val apodRepository = ApodRepositoryImpl(
                    service = NetworkModule.apodService,
                    apiKey = BuildConfig.NASA_API_KEY,
                )
                val favoritesRepository = FavoritesRepositoryImpl(
                    dao = DatabaseModule.get(app).favoriteApodDao(),
                )
                ChatViewModel(app, apodRepository, favoritesRepository)
            }
        }
    }
}
