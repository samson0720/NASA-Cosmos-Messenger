package io.github.samson0720.cosmosmessenger.feature.chat.starcard

import androidx.annotation.ColorInt
import java.time.LocalDate

enum class ZodiacSign {
    Capricorn,
    Aquarius,
    Pisces,
    Aries,
    Taurus,
    Gemini,
    Cancer,
    Leo,
    Virgo,
    Libra,
    Scorpio,
    Sagittarius,
}

data class ZodiacAccent(
    val sign: ZodiacSign,
    val label: String,
    val subtitle: String,
    @ColorInt val color: Int,
)

object ZodiacAccentResolver {

    fun resolve(date: LocalDate): ZodiacAccent {
        val sign = when {
            date.isBetween(1, 20, 2, 18) -> ZodiacSign.Aquarius
            date.isBetween(2, 19, 3, 20) -> ZodiacSign.Pisces
            date.isBetween(3, 21, 4, 19) -> ZodiacSign.Aries
            date.isBetween(4, 20, 5, 20) -> ZodiacSign.Taurus
            date.isBetween(5, 21, 6, 20) -> ZodiacSign.Gemini
            date.isBetween(6, 21, 7, 22) -> ZodiacSign.Cancer
            date.isBetween(7, 23, 8, 22) -> ZodiacSign.Leo
            date.isBetween(8, 23, 9, 22) -> ZodiacSign.Virgo
            date.isBetween(9, 23, 10, 22) -> ZodiacSign.Libra
            date.isBetween(10, 23, 11, 21) -> ZodiacSign.Scorpio
            date.isBetween(11, 22, 12, 21) -> ZodiacSign.Sagittarius
            else -> ZodiacSign.Capricorn
        }
        return sign.toAccent()
    }

    private fun ZodiacSign.toAccent(): ZodiacAccent = when (this) {
        ZodiacSign.Capricorn -> ZodiacAccent(this, "Capricorn Birthday Sky", "Earth-toned midnight orbit", 0xFF8FA7A3.toInt())
        ZodiacSign.Aquarius -> ZodiacAccent(this, "Aquarius Birthday Sky", "Electric blue constellation trail", 0xFF74B9FF.toInt())
        ZodiacSign.Pisces -> ZodiacAccent(this, "Pisces Birthday Sky", "Soft nebula tide", 0xFF9B8CFF.toInt())
        ZodiacSign.Aries -> ZodiacAccent(this, "Aries Birthday Sky", "A bright launch spark", 0xFFFF7A7A.toInt())
        ZodiacSign.Taurus -> ZodiacAccent(this, "Taurus Birthday Sky", "A calm stellar horizon", 0xFF8FD6A3.toInt())
        ZodiacSign.Gemini -> ZodiacAccent(this, "Gemini Birthday Sky", "Twin points of starlight", 0xFFFFD166.toInt())
        ZodiacSign.Cancer -> ZodiacAccent(this, "Cancer Birthday Sky", "Moonlit cosmic shore", 0xFFB8D8FF.toInt())
        ZodiacSign.Leo -> ZodiacAccent(this, "Leo Birthday Sky", "Golden solar flare", 0xFFFFB84D.toInt())
        ZodiacSign.Virgo -> ZodiacAccent(this, "Virgo Birthday Sky", "Clean celestial line", 0xFFA8E6CF.toInt())
        ZodiacSign.Libra -> ZodiacAccent(this, "Libra Birthday Sky", "Balanced violet glow", 0xFFD0A2F7.toInt())
        ZodiacSign.Scorpio -> ZodiacAccent(this, "Scorpio Birthday Sky", "Deep crimson starfield", 0xFFFF6B9A.toInt())
        ZodiacSign.Sagittarius -> ZodiacAccent(this, "Sagittarius Birthday Sky", "Far-traveling comet arc", 0xFFFFC857.toInt())
    }

    private fun LocalDate.isBetween(
        startMonth: Int,
        startDay: Int,
        endMonth: Int,
        endDay: Int,
    ): Boolean {
        val value = monthValue * 100 + dayOfMonth
        val start = startMonth * 100 + startDay
        val end = endMonth * 100 + endDay
        return value in start..end
    }
}
