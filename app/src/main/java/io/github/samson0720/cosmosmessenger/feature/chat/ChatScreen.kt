package io.github.samson0720.cosmosmessenger.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.ApodMediaType
import io.github.samson0720.cosmosmessenger.feature.chat.model.ApodCard
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatContent
import io.github.samson0720.cosmosmessenger.feature.chat.model.ChatMessage
import io.github.samson0720.cosmosmessenger.feature.chat.model.Sender
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayCardDialog
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayCardDialogState
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayCardShareHelper
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.BirthdayStarCardRenderer
import io.github.samson0720.cosmosmessenger.ui.CosmosTopBar
import io.github.samson0720.cosmosmessenger.ui.theme.CosmosMessengerTheme
import io.github.samson0720.cosmosmessenger.util.ApodDateParser
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

private val BubbleNovaColor = Color(0xCC1E2547)
private val BubbleNovaBorder = Color(0x406C5CE7)
private val BubbleUserColor = Color(0xCC3A3178)
private val BubbleUserBorder = Color(0x606C5CE7)
private val ApodCardBorder = BorderStroke(0.5.dp, Color(0x3074B9FF))

@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        uiState = uiState,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::onSendClick,
        onDatePicked = viewModel::onDatePicked,
        onApodLongPress = viewModel::onApodLongPress,
        onFeedbackShown = viewModel::consumeFeedback,
        modifier = modifier,
    )
}

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    onApodLongPress: (ChatMessage) -> Unit,
    onFeedbackShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val renderer = remember(context.applicationContext) {
        BirthdayStarCardRenderer(context.applicationContext)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var birthdayCardState by remember {
        mutableStateOf<BirthdayCardDialogState?>(null)
    }
    val onBirthdayCardClick: (Apod) -> Unit = { apod ->
        birthdayCardState = BirthdayCardDialogState.Loading
        scope.launch {
            birthdayCardState = runCatching { renderer.render(apod) }
                .fold(
                    onSuccess = { BirthdayCardDialogState.Ready(it) },
                    onFailure = { BirthdayCardDialogState.Error },
                )
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    // Translate the ViewModel's typed FavoriteFeedback into a localized
    // snackbar string here, at the UI boundary, so the ViewModel itself
    // stays free of UI strings for this path.
    val feedback = uiState.feedback
    val feedbackMessage: String? = when (feedback) {
        FavoriteFeedback.Saved -> stringResource(R.string.fav_saved)
        FavoriteFeedback.AlreadyExists -> stringResource(R.string.fav_already)
        FavoriteFeedback.SaveFailed -> stringResource(R.string.fav_save_failed)
        null -> null
    }
    LaunchedEffect(feedback) {
        if (feedback != null && feedbackMessage != null) {
            // finally {} clears feedback even if the user switches tabs
            // while showSnackbar is still suspended; ChatViewModel is
            // retained across tabs, so without this the stale message
            // would replay on return.
            try {
                snackbarHostState.showSnackbar(feedbackMessage)
            } finally {
                onFeedbackShown()
            }
        }
    }

    // The activity window is resized by the IME, so the root column should
    // fill the resized viewport without adding imePadding() again. Applying
    // both would subtract the keyboard height twice and leave a large gap
    // between the input bar and the keyboard.
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        CosmosTopBar(title = stringResource(R.string.tab_nova))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // Tap on the message area dismisses the keyboard; detectTapGestures
                // ignores drag sequences so the list's own scroll gesture is intact.
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = uiState.messages, key = { it.id }) { message ->
                MessageRow(
                    message = message,
                    onApodLongPress = onApodLongPress,
                    onBirthdayCardClick = onBirthdayCardClick,
                )
            }
        }
        // Inline above the divider: takes zero height when no snackbar is
        // visible, so the resting layout is unchanged.
        SnackbarHost(hostState = snackbarHostState)
        HorizontalDivider()
        ChatInputBar(
            inputText = uiState.inputText,
            sendEnabled = uiState.inputText.isNotBlank() && !uiState.isSending,
            onInputChange = onInputChange,
            // clearFocus() both hides the soft keyboard and releases focus —
            // a plain SoftwareKeyboardController.hide() would leave the field
            // focused and re-open the IME on the next touch.
            onSendClick = {
                onSendClick()
                focusManager.clearFocus()
            },
            onDatePickerClick = {
                focusManager.clearFocus()
                showDatePicker = true
            },
        )
    }

    if (showDatePicker) {
        ApodDatePickerDialog(
            minDate = ApodDateParser.EARLIEST,
            maxDate = LocalDate.now(),
            onDismiss = { showDatePicker = false },
            onDateConfirmed = { date ->
                showDatePicker = false
                onDatePicked(date)
            },
        )
    }

    birthdayCardState?.let { state ->
        BirthdayCardDialog(
            state = state,
            onDismiss = { birthdayCardState = null },
            onShare = { card -> BirthdayCardShareHelper.share(context, card) },
        )
    }
}

