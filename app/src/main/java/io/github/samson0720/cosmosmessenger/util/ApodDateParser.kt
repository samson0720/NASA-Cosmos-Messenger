package io.github.samson0720.cosmosmessenger.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

// Centralized date handling for APOD. Anything that needs to turn raw
// user input into a LocalDate (or validate its range) should go through
// here so parsing rules stay in one place.
object ApodDateParser {

    val EARLIEST: LocalDate = LocalDate.of(1995, 6, 16)

    // yyyy-MM-dd, yyyy/MM/dd, or yyyy.MM.dd — 4-digit year, 1-2 digit
    // month/day, same separator throughout. Matched on word boundaries
    // so dates embedded in a sentence still work.
    private val DATE_REGEX = Regex("""(?<!\d)(\d{4})([-/.])(\d{1,2})\2(\d{1,2})(?!\d)""")

    // Looser detection: "4-digit year, 1-3 non-digit chars, 1-2 digits,
    // 1-3 non-digit chars, 1-2 digits" — catches clearly date-like but
    // unsupported inputs (e.g. 2024年01月01日, mixed separators like
    // 2024-01/01) so the ViewModel surfaces invalid-date guidance
    // instead of silently fetching today's APOD.
    private val DATE_LIKE_REGEX = Regex("""(?<!\d)\d{4}\D{1,3}\d{1,2}\D{1,3}\d{1,2}(?!\d)""")

    // 'uuuu' (proleptic year) is required instead of 'yyyy' so STRICT
    // resolver style doesn't demand an explicit era pattern.
    private val FORMATTER_DASH: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT)
    private val FORMATTER_SLASH: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu/M/d").withResolverStyle(ResolverStyle.STRICT)
    private val FORMATTER_DOT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu.M.d").withResolverStyle(ResolverStyle.STRICT)

    private val DISPLAY_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd")

    fun formatForDisplay(date: LocalDate): String = date.format(DISPLAY_FORMATTER)

    sealed interface DateResult {
        data object None : DateResult
        data class Valid(val date: LocalDate) : DateResult
        data object Malformed : DateResult   // looked like a date but not a real calendar day
        data class OutOfRange(val date: LocalDate, val tooOld: Boolean) : DateResult
    }

    fun extract(input: String, today: LocalDate = LocalDate.now()): DateResult {
        val match = DATE_REGEX.find(input)
            ?: return if (DATE_LIKE_REGEX.containsMatchIn(input)) DateResult.Malformed
            else DateResult.None
        val separator = match.groupValues[2]
        val formatter = when (separator) {
            "-" -> FORMATTER_DASH
            "/" -> FORMATTER_SLASH
            else -> FORMATTER_DOT
        }
        val raw = match.value
        val parsed = runCatching { LocalDate.parse(raw, formatter) }.getOrNull()
            ?: return DateResult.Malformed

        return when {
            parsed.isBefore(EARLIEST) -> DateResult.OutOfRange(parsed, tooOld = true)
            parsed.isAfter(today) -> DateResult.OutOfRange(parsed, tooOld = false)
            else -> DateResult.Valid(parsed)
        }
    }
}
