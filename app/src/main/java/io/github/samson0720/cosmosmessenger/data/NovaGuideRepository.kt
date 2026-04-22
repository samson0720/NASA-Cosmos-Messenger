package io.github.samson0720.cosmosmessenger.data

import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.NovaGuide

interface NovaGuideRepository {
    suspend fun explain(apod: Apod): Result<NovaGuide>
}
