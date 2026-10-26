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
 *           X-goog-api-key: <API key>
 *
 * NON-STREAMING. Satu permintaan → satu jawaban utuh.
 *
 * K14 — TANGGA ADAPTIF 3 BENTUK (satu model, tiga bentuk badan permintaan):
 *  1. PENUH   : tools (kalau riset aktif) + thinking dimatikan utk model yang
 *               cuma-cuma bisa berpikir (keluarga 2.5 / alias -latest).
 *  2. TANPA TOOLS : thinking off, tanpa google_search.
 *  3. BENTUK CURL BUKTI : badan persis curl pemilik — contents saja, TANPA
 *               generationConfig apa pun. Bentuk inilah yang terbukti jalan di
 *               nyaring, jadi selalu jadi bentuk penyelamat terakhir.
 * Turun satu anak tangga tiap kena 400/404/5xx ATAU jawaban kosong (mis. token
 * habis dimakan "thinking") — bukti lapangan: 2.5 bisa menghabiskan seluruh
 * budget token utk berpikir lalu mengirim teks kosong.
 */
class GeminiClient : AiClient {

    private fun url(model: String): String =
        "https://generativelanguage.googleapis.com/v1beta/$model:generateContent"

    /** Auth selalu lewat HEADER X-goog-api-key (key "AQ." baru & "AIza" lama). */
    private fun request(model: String, key: String, body: String): Request =
        okhttpAuth(Request.Builder(), key)
            .url(url(model))
            .header("Content-Type", "application/json")
            .post(body.toRequestBody(Http.jsonMedia))
            .build()

    private fun contentsOf(history: List<ChatMessage>): List<GeminiContent> =
        history.map { msg ->
            GeminiContent(
                role = if (msg.role == "user") "user" else "model",
                parts = listOf(GeminiPart(text = msg.text))
            )
        }

    /** Model yang mungkin berpikir: keluarga 2.5 apa pun bentuk namanya + alias -latest. */
    private fun mungkinBerpikir(model: String): Boolean {
        val m = model.lowercase()
        return m.contains("2.5") || m.contains("-latest") || m.contains("thinking")
    }

    private fun buildBody(
        history: List<ChatMessage>,
        settings: AviSettings,
        withTools: Boolean,
        thinkingOff: Boolean,
        minimal: Boolean
    ): String {
        val contents = contentsOf(history)
        if (minimal) {
            // BENTUK CURL BUKTI pemilik — tanpa generationConfig, tanpa tools.
            return jsonParser.encodeToString(
                GeminiRequest(
                    contents = contents,
                    systemInstruction = GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = SystemPrompts.build(settings)))
                    )
                )
            )
        }
        return jsonParser.encodeToString(
            GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = SystemPrompts.build(settings)))
                ),
                tools = if (withTools && settings.webSearch) listOf(GeminiTool()) else null,
                generationConfig = GeminiGenerationConfig(
                    maxOutputTokens = 8192,
                    thinkingConfig = if (thinkingOff && mungkinBerpikir(settings.geminiModel))
                        GeminiThinkingConfig(0) else null
                )
            )
        )
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

            // Tangga K14: (tools, thinkingOff, minimal)
            val tangga = listOf(
                Triple(true, true, false),
                Triple(false, true, false),
                Triple(false, false, true)
            )

            var kodeTerakhir = 0
            var pesanTerakhir = ""
            for ((index, step) in tangga.withIndex()) {
                val (useTools, thinkingOff, minimal) = step
                val body = buildBody(history, settings, useTools, thinkingOff, minimal)

                val response = try {
                    Http.client.newCall(request(model, key, body)).execute()
                } catch (e: Exception) {
                    // Jaringan putus — tidak ada gunanya turun tangga (bukan penolakan model)
                    callback.onError(
                        "Koneksi ke Google terputus (${e.message ?: "unknown"}). " +
                            "Cek internet, VPN, lalu coba lagi ya pak."
                    )
                    return@withContext
                }

                response.use {
                    val code = it.code
                    val errBody = if (it.isSuccessful) null else
                        try { it.body?.string() } catch (_: Exception) { null }
                    val message = extractError(errBody, "HTTP $code")
                    kodeTerakhir = code
                    pesanTerakhir = message

                    if (!it.isSuccessful) {
                        if (isProjectLevelBlock(code, message)) {
                            callback.onError(humanizeError(code, message, model))
                            return@withContext
                        }
                        val retriable = code == 400 || code == 404 || code == 500 || code == 503
                        if (index < tangga.lastIndex && retriable) return@use // turun tangga
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

                    if (text.isNotEmpty()) {
                        callback.onDelta(text)
                        callback.onComplete(text)
                        return@withContext
                    }

                    // 200 tapi kosong → turun tangga (bentuk berikutnya), kecuali ini bentuk terakhir.
                    if (index < tangga.lastIndex) return@use

                    val blocked = chunk?.promptFeedback?.blockReason
                    val alasan = when {
                        !blocked.isNullOrBlank() ->
                            "permintaan diblokir filter Google ($blocked)"
                        finishReason == "SAFETY" || finishReason == "PROHIBITED_CONTENT" || finishReason == "BLOCKLIST" ->
                            "jawaban diblokir filter keamanan Google"
                        finishReason == "RECITATION" ->
                            "jawaban dicegah karena mirip sumber tertentu"
                        finishReason == "MAX_TOKENS" ->
                            "budget token habis (umumnya dimakan proses berpikir model)"
                        else -> "model merespons kosong"
                    }
                    callback.onError(
                        "Gemini tidak mengirim jawaban (model: ${model.removePrefix("models/")}) — $alasan. " +
                            "Ketiga bentuk permintaan (termasuk bentuk curl bukti Pak) sudah dicoba. " +
                            "Coba UJI SEMUA MODEL di Pusat Kendali lalu pilih model berbadge ✓ ya, pak."
                    )
                    return@withContext
                }
            }
        }
    }
}
