package com.anztdree.avi.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val jsonParser: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
}

// ============ GEMINI DTO ============

@Serializable
data class GeminiBlob(
    @SerialName("mimeType") val mimeType: String? = null,
    @SerialName("data") val data: String? = null
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inlineData") val inlineData: GeminiBlob? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart> = emptyList()
)

@Serializable
data class GeminiPrebuiltVoiceConfig(
    @SerialName("voiceName") val voiceName: String? = null
)

@Serializable
data class GeminiVoiceConfig(
    @SerialName("prebuiltVoiceConfig") val prebuiltVoiceConfig: GeminiPrebuiltVoiceConfig? = null
)

@Serializable
data class GeminiSpeechConfig(
    @SerialName("voiceConfig") val voiceConfig: GeminiVoiceConfig? = null
)

@Serializable
data class GeminiThinkingConfig(
    @SerialName("thinkingBudget") val thinkingBudget: Int = 0
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Double = 0.8,
    val maxOutputTokens: Int = 8192,
    @SerialName("thinkingConfig") val thinkingConfig: GeminiThinkingConfig? = null,
    @SerialName("responseModalities") val responseModalities: List<String>? = null,
    @SerialName("speechConfig") val speechConfig: GeminiSpeechConfig? = null
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    // K17 — riset web dihapus total: tidak ada field tools lagi.
    // K14 — nullable: kalau tidak dikirim, field ini DILEPAS dari JSON
    // (explicitNulls=false) sehingga badan permintaan bisa PERSIS curl bukti
    // pemilik: {"contents":[{"parts":[{"text":"..."}]}]} tanpa generationConfig.
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@Serializable
data class GeminiErrorDetail(val message: String? = null)

@Serializable
data class GeminiErrorBody(val error: GeminiErrorDetail? = null)

@Serializable
data class GeminiPromptFeedback(
    @SerialName("blockReason") val blockReason: String? = null
)

@Serializable
data class GeminiChunk(
    val candidates: List<GeminiCandidate> = emptyList(),
    @SerialName("promptFeedback") val promptFeedback: GeminiPromptFeedback? = null
)

// ============ MODEL CATALOG DTO (ListModels) ============

@Serializable
data class ModelDto(
    val name: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    val description: String? = null,
    @SerialName("inputTokenLimit") val inputTokenLimit: Long? = null,
    @SerialName("supportedGenerationMethods") val supportedGenerationMethods: List<String> = emptyList()
)

@Serializable
data class ModelListResponse(
    val models: List<ModelDto> = emptyList()
)

// ============ OPENAI-COMPATIBLE DTO ============

@Serializable
data class OcMessage(val role: String, val content: String)

@Serializable
data class OcRequest(
    val model: String,
    val messages: List<OcMessage>,
    val stream: Boolean = true,
    val temperature: Double = 0.8
)

@Serializable
data class OcDelta(val content: String? = null)

@Serializable
data class OcChoice(val delta: OcDelta? = null, val finishReason: String? = null)

@Serializable
data class OcChunk(val choices: List<OcChoice> = emptyList())

@Serializable
data class OcErrorDetail(val message: String? = null)

@Serializable
data class OcErrorBody(val error: OcErrorDetail? = null)
