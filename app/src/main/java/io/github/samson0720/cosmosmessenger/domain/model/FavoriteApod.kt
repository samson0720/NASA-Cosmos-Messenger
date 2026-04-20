package io.github.samson0720.cosmosmessenger.domain.model

import java.time.Instant

// Composes Apod rather than duplicating its fields: a favorite is "an APOD
// the user explicitly saved, plus when they saved it".
data class FavoriteApod(
    val apod: Apod,
    val savedAt: Instant,
)