@Composable
private fun MessageRow(
    message: ChatMessage,
    onApodLongPress: (ChatMessage) -> Unit,
    onBirthdayCardClick: (Apod) -> Unit,
) {
    val isUser = message.sender == Sender.User
    // User text bubbles stay compact; Nova bubbles hold APOD cards in a narrower frame
    // so the chat still feels like a conversation rather than a full-width gallery.
    val maxWidth = if (isUser) 300.dp else 272.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Bubble(
            message = message,
            isUser = isUser,
            onApodLongPress = onApodLongPress,
            onBirthdayCardClick = onBirthdayCardClick,
            modifier = Modifier.widthIn(max = maxWidth),
        )
    }
}

@Composable
private fun Bubble(
    message: ChatMessage,
    isUser: Boolean,
    onApodLongPress: (ChatMessage) -> Unit,
    onBirthdayCardClick: (Apod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    // Frosted-glass over the starfield: fixed dark tints with ~80% alpha read legibly
    // regardless of system theme, since the backdrop is always the cosmos background.
    val container = if (isUser) BubbleUserColor else BubbleNovaColor
    val border = BorderStroke(
        width = 0.5.dp,
        color = if (isUser) BubbleUserBorder else BubbleNovaBorder,
    )
    val content = Color.White

    val isApod = message.content is ChatContent.ApodImage ||
        message.content is ChatContent.ApodVideo
    val haptic = LocalHapticFeedback.current
    // Long press is bound only on APOD bubbles. detectTapGestures (rather
    // than combinedClickable with a no-op onClick) keeps normal taps inert
    // and avoids a misleading ripple that would imply tap-interactivity.
    val gestureModifier = if (isApod) {
        Modifier.pointerInput(message.id) {
            detectTapGestures(
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onApodLongPress(message)
                },
            )
        }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(gestureModifier),
        shape = shape,
        color = container,
        contentColor = content,
        border = border,
    ) {
        when (val c = message.content) {
            is ChatContent.Text -> Text(
                text = c.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
            is ChatContent.ApodImage -> ApodImageCard(
                content = c,
                onBirthdayCardClick = onBirthdayCardClick,
            )
            is ChatContent.ApodVideo -> ApodVideoCard(card = c.card)
        }
    }
}

@Composable
private fun ApodImageCard(
    content: ChatContent.ApodImage,
    onBirthdayCardClick: (Apod) -> Unit,
) {
    val card = content.card
    // Full-bleed hero image: the surrounding Bubble Surface shape already
    // clips the rounded corners, so no extra clip/shape is needed here.
    Column(modifier = Modifier.border(ApodCardBorder, RoundedCornerShape(16.dp))) {
        if (card.imageUrl != null) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
            )
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            ApodHeader(card = card)
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.explanation,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { onBirthdayCardClick(content.payload) },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
            ) {
                Text(stringResource(R.string.birthday_card_action))
            }
        }
    }
}

@Composable
private fun ApodVideoCard(card: ApodCard) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .border(ApodCardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        ApodHeader(card = card)
        Spacer(Modifier.height(6.dp))
        Text(
            text = card.explanation,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.apod_video_watch),
            style = MaterialTheme.typography.labelLarge.copy(
                textDecoration = TextDecoration.Underline,
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { uriHandler.openUri(card.sourceUrl) },
        )
    }
}

@Composable
private fun ApodHeader(card: ApodCard) {
    Text(
        text = card.title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = card.displayDate,
            style = MaterialTheme.typography.labelSmall,
            // Secondary metadata: soften against the bubble's contentColor.
            color = LocalContentColor.current.copy(alpha = 0.65f),
        )
        if (card.isFromCache) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    text = stringResource(R.string.apod_cache_badge),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(
    inputText: String,
    sendEnabled: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onDatePickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // BasicTextField + OutlinedTextFieldDefaults.DecorationBox lets us
        // shrink past OutlinedTextField's built-in 56dp min height while
        // keeping the Material3 outlined look.
        val interactionSource = remember { MutableInteractionSource() }
        val colors = OutlinedTextFieldDefaults.colors()
        IconButton(onClick = onDatePickerClick) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = stringResource(R.string.chat_pick_date),
            )
        }
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
            singleLine = true,
            maxLines = 1,
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = interactionSource,
            // Enter key in the IME acts as Send — matches the UX of LINE /
            // Messenger / WhatsApp for a single-line chat input.
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(
                onSend = { if (sendEnabled) onSendClick() },
            ),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = inputText,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = RoundedCornerShape(24.dp),
                        )
                    },
                )
            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApodDatePickerDialog(
    minDate: LocalDate,
    maxDate: LocalDate,
    onDismiss: () -> Unit,
    onDateConfirmed: (LocalDate) -> Unit,
) {
    var selectedDate by remember(maxDate) { mutableStateOf(maxDate) }
    var displayedMonth by remember(maxDate) { mutableStateOf(YearMonth.from(maxDate)) }
    var choosingYear by remember { mutableStateOf(false) }
    val minMonth = remember(minDate) { YearMonth.from(minDate) }
    val maxMonth = remember(maxDate) { YearMonth.from(maxDate) }
    val canGoPrevious = displayedMonth > minMonth
    val canGoNext = displayedMonth < maxMonth

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chat_pick_date)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CalendarMonthHeader(
                    displayedMonth = displayedMonth,
                    choosingYear = choosingYear,
                    canGoPrevious = !choosingYear && canGoPrevious,
                    canGoNext = !choosingYear && canGoNext,
                    onPrevious = { if (!choosingYear) displayedMonth = displayedMonth.minusMonths(1) },
                    onNext = { if (!choosingYear) displayedMonth = displayedMonth.plusMonths(1) },
                    onTitleClick = { choosingYear = !choosingYear },
                )
                if (choosingYear) {
                    CalendarYearGrid(
                        minYear = minDate.year,
                        maxYear = maxDate.year,
                        selectedYear = displayedMonth.year,
                        onYearSelected = { year ->
                            selectedDate = selectedDate.withYearClamped(year).coerceIn(minDate, maxDate)
                            displayedMonth = YearMonth.from(selectedDate)
                            choosingYear = false
                        },
                    )
                } else {
                    CalendarWeekdayRow()
                    CalendarMonthGrid(
                        displayedMonth = displayedMonth,
                        selectedDate = selectedDate,
                        minDate = minDate,
                        maxDate = maxDate,
                        onDateSelected = { selectedDate = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDateConfirmed(selectedDate) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun CalendarMonthHeader(
    displayedMonth: YearMonth,
    choosingYear: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onTitleClick,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (choosingYear) {
                    "\u9078\u64c7\u5e74\u4efd"
                } else {
                    "${displayedMonth.year}\u5e74${displayedMonth.monthValue}\u6708"
                },
                style = MaterialTheme.typography.titleMedium,
            )
        }
        TextButton(
            enabled = canGoPrevious,
            onClick = onPrevious,
        ) {
            Text("<")
        }
        TextButton(
            enabled = canGoNext,
            onClick = onNext,
        ) {
            Text(">")
        }
    }
}

