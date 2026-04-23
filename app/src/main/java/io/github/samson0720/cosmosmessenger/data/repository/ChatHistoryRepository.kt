package io.github.samson0720.cosmosmessenger.data.repository

import io.github.samson0720.cosmosmessenger.data.local.ChatMessageDao
import io.github.samson0720.cosmosmessenger.data.mapper.toDomain
import io.github.samson0720.cosmosmessenger.data.mapper.toEntity
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage

interface ChatHistoryRepository {
    suspend fun load(): List<ChatMessage>
    suspend fun replace(messages: List<ChatMessage>)
}

class RoomChatHistoryRepository(
    private val dao: ChatMessageDao,
) : ChatHistoryRepository {

    override suspend fun load(): List<ChatMessage> =
        dao.getAll().mapNotNull { it.toDomain() }

    override suspend fun replace(messages: List<ChatMessage>) {
        dao.replaceAll(messages.mapIndexed { index, message -> message.toEntity(index) })
    }
}
