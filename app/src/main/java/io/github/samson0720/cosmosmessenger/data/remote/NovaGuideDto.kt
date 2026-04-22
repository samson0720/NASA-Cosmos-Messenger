package io.github.samson0720.cosmosmessenger.data.remote

import com.squareup.moshi.Json
import io.github.samson0720.cosmosmessenger.domain.model.Apod
import io.github.samson0720.cosmosmessenger.domain.model.NovaGuide
import io.github.samson0720.cosmosmessenger.domain.model.NovaGuideTerm

data class NovaGuideRequestDto(
    @Json(name = "date") val date: String,
    @Json(name = "title") val title: String,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "imageUrl") val imageUrl: String,
)

data class NovaGuideDto(
    @Json(name = "shortSummary") val shortSummary: String,
    @Json(name = "plainChinese") val plainChinese: String,
    @Json(name = "keyPoints") val keyPoints: List<String>,
    @Json(name = "terms") val terms: List<NovaGuideTermDto> = emptyList(),
    @Json(name = "source") val source: String,
)

data class NovaGuideTermDto(
    @Json(name = "term") val term: String,
    @Json(name = "zh") val zh: String,
    @Json(name = "explanation") val explanation: String,
)

fun Apod.toNovaGuideRequestDto(): NovaGuideRequestDto = NovaGuideRequestDto(
    date = date.toString(),
    title = title,
    explanation = explanation,
    imageUrl = hdUrl ?: url,
)

fun NovaGuideDto.toDomainOrNull(): NovaGuide? {
    val summary = shortSummary.trim()
    val body = plainChinese.trim()
    val points = keyPoints.map { it.trim() }.filter { it.isNotEmpty() }.take(3)
    val sourceText = source.trim()
    if (summary.isEmpty() || body.isEmpty() || points.size != 3 || sourceText.isEmpty()) {
        return null
    }

    return NovaGuide(
        shortSummary = summary,
        plainChinese = body,
        keyPoints = points,
        terms = terms.mapNotNull { it.toDomainOrNull() }.take(3),
        source = sourceText,
    )
}

private fun NovaGuideTermDto.toDomainOrNull(): NovaGuideTerm? {
    val termText = term.trim()
    val zhText = zh.trim()
    val explanationText = explanation.trim()
    if (termText.isEmpty() || zhText.isEmpty() || explanationText.isEmpty()) return null

    return NovaGuideTerm(
        term = termText,
        zh = zhText,
        explanation = explanationText,
    )
}
