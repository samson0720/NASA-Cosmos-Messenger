package io.github.samson0720.cosmosmessenger.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChatMessageDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ChatMessageDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.chatMessageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replaceAll_persistsRowsOrderedBySortOrder() = runTest {
        val second = textEntity(id = "2", sortOrder = 2, text = "second")
        val first = textEntity(id = "1", sortOrder = 1, text = "first")

        dao.replaceAll(listOf(second, first))

        assertEquals(listOf(first, second), dao.getAll())
    }

    @Test
    fun replaceAll_replacesPreviousHistory() = runTest {
        dao.replaceAll(listOf(textEntity(id = "old", sortOrder = 0, text = "old")))

        val replacement = textEntity(id = "new", sortOrder = 0, text = "new")
        dao.replaceAll(listOf(replacement))

        assertEquals(listOf(replacement), dao.getAll())
    }

    @Test
    fun replaceAll_emptyList_clearsHistory() = runTest {
        dao.replaceAll(listOf(textEntity(id = "old", sortOrder = 0, text = "old")))

        dao.replaceAll(emptyList())

        assertTrue(dao.getAll().isEmpty())
    }

    private fun textEntity(
        id: String,
        sortOrder: Int,
        text: String,
    ): ChatMessageEntity = ChatMessageEntity(
        id = id,
        sortOrder = sortOrder,
        sender = "Nova",
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
