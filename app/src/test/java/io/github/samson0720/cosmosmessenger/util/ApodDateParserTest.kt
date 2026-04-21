package io.github.samson0720.cosmosmessenger.util

import io.github.samson0720.cosmosmessenger.util.ApodDateParser.DateResult
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ApodDateParserTest {

    private val today = LocalDate.of(2026, 4, 21)

    @Test
    fun extract_slashDate_returnsValidDate() {
        val result = ApodDateParser.extract("1995/06/20", today)

        assertValidDate(result, LocalDate.of(1995, 6, 20))
    }

    @Test
    fun extract_dashDate_returnsValidDate() {
        val result = ApodDateParser.extract("1995-06-20", today)

        assertValidDate(result, LocalDate.of(1995, 6, 20))
    }

    @Test
    fun extract_dotDate_returnsValidDate() {
        val result = ApodDateParser.extract("1995.06.20", today)

        assertValidDate(result, LocalDate.of(1995, 6, 20))
    }

    @Test
    fun extract_dateInsideSentence_returnsValidDate() {
        val result = ApodDateParser.extract("show me the sky on 2024/1/2 please", today)

        assertValidDate(result, LocalDate.of(2024, 1, 2))
    }

    @Test
    fun extract_plainMessage_returnsNone() {
        val result = ApodDateParser.extract("hello nova", today)

        assertSame(DateResult.None, result)
    }

    @Test
    fun extract_invalidCalendarDate_returnsMalformed() {
        val result = ApodDateParser.extract("2024-02-30", today)

        assertSame(DateResult.Malformed, result)
    }

    @Test
    fun extract_mixedSeparators_returnsMalformed() {
        val result = ApodDateParser.extract("2024-01/01", today)

        assertSame(DateResult.Malformed, result)
    }

    @Test
    fun extract_chineseDateLikeText_returnsMalformed() {
        val result = ApodDateParser.extract("2024年01月01日", today)

        assertSame(DateResult.Malformed, result)
    }

    @Test
    fun extract_beforeApodStart_returnsOutOfRangeTooOld() {
        val result = ApodDateParser.extract("1995/06/15", today)

        assertTrue(result is DateResult.OutOfRange)
        val outOfRange = result as DateResult.OutOfRange
        assertEquals(LocalDate.of(1995, 6, 15), outOfRange.date)
        assertTrue(outOfRange.tooOld)
    }

    @Test
    fun extract_apodStartDate_returnsValid() {
        val result = ApodDateParser.extract("1995/06/16", today)

        assertValidDate(result, LocalDate.of(1995, 6, 16))
    }

    @Test
    fun extract_futureDate_returnsOutOfRangeFuture() {
        val result = ApodDateParser.extract("2026/04/22", today)

        assertTrue(result is DateResult.OutOfRange)
        val outOfRange = result as DateResult.OutOfRange
        assertEquals(LocalDate.of(2026, 4, 22), outOfRange.date)
        assertFalse(outOfRange.tooOld)
    }

    @Test
    fun formatForDisplay_returnsSlashPaddedDate() {
        val result = ApodDateParser.formatForDisplay(LocalDate.of(2024, 1, 2))

        assertEquals("2024/01/02", result)
    }

    private fun assertValidDate(result: DateResult, expected: LocalDate) {
        assertTrue(result is DateResult.Valid)
        assertEquals(expected, (result as DateResult.Valid).date)
    }
}
