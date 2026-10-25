package com.anztdree.avi.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

val jsonParser: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
}

// ============ GEMINI DTO ============

@Serializable
data class GeminiPart(
    val text: String? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart> = emptyList()
)

@Serializable
data class GeminiTool(
    @SerialName("google_search") val googleSearch: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Double = 0.8,
    val maxOutputTokens: Int = 2048
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val tools: List<GeminiTool>? = null,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
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
data class GeminiChunk(
    val candidates: List<GeminiCandidate> = emptyList()
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
