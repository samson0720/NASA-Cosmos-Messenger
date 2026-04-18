package io.github.samson0720.cosmosmessenger.feature.chat.model

enum class Sender { User, Nova }

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val apod: ApodPayload? = null,
    val canFavorite: Boolean = false,
)

data class ApodPayload(
    val title: String,
    val date: String,
    val mediaUrl: String,
    val mediaType: String,
    val explanation: String,
)
