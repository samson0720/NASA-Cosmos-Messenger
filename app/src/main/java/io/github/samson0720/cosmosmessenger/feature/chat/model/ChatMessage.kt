package io.github.samson0720.cosmosmessenger.feature.chat.model

import io.github.samson0720.cosmosmessenger.domain.model.Apod

enum class Sender { User, Nova }

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val content: ChatContent,
)

sealed interface ChatContent {
    data class Text(val text: String) : ChatContent

    /**
     * APOD image bubble. Two payloads on purpose:
     *  - [card] is the display model (already formatted strings, single image
     *    URL chosen for in-bubble rendering). Composables read this.
     *  - [payload] is the original domain object. ViewModel reads this for
     *    domain-side actions like saving a favorite, so we never have to
     *    re-parse [ApodCard.displayDate] back into a [java.time.LocalDate]
     *    or reverse-engineer url/hdUrl from [ApodCard.sourceUrl].
     */
    data class ApodImage(val card: ApodCard, val payload: Apod) : ChatContent

    /** APOD video bubble. Same display/payload split as [ApodImage]. */
    data class ApodVideo(val card: ApodCard, val payload: Apod) : ChatContent
}

data class ApodCard(
    val title: String,
    val displayDate: String,
    val explanation: String,
    val imageUrl: String?,
    val sourceUrl: String,
    val isFromCache: Boolean = false,
)
