package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Klien Google Gemini (generativelanguage.googleapis.com) dengan streaming SSE.
 * Model diambil dari pengaturan (di-fetch dari ListModels) — TIDAK ADA HARDCODE.
 * Mendukung grounding Google Search dengan fallback otomatis.
 */
class GeminiClient : AiClient {

    private fun url(model: String, key: String): String =
        "https://generativelanguage.googleapis.com/v1beta/$model:streamGenerateContent?alt=sse&key=$key"

    private fun buildBody(history: List<ChatMessage>, settings: AviSettings, withTools: Boolean): String {
        val contents = history.map { msg ->
            GeminiContent(
                role = if (msg.role == "user") "user" else "model",
                parts = listOf(GeminiPart(text = msg.text))
            )
        }
        val req = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = SystemPrompts.build(settings)))
            ),
            tools = if (withTools && settings.webSearch) listOf(GeminiTool()) else null
        )
        return jsonParser.encodeToString(req)
    }

    override suspend fun stream(history: List<ChatMessage>, settings: AviSettings, callback: AiStreamCallback) {
        withContext(Dispatchers.IO) {
            val key = settings.geminiKey.trim()
            if (key.isEmpty()) {
                callback.onError("API key Gemini belum diisi pak. Buka Pusat Kendali dan masukkan kuncinya ya.")
                return@withContext
            }
            val model = settings.geminiModel.trim()
            if (model.isEmpty()) {
                callback.onError("Model belum dipilih pak. Buka Pusat Kendali → OTAK → Muat Daftar Model, lalu pilih model terbaru.")
                return@withContext
            }
            var useTools = settings.webSearch

            // percobaan pertama (dengan tools), jika ditolak ulangi tanpa tools
            repeat(2) { attempt ->
                val body = buildBody(history, settings, withTools = useTools)
                val request = Request.Builder()
                    .url(url(model, key))
                    .header("Content-Type", "application/json")
                    .post(body.toRequestBody(Http.jsonMedia))
                    .build()

                Http.client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = try { response.body?.string() } catch (_: Exception) { null }
                        val message = extractError(errBody, "HTTP ${response.code}")
                        if (useTools && attempt == 0 && response.code == 400) {
                            useTools = false
                            return@repeat
                        }
                        callback.onError("Gemini: $message")
                        return@withContext
                    }

                    val sb = StringBuilder()
                    readSse(response, CoroutineScope(currentCoroutineContext()), onEvent = { payload ->
                        try {
                            val chunk = jsonParser.decodeFromString<GeminiChunk>(payload)
                            val text = chunk.candidates.firstOrNull()
                                ?.content?.parts
                                ?.mapNotNull { it.text }
                                ?.joinToString("") { it }
                            if (!text.isNullOrEmpty()) {
                                sb.append(text)
                                callback.onDelta(text)
                            }
                        } catch (_: Exception) {
                        }
                    })

                    val full = sb.toString().trim()
                    if (full.isEmpty()) {
                        callback.onError("Gemini tidak mengirim jawaban. Coba ulangi ya pak.")
                    } else {
                        callback.onComplete(full)
                    }
                    return@withContext
                }
            }
        }
    }
}
