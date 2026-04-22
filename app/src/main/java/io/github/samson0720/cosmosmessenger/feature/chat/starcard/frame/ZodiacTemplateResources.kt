package io.github.samson0720.cosmosmessenger.feature.chat.starcard.frame

import androidx.annotation.DrawableRes
import io.github.samson0720.cosmosmessenger.R
import io.github.samson0720.cosmosmessenger.feature.chat.starcard.ZodiacSign

internal object ZodiacTemplateResources {

    @DrawableRes
    fun resourceIdFor(sign: ZodiacSign): Int = when (sign) {
        ZodiacSign.Capricorn -> R.drawable.starcard_template_capricorn
        ZodiacSign.Aquarius -> R.drawable.starcard_template_aquarius
        ZodiacSign.Pisces -> R.drawable.starcard_template_pisces
        ZodiacSign.Aries -> R.drawable.starcard_template_aries
        ZodiacSign.Taurus -> R.drawable.starcard_template_taurus
        ZodiacSign.Gemini -> R.drawable.starcard_template_gemini
        ZodiacSign.Cancer -> R.drawable.starcard_template_cancer
        ZodiacSign.Leo -> R.drawable.starcard_template_leo
        ZodiacSign.Virgo -> R.drawable.starcard_template_virgo
        ZodiacSign.Libra -> R.drawable.starcard_template_libra
        ZodiacSign.Scorpio -> R.drawable.starcard_template_scorpio
        ZodiacSign.Sagittarius -> R.drawable.starcard_template_sagittarius
    }
}
