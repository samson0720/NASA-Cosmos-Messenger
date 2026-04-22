package io.github.samson0720.cosmosmessenger.feature.chat.starcard

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZodiacAccentResolverTest {

    @Test
    fun resolve_startOfAquarius_returnsAquarius() {
        val accent = ZodiacAccentResolver.resolve(LocalDate.of(2024, 1, 20))

        assertEquals(ZodiacSign.Aquarius, accent.sign)
        assertTrue(accent.label.contains("水瓶座"))
    }

    @Test
    fun resolve_startOfLeo_returnsLeo() {
        val accent = ZodiacAccentResolver.resolve(LocalDate.of(2024, 7, 23))

        assertEquals(ZodiacSign.Leo, accent.sign)
        assertTrue(accent.label.contains("獅子座"))
    }

    @Test
    fun resolve_endOfYear_returnsCapricorn() {
        val accent = ZodiacAccentResolver.resolve(LocalDate.of(2024, 12, 31))

        assertEquals(ZodiacSign.Capricorn, accent.sign)
        assertTrue(accent.label.contains("摩羯座"))
    }
}
