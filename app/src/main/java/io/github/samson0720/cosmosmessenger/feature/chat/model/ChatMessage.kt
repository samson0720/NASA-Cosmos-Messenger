package io.github.samson0720.cosmosmessenger.feature.chat.model

enum class Sender { User, Nova }

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val content: ChatContent,
)

sealed interface ChatContent {
    data class Text(val text: String) : ChatContent
    data class ApodImage(val card: ApodCard) : ChatContent
    data class ApodVideo(val card: ApodCard) : ChatContent
}

data class ApodCard(
    val title: String,
    val displayDate: String,
    val explanation: String,
    val imageUrl: String?,
    val sourceUrl: String,
)
