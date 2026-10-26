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

    private fun url(model: String): String =
        "https://generativelanguage.googleapis.com/v1beta/$model:streamGenerateContent?alt=sse"

    /**
     * K11 — auth selalu lewat HEADER x-goog-api-key (format key baru "AQ." dan
     * lama "AIza." sama-sama didukung; sama persis dengan contoh resmi Google).
     */
    private fun request(model: String, key: String, body: String): Request =
        okhttpAuth(Request.Builder(), key)
            .url(url(model))
            .header("Content-Type", "application/json")
            .post(body.toRequestBody(Http.jsonMedia))
            .build()

    /**
     * Config generasi adaptif per model (aturan resmi Google):
     * - HANYA keluarga 2.5-flash (flash & flash-lite) yang boleh menerima
     *   thinkingBudget = 0. Model 2.0 TIDAK kenal thinkingConfig (400 error),
     *   dan 2.5-pro WAJIB berpikir (budget 0 ditolak).
     * - Versi minimal TANPA thinking/tools untuk fallback kalau model menolak.
     */
    private fun genConfig(model: String, minimal: Boolean): GeminiGenerationConfig {
        if (minimal) return GeminiGenerationConfig(maxOutputTokens = 8192)
        val m = model.lowercase()
        val allowThinkingOff = m.contains("2.5-flash")   // mencakup flash & flash-lite
        return GeminiGenerationConfig(
            maxOutputTokens = 8192,
            thinkingConfig = if (allowThinkingOff) GeminiThinkingConfig(0) else null
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

            // Tangga fallback: (tools, config penuh) → (tanpa tools, config minimal).
            // Gagal 400/404/500/503 → turun ke langkah berikutnya DENGAN MODEL YANG SAMA.
            // 429/401/403 → berhenti dengan pesan yang jelas.
            val ladder = listOf(
                settings.webSearch to false,
                false to true
            )
            for ((index, step) in ladder.withIndex()) {
                val (useTools, minimal) = step
                val body = buildBody(history, settings, withTools = useTools, minimal = minimal)

                val response = Http.client.newCall(request(model, key, body)).execute()
                response.use {
                    if (!it.isSuccessful) {
                        val errBody = try { it.body?.string() } catch (_: Exception) { null }
                        val message = extractError(errBody, "HTTP ${it.code}")
                        // K11 — blok tingkat-project (free tier/lokasi): mencoba langkah
                        // lain PERCUMA, semua model kena penolakan sama → berhenti sekarang.
                        if (isProjectLevelBlock(it.code, message)) {
                            callback.onError(humanizeError(it.code, message, model))
                            return@withContext
                        }
                        val retriable = it.code == 400 || it.code == 404 ||
                            it.code == 500 || it.code == 503
                        if (index == 0 && retriable) return@use // turun ke langkah minimal
                        callback.onError(humanizeError(it.code, message, model))
                        return@withContext
                    }
                    val sb = StringBuilder()
                    var finishReason: String? = null
                    readSse(it, CoroutineScope(currentCoroutineContext()), onEvent = { payload ->
                        try {
                            val chunk = jsonParser.decodeFromString<GeminiChunk>(payload)
                            finishReason = chunk.candidates.firstOrNull()?.finishReason ?: finishReason
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
                        val alasan = when (finishReason) {
                            "SAFETY", "PROHIBITED_CONTENT", "BLOCKLIST" ->
                                "jawaban diblokir filter keamanan Google"
                            "RECITATION" -> "jawaban dicegah karena mirip sumber tertentu"
                            "MAX_TOKENS" -> "token output habis — turunkan panjang konteks"
                            else -> "model merespons kosong"
                        }
                        callback.onError(
                            "Gemini tidak mengirim jawaban (model: ${model.removePrefix("models/")}) — $alasan." +
                                " Coba pilih model lain di Pusat Kendali ya, pak."
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
