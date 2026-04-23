package io.github.samson0720.cosmosmessenger.feature.chat

import android.app.Application
import io.github.samson0720.cosmosmessenger.MainDispatcherRule
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.data.ApodRepository
import io.github.samson0720.cosmosmessenger.data.NovaGuideRepository
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.domain.model.ApodSource
import io.github.samson0720.cosmosmessenger.domain.model.FavoriteApod
import io.github.samson0720.cosmosmessenger.domain.model.NovaGuide
import io.github.samson0720.cosmosmessenger.domain.repository.FavoritesRepository
import io.github.samson0720.cosmosmessenger.domain.repository.SaveResult
import io.github.samson0720.cosmosmessenger.feature.chat.model.ApodCard
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatContent
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onInputChange_updatesInputTextOnly() {
        val viewModel = newViewModel()
        val initialMessages = viewModel.uiState.value.messages

        viewModel.onInputChange("hello nova")

        val state = viewModel.uiState.value
        assertEquals("hello nova", state.inputText)
        assertEquals(initialMessages, state.messages)
        assertFalse(state.isSending)
        assertNull(state.feedback)
    }

    @Test
    fun onSendClick_withoutDate_callsRepositoryWithNullAndAppendsApodReply() = runTest {
        val apod = sampleApod()
        val repository = FakeApodRepository(Result.success(apod))
        val viewModel = newViewModel(repository = repository)
        val initialMessageCount = viewModel.uiState.value.messages.size

        viewModel.onInputChange("show me today's APOD")
        viewModel.onSendClick()

        val state = viewModel.uiState.value
        assertEquals(listOf(null), repository.calls)
        assertEquals(initialMessageCount + 3, state.messages.size)
        assertEquals("", state.inputText)
        assertFalse(state.isSending)

        val userMessage = state.messages[state.messages.lastIndex - 2]
        assertEquals(Sender.User, userMessage.sender)
        assertEquals(ChatContent.Text("show me today's APOD"), userMessage.content)

        val intro = state.messages[state.messages.lastIndex - 1]
        assertEquals(Sender.Nova, intro.sender)
        assertEquals(ChatContent.Text(stringFor(R.string.nova_today_intro)), intro.content)

        val reply = state.messages.last()
        assertEquals(Sender.Nova, reply.sender)
        val content = reply.content
        assertTrue(content is ChatContent.ApodImage)
        val apodContent = content as ChatContent.ApodImage
        assertApodCardMatches(apodContent.card, apod)
        assertFalse(apodContent.card.isFromCache)
        assertSame(apod, apodContent.payload)
    }

    @Test
    fun onSendClick_cachedApod_marksReplyCardAsFromCache() = runTest {
        val apod = sampleApod(source = ApodSource.CACHE)
        val repository = FakeApodRepository(Result.success(apod))
        val viewModel = newViewModel(repository = repository)

        viewModel.onInputChange("show me 2024/01/02")
        viewModel.onSendClick()

        val content = viewModel.uiState.value.messages.last().content
        assertTrue(content is ChatContent.ApodImage)
        assertTrue((content as ChatContent.ApodImage).card.isFromCache)
    }

    @Test
    fun onSendClick_withValidDate_callsRepositoryWithParsedDate() = runTest {
        val apod = sampleApod(date = LocalDate.of(2024, 1, 2))
        val repository = FakeApodRepository(Result.success(apod))
        val viewModel = newViewModel(repository = repository)

        viewModel.onInputChange("please show 2024/01/02")
        viewModel.onSendClick()

        val state = viewModel.uiState.value
        assertEquals(listOf(LocalDate.of(2024, 1, 2)), repository.calls)
        assertEquals(4, state.messages.size)
        assertFalse(state.isSending)
        assertEquals(ChatContent.Text(stringFor(R.string.nova_date_intro)), state.messages[2].content)
        assertTrue(state.messages.last().content is ChatContent.ApodImage)
    }

    @Test
    fun onDatePicked_formatsDateAndReusesSendFlow() = runTest {
        val apod = sampleApod(date = LocalDate.of(2024, 1, 2))
        val repository = FakeApodRepository(Result.success(apod))
        val viewModel = newViewModel(repository = repository)

        viewModel.onDatePicked(LocalDate.of(2024, 1, 2))

        val state = viewModel.uiState.value
        assertEquals(listOf(LocalDate.of(2024, 1, 2)), repository.calls)
        assertEquals(4, state.messages.size)
        assertEquals("", state.inputText)
        assertFalse(state.isSending)
        assertEquals(ChatContent.Text("2024-01-02"), state.messages[1].content)
        assertEquals(ChatContent.Text(stringFor(R.string.nova_date_intro)), state.messages[2].content)
        assertTrue(state.messages.last().content is ChatContent.ApodImage)
    }

    @Test
    fun onSendClick_malformedDate_returnsErrorWithoutRepositoryCall() = runTest {
        val repository = FakeApodRepository()
        val viewModel = newViewModel(repository = repository)
        val initialMessageCount = viewModel.uiState.value.messages.size

        viewModel.onInputChange("show me 2024-02-30")
        viewModel.onSendClick()

        val state = viewModel.uiState.value
        assertEquals(emptyList<LocalDate?>(), repository.calls)
        assertEquals(initialMessageCount + 2, state.messages.size)
        assertFalse(state.isSending)

        val reply = state.messages.last()
        assertEquals(Sender.Nova, reply.sender)
        assertTrue(reply.content is ChatContent.Text)
    }

    @Test
    fun onApodLongPress_imageSaved_setsSavedFeedback() = runTest {
        val apod = sampleApod()
        val favoritesRepository = FakeFavoritesRepository(saveResult = SaveResult.Saved)
        val viewModel = newViewModel(favoritesRepository = favoritesRepository)
        val message = ChatMessage(
            id = "apod-message",
            sender = Sender.Nova,
            content = ChatContent.ApodImage(
                card = sampleApodCard(apod),
                payload = apod,
            ),
        )

        viewModel.onApodLongPress(message)

        assertEquals(listOf(apod), favoritesRepository.saveCalls)
        assertSame(FavoriteFeedback.Saved, viewModel.uiState.value.feedback)
    }

    @Test
    fun consumeFeedback_clearsFeedback() = runTest {
        val apod = sampleApod()
        val favoritesRepository = FakeFavoritesRepository(saveResult = SaveResult.Saved)
        val viewModel = newViewModel(favoritesRepository = favoritesRepository)
        val message = ChatMessage(
            id = "apod-message",
            sender = Sender.Nova,
            content = ChatContent.ApodImage(
                card = sampleApodCard(apod),
                payload = apod,
            ),
        )
        viewModel.onApodLongPress(message)
        assertSame(FavoriteFeedback.Saved, viewModel.uiState.value.feedback)

        viewModel.consumeFeedback()

        assertNull(viewModel.uiState.value.feedback)
    }

    @Test
    fun generateNovaGuide_delegatesToGuideRepository() = runTest {
        val apod = sampleApod()
        val guide = sampleGuide()
        val guideRepository = FakeNovaGuideRepository(Result.success(guide))
        val viewModel = newViewModel(guideRepository = guideRepository)

        val result = viewModel.generateNovaGuide(apod)

        assertEquals(listOf(apod), guideRepository.calls)
        assertEquals(Result.success(guide), result)
    }

    private fun newViewModel(
        repository: FakeApodRepository = FakeApodRepository(),
        guideRepository: FakeNovaGuideRepository = FakeNovaGuideRepository(),
        favoritesRepository: FakeFavoritesRepository = FakeFavoritesRepository(),
    ): ChatViewModel = ChatViewModel(
        application = Application(),
        repository = repository,
        novaGuideRepository = guideRepository,
        favoritesRepository = favoritesRepository,
        stringProvider = ChatStringProvider { resId -> stringFor(resId) },
    )

    private fun sampleApod(
        date: LocalDate = LocalDate.of(2024, 1, 2),
        mediaType: ApodMediaType = ApodMediaType.IMAGE,
        hdUrl: String? = "https://example.com/hd.jpg",
        source: ApodSource = ApodSource.NETWORK,
    ): Apod = Apod(
        date = date,
        title = "Sample title",
        explanation = "Sample explanation",
        mediaType = mediaType,
        url = "https://example.com/image.jpg",
        hdUrl = hdUrl,
        source = source,
    )

    private fun sampleApodCard(apod: Apod): ApodCard = ApodCard(
        title = apod.title,
        displayDate = "2024/01/02",
        explanation = apod.explanation,
        imageUrl = apod.url,
        sourceUrl = apod.hdUrl ?: apod.url,
    )

    private fun assertApodCardMatches(card: ApodCard, apod: Apod) {
        assertEquals(apod.title, card.title)
        assertEquals("2024/01/02", card.displayDate)
        assertEquals(apod.explanation, card.explanation)
        assertEquals(apod.url, card.imageUrl)
        assertEquals(apod.hdUrl, card.sourceUrl)
        assertEquals(apod.source == ApodSource.CACHE, card.isFromCache)
    }

    private fun sampleGuide(): NovaGuide = NovaGuide(
        shortSummary = "一張宇宙照片的白話摘要。",
        plainChinese = "這裡用簡短中文說明 NASA APOD 原文。",
        keyPoints = listOf("重點一", "重點二", "重點三"),
        terms = emptyList(),
        source = "NASA APOD explanation",
    )

    private class FakeApodRepository(
        private val result: Result<Apod> = Result.failure(
            IllegalStateException("No APOD result configured"),
        ),
    ) : ApodRepository {
        val calls = mutableListOf<LocalDate?>()

        override suspend fun getApod(date: LocalDate?): Result<Apod> {
            calls += date
            return result
        }
    }

    private class FakeNovaGuideRepository(
        private val result: Result<NovaGuide> = Result.failure(
            IllegalStateException("No Nova guide result configured"),
        ),
    ) : NovaGuideRepository {
        val calls = mutableListOf<Apod>()

        override suspend fun explain(apod: Apod): Result<NovaGuide> {
            calls += apod
            return result
        }
    }

    private class FakeFavoritesRepository(
        private val saveResult: SaveResult = SaveResult.Saved,
    ) : FavoritesRepository {
        val saveCalls = mutableListOf<Apod>()

        override fun observeAll(): Flow<List<FavoriteApod>> = flowOf(emptyList())

        override suspend fun save(apod: Apod): SaveResult {
            saveCalls += apod
            return saveResult
        }

        override suspend fun delete(date: LocalDate) = Unit
    }

    private fun stringFor(resId: Int): String = "string-$resId"
}
