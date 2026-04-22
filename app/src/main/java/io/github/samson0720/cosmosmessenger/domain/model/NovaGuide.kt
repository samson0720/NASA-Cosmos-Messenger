package io.github.samson0720.cosmosmessenger.domain.model

data class NovaGuide(
    val shortSummary: String,
    val plainChinese: String,
    val keyPoints: List<String>,
    val terms: List<NovaGuideTerm>,
    val source: String,
)

data class NovaGuideTerm(
    val term: String,
    val zh: String,
    val explanation: String,
)
