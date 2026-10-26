package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Klien Google Gemini — PERSIS mengikuti spesifikasi terbukti milik pemilik:
 *
 *   POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
 *   Header: Content-Type: application/json
 *           x-goog-api-key: <API key>
 *
 * NON-STREAMING. Satu permintaan → satu jawaban utuh. Tidak ada lagi jalur
 * SSE/streaming yang di-buffer sebagian jaringan seluler Indonesia sehingga
 * terlihat "loading lama lalu gagal semua".
 *
 * Model diambil dari pengaturan (di-fetch dari ListModels) — TIDAK ADA HARDCODE.
 */
class GeminiClient : AiClient {

    private fun url(model: String): String =
        "https://generativelanguage.googleapis.com/v1beta/$model:generateContent"

    /** Auth selalu lewat HEADER x-goog-api-key (key baru "AQ." & lama "AIza." sama-sama didukung). */
    private fun request(model: String, key: String, body: String): Request =
        okhttpAuth(Request.Builder(), key)
            .url(url(model))
            .header("Content-Type", "application/json")
            .post(body.toRequestBody(Http.jsonMedia))
            .build()

    /**
     * Config generasi adaptif per model (aturan resmi Google):
     * - HANYA keluarga 2.5-flash yang boleh thinkingBudget = 0 (model lain 400 / wajib berpikir).
     * - Versi minimal TANPA thinking/tools untuk fallback kalau model menolak.
     */
    private fun genConfig(model: String, minimal: Boolean): GeminiGenerationConfig {
        if (minimal) return GeminiGenerationConfig(maxOutputTokens = 8192)
        val m = model.lowercase()
        val allowThinkingOff = m.contains("2.5-flash")
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
                    val code = it.code
                    val errBody = if (it.isSuccessful) null else
                        try { it.body?.string() } catch (_: Exception) { null }

                    if (!it.isSuccessful) {
                        val message = extractError(errBody, "HTTP $code")
                        // Blok tingkat-project (free tier/lokasi): mencoba langkah lain
                        // PERCUMA, semua model kena penolakan sama → berhenti sekarang.
                        if (isProjectLevelBlock(code, message)) {
                            callback.onError(humanizeError(code, message, model))
                            return@withContext
                        }
                        val retriable = code == 400 || code == 404 || code == 500 || code == 503
                        if (index == 0 && retriable) return@use // turun ke langkah minimal
                        callback.onError(humanizeError(code, message, model))
                        return@withContext
                    }

                    // ---- Jawaban utuh (non-streaming) ----
                    val raw = try { it.body?.string() } catch (_: Exception) { null }
                    val chunk = try {
                        raw?.let { r -> jsonParser.decodeFromString<GeminiChunk>(r) }
                    } catch (_: Exception) { null }

                    val cand = chunk?.candidates?.firstOrNull()
                    val finishReason = cand?.finishReason
                    val text = cand?.content?.parts
                        ?.mapNotNull { part -> part.text }
                        ?.joinToString("") { t -> t }
                        ?.trim()
                        .orEmpty()

                    val blocked = chunk?.promptFeedback?.blockReason
                    if (text.isNotEmpty()) {
                        callback.onDelta(text)
                        callback.onComplete(text)
                        return@withContext
                    }

                    val alasan = when {
                        !blocked.isNullOrBlank() ->
                            "permintaan diblokir filter Google ($blocked)"
                        finishReason == "SAFETY" || finishReason == "PROHIBITED_CONTENT" || finishReason == "BLOCKLIST" ->
                            "jawaban diblokir filter keamanan Google"
                        finishReason == "RECITATION" ->
                            "jawaban dicegah karena mirip sumber tertentu"
                        finishReason == "MAX_TOKENS" ->
                            "token output habis — turunkan panjang konteks"
                        else -> "model merespons kosong"
                    }
                    callback.onError(
                        "Gemini tidak mengirim jawaban (model: ${model.removePrefix("models/")}) — $alasan." +
                            " Coba pilih model lain di Pusat Kendali ya, pak."
                    )
                    return@withContext
                }
            }
        }
    }
}
