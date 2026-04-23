package io.github.samson0720.cosmosmessenger.data.repository

import io.github.samson0720.cosmosmessenger.data.local.ChatMessageDao
import io.github.samson0720.cosmosmessenger.data.local.ChatMessageEntity
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatContent
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatHistoryRepositoryTest {

    @Test
    fun load_mapsDaoRowsAndDropsMalformedRows() = runTest {
        val valid = textEntity(id = "valid", sortOrder = 0, sender = "Nova", text = "hello")
        val invalid = textEntity(id = "invalid", sortOrder = 1, sender = "Legacy", text = "skip")
        val repository = RoomChatHistoryRepository(FakeChatMessageDao(listOf(valid, invalid)))

        val messages = repository.load()

        assertEquals(
            listOf(
                ChatMessage(
                    id = "valid",
                    sender = Sender.Nova,
                    content = ChatContent.Text("hello"),
                ),
            ),
            messages,
        )
    }

    @Test
    fun replace_writesMessagesWithStableSortOrder() = runTest {
        val dao = FakeChatMessageDao()
        val repository = RoomChatHistoryRepository(dao)
        val messages = listOf(
            ChatMessage(id = "first", sender = Sender.User, content = ChatContent.Text("one")),
            ChatMessage(id = "second", sender = Sender.Nova, content = ChatContent.Text("two")),
        )

        repository.replace(messages)

        assertEquals(1, dao.replaceCalls)
        assertEquals(listOf("first", "second"), dao.rows.map { it.id })
        assertEquals(listOf(0, 1), dao.rows.map { it.sortOrder })
    }

    private class FakeChatMessageDao(
        initialRows: List<ChatMessageEntity> = emptyList(),
    ) : ChatMessageDao() {
        var rows = initialRows
        var replaceCalls = 0

        override suspend fun getAll(): List<ChatMessageEntity> = rows

        override suspend fun insertAll(rows: List<ChatMessageEntity>) {
            this.rows = rows
        }

        override suspend fun clearAll() {
            rows = emptyList()
        }

        override suspend fun replaceAll(rows: List<ChatMessageEntity>) {
            replaceCalls += 1
            super.replaceAll(rows)
        }
    }

    private fun textEntity(
        id: String,
        sortOrder: Int,
        sender: String,
        text: String,
    ): ChatMessageEntity = ChatMessageEntity(
        id = id,
        sortOrder = sortOrder,
        sender = sender,
        contentType = "TEXT",
        text = text,
        apodDate = null,
        apodTitle = null,
        apodExplanation = null,
        apodMediaType = null,
        apodUrl = null,
        apodHdUrl = null,
        apodSource = null,
    )
}
