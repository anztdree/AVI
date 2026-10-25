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

    /**
     * Config generasi adaptif per model:
     * - Keluarga 2.5-flash: thinking dimatikan (budget 0) supaya jawaban instan
     *   dan token output tidak habis dipakai "pikiran" (penyebab jawaban kosong).
     * - Model lain: tanpa thinkingConfig (aman untuk pro / lama).
     * Buat versi minimal TANPA thinking/tools untuk fallback kalau model menolak.
     */
    private fun genConfig(model: String, minimal: Boolean): GeminiGenerationConfig {
        if (minimal) return GeminiGenerationConfig(maxOutputTokens = 8192)
        val noThinking = model.contains("2.5-flash") || model.contains("2.0-flash")
        return GeminiGenerationConfig(
            maxOutputTokens = 8192,
            thinkingConfig = if (noThinking) GeminiThinkingConfig(0) else null
        )
    }

    private fun buildBody(
        history: List<ChatMessage>,
        settings: AviSettings,
        withTools: Boolean,
        minimal: Boolean
    ): String {
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
            tools = if (withTools && settings.webSearch) listOf(GeminiTool()) else null,
            generationConfig = genConfig(settings.geminiModel, minimal)
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

            // Tangga fallback: (tools, config penuh) → (tanpa tools, config minimal)
            val ladder = listOf(
                settings.webSearch to false,
                false to true
            )
            for ((index, step) in ladder.withIndex()) {
                val (useTools, minimal) = step
                val body = buildBody(history, settings, withTools = useTools, minimal = minimal)
                val request = Request.Builder()
                    .url(url(model, key))
                    .header("Content-Type", "application/json")
                    .post(body.toRequestBody(Http.jsonMedia))
                    .build()

                val response = Http.client.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        val errBody = try { it.body?.string() } catch (_: Exception) { null }
                        val message = extractError(errBody, "HTTP ${it.code}")
                        if (index == 0 && it.code == 400) return@use // coba langkah berikutnya
                        callback.onError(humanizeError(it.code, message))
                        return@withContext
                    }
                    val sb = StringBuilder()
                    readSse(it, CoroutineScope(currentCoroutineContext()), onEvent = { payload ->
                        try {
                            val chunk = jsonParser.decodeFromString<GeminiChunk>(payload)
                            val text = chunk.candidates.firstOrNull()
                                ?.content?.parts
                                ?.mapNotNull { part -> part.text }
                                ?.joinToString("") { t -> t }
                            if (!text.isNullOrEmpty()) {
                                sb.append(text)
                                callback.onDelta(text)
                            }
                        } catch (_: Exception) {
                        }
                    })

                    val full = sb.toString().trim()
                    if (full.isEmpty()) {
                        callback.onError(
                            "Gemini tidak mengirim jawaban (model: ${model.removePrefix("models/")}). " +
                                "Coba pilih model lain di Pusat Kendali ya, pak."
                        )
                    } else {
                        callback.onComplete(full)
                    }
                    return@withContext
                }
            }
        }
    }
}