@Composable
private fun CalendarYearGrid(
    minYear: Int,
    maxYear: Int,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(318.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items((maxYear downTo minYear).toList()) { year ->
            val selected = year == selectedYear
            TextButton(
                onClick = { onYearSelected(year) },
                modifier = Modifier
                    .height(48.dp)
                    .then(
                        if (selected) {
                            Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Text(
                    text = "${year}\u5e74",
                    color = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                )
            }
        }
    }
}

private fun LocalDate.coerceIn(min: LocalDate, max: LocalDate): LocalDate = when {
    isBefore(min) -> min
    isAfter(max) -> max
    else -> this
}

private fun LocalDate.withYearClamped(year: Int): LocalDate {
    val targetMonth = YearMonth.of(year, monthValue)
    return LocalDate.of(year, monthValue, dayOfMonth.coerceAtMost(targetMonth.lengthOfMonth()))
}

@Composable
private fun CalendarWeekdayRow() {
    val weekdays = listOf("\u65e5", "\u4e00", "\u4e8c", "\u4e09", "\u56db", "\u4e94", "\u516d")
    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEach { weekday ->
            Text(
                text = weekday,
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val firstDayOffset = displayedMonth.atDay(1).dayOfWeek.value % 7
    val daysInMonth = displayedMonth.lengthOfMonth()
    val cellCount = ((firstDayOffset + daysInMonth + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        (0 until cellCount).chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    val day = cell - firstDayOffset + 1
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (day in 1..daysInMonth) {
                            val date = displayedMonth.atDay(day)
                            CalendarDay(
                                day = day,
                                selected = date == selectedDate,
                                enabled = date in minDate..maxDate,
                                onClick = { onDateSelected(date) },
                            )
                        } else {
                            Spacer(Modifier.size(42.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        enabled -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.toString(),
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    val previewApod = Apod(
        date = LocalDate.of(2024, 1, 1),
        title = "Andromeda Galaxy",
        explanation = "The Andromeda Galaxy is the closest spiral galaxy to our own Milky Way.",
        mediaType = ApodMediaType.IMAGE,
        url = "https://apod.nasa.gov/apod/image/2401/example.jpg",
        hdUrl = null,
    )
    CosmosMessengerTheme {
        ChatScreen(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        sender = Sender.Nova,
                        content = ChatContent.Text("你好，我是 Nova。"),
                    ),
                    ChatMessage(
                        id = "2",
                        sender = Sender.User,
                        content = ChatContent.Text("2024/01/01"),
                    ),
                    ChatMessage(
                        id = "3",
                        sender = Sender.Nova,
                        content = ChatContent.ApodImage(
                            card = ApodCard(
                                title = "Andromeda Galaxy",
                                displayDate = "2024/01/01",
                                explanation = "The Andromeda Galaxy is the closest spiral galaxy to our own Milky Way, located roughly 2.5 million light-years away.",
                                imageUrl = null,
                                sourceUrl = "https://apod.nasa.gov/",
                            ),
                            payload = previewApod,
                        ),
                    ),
                ),
                inputText = "",
            ),
            onInputChange = {},
            onSendClick = {},
            onDatePicked = {},
            onApodLongPress = {},
            onFeedbackShown = {},
        )
    }
}
