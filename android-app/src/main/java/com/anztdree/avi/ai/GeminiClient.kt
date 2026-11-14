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
 * K17 — RISET WEB DIHAPUS TOTAL (keputusan pemilik): tools google_search
 * tidak pernah dikirim lagi. Alasannya terbukti (matriks DIAG-429, server AS,
 * 4/4 model): kuota grounding google_search free tier = 0 → request ber-tools
 * selalu ditolak 429 instan, sementara request tanpa-tools selalu 200. Pencarian
 * web nanti memakai provider lain (mis. DDG), bukan lewat Gemini.
 *
 * TANGGA 2 BENTUK (satu model, dua bentuk badan permintaan):
 *  1. BENTUK LENGKAP : systemInstruction + generationConfig (thinking dimatikan
 *                      utk model yang cuma-cuma bisa berpikir).
 *  2. BENTUK CURL BUKTI : badan persis curl pemilik — contents saja, TANPA
 *                      generationConfig apa pun. Terbukti jalan di nyaring.
 * Turun satu anak tangga tiap kena 400/404/5xx ATAU jawaban kosong (mis. token
 * habis dimakan "thinking"). 429 tanpa-tools = kuota sungguhan → dilaporkan.
 *
 * K22 — generateOnce(): SATU putaran model dengan perkakas AVI (function
 * calling). Bukti DIAG-AGENT v2 (runner AS, key pemilik):
 *   - fc tunggal 200 + functionCall tepat (2/2 model)
 *   - loop echo-verbatim + functionResponse 200 (2/2 model)
 *   - thinkingOff+tools 400 di flash-lite → tangga wajib turun ke bentuk minimal.
 * thoughtSignature pada part model HARUS dikembalikan verbatim.
 *
 * P48 — ROTASI KUNCI GEMINI: semua jalur (chat & agen) menerima daftar kunci
 * (utama + cadangan, AviSettings.geminiKeyList()). Kuota habis (429), akses
 * project ditolak (403), atau kunci invalid → otomatis lanjut ke kunci
 * berikutnya TANPA panggilan ulang yang membuang kuota.
 */
class GeminiClient : AiClient {

    /** Hasil SATU putaran model — kontrak AgentEngine. */
    sealed class Round {
        /** Model selesai — jawaban teks utuh. */
        data class Teks(val text: String) : Round()

        /** Model minta perkakas dijalankan. modelContent WAJIB di-echo verbatim
         *  (termasuk thoughtSignature) di putaran berikutnya. */
        data class Panggilan(
            val calls: List<GeminiFunctionCall>,
            val modelContent: GeminiContent
        ) : Round()

        /** Gagal bermakna (kuota/blokir) — AgentEngine jatuh ke jalur chat polos. */
        data class Gagal(val httpCode: Int, val pesan: String) : Round()
    }

    /** P48 — hasil percobaan SATU kunci pada jalur chat. */
    private sealed class HasilKunci {
        /** Jawaban sudah terkirim lewat callback. */
        data object Sukses : HasilKunci()

        /** Kunci ini bermasalah (kuota/ditolak) — layak coba kunci berikutnya. */
        data class Rotasi(val code: Int, val pesan: String) : HasilKunci()

        /** Bukan urusan kunci (jaringan/model/lokasi) — pesan sudah dilaporkan. */
        data object Berhenti : HasilKunci()
    }

    /**
     * P48 — kapan kunci layak diganti? Kuota habis (429), akses project ditolak
     * (403 — kunci cadangan bisa dari project BERBEDA jadi tetap dicoba), dan
     * kunci invalid (400 berbunyi "API key ..."). Selain itu percuma ganti kunci:
     * model tutup (404), Google bermasalah (5xx), lokasi jaringan ditolak, dsb.
     */
    private fun bolehRotasiKunci(code: Int, raw: String): Boolean {
        if (code == 429 || code == 403) return true
        if (code == 400) {
            val r = raw.lowercase()
            return r.contains("api key") || r.contains("api_key")
        }
        return false
    }

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

    // ===== K22+P48 — SATU PUTARAN DENGAN PERKAKAS + ROTASI KUNCI =====

    /** P48 — hasil percobaan SATU kunci pada jalur agen. */
    private sealed class HasilAgenKunci {
        /** Putaran sukses — Round siap dipakai (Teks / Panggilan). */
        data class Sukses(val round: Round) : HasilAgenKunci()

        /** Kunci ini bermasalah — layak dicoba kunci berikutnya. */
        data class Gagal(val code: Int, val pesan: String) : HasilAgenKunci()

        /** Koneksi putus — ganti kunci tidak menolong. */
        data class Putus(val pesan: String) : HasilAgenKunci()
    }

    /**
     * P48 — SATU putaran model dengan perkakas untuk SATU kunci.
     * Tangga bentuk (thinkingOff+config → minimal) dipertahankan persis.
     */
    private suspend fun putaranAgenSatuKunci(
        contents: List<GeminiContent>,
        settings: AviSettings,
        tools: List<GeminiTool>,
        model: String,
        key: String
    ): HasilAgenKunci = withContext(Dispatchers.IO) {
        // Putaran pamungkas tanpa perkakas: tools kosong TIDAK boleh dikirim
        // sebagai [] (INVALID_ARGUMENT) — harus dilepas dari JSON (null).
        val toolsKirim: List<GeminiTool>? = if (tools.isEmpty()) null else tools

        val si = GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(text = SystemPrompts.buildAgent(settings)))
        )

        fun body(minimal: Boolean): String {
            val req = if (minimal) {
                GeminiRequest(contents = contents, systemInstruction = si, tools = toolsKirim)
            } else {
                GeminiRequest(
                    contents = contents,
                    systemInstruction = si,
                    tools = toolsKirim,
                    generationConfig = GeminiGenerationConfig(
                        maxOutputTokens = 8192,
                        thinkingConfig = if (mungkinBerpikir(model)) GeminiThinkingConfig(0) else null
                    )
                )
            }
            return jsonParser.encodeToString(req)
        }

        val tangga = listOf(false, true) // false = bentuk penuh, true = minimal
        var kodeTerakhir = 0
        var pesanTerakhir = ""

        for ((index, minimal) in tangga.withIndex()) {
            val response = try {
                Http.client.newCall(request(model, key, body(minimal))).execute()
            } catch (e: Exception) {
                return@withContext HasilAgenKunci.Putus("Koneksi ke Google terputus (${e.message ?: "unknown"})")
            }

            response.use {
                val code = it.code
                val errBody = if (it.isSuccessful) null else
                    try { it.body?.string() } catch (_: Exception) { null }
                kodeTerakhir = code
                pesanTerakhir = extractError(errBody, "HTTP $code")

                if (!it.isSuccessful) {
                    val lanjut = code == 400 || code == 404 || code == 500 || code == 503
                    if (index < tangga.lastIndex && lanjut) return@use // turun tangga
                    return@withContext HasilAgenKunci.Gagal(code, pesanTerakhir)
                }

                val raw = try { it.body?.string() } catch (_: Exception) { null }
                val chunk = try {
                    raw?.let { r -> jsonParser.decodeFromString<GeminiChunk>(r) }
                } catch (_: Exception) { null }
                val cand = chunk?.candidates?.firstOrNull()
                val parts = cand?.content?.parts.orEmpty()

                val calls = parts.mapNotNull { p -> p.functionCall }
                    .filter { !it.name.isNullOrBlank() }

                if (calls.isNotEmpty()) {
                    // ECHO VERBATIM — bagian model persis seperti dikirim Google
                    // (functionCall + thoughtSignature). Bukti DIAG-AGENT v2.
                    val echo = GeminiContent(
                        role = cand?.content?.role ?: "model",
                        parts = parts
                    )
                    return@withContext HasilAgenKunci.Sukses(Round.Panggilan(calls, echo))
                }

                val text = parts.mapNotNull { p -> p.text }
                    .joinToString("") { t -> t }
                    .trim()
                if (text.isNotEmpty()) return@withContext HasilAgenKunci.Sukses(Round.Teks(text))

                // 200 tapi kosong → turun tangga bila bisa
                if (index < tangga.lastIndex) return@use
            }
        }

        HasilAgenKunci.Gagal(kodeTerakhir, pesanTerakhir)
    }

    /**
     * P48 — SATU putaran dengan ROTASI KUNCI: kunci utama dulu; kuota habis
     * (429) / kunci ditolak (403 / API key invalid) → lanjut kunci berikutnya
     * otomatis. Hasil putaran sukses dikembalikan APA ADANYA — tidak ada
     * panggilan ulang, kuota tidak terbuang dobel.
     */
    suspend fun generateOnce(
        contents: List<GeminiContent>,
        settings: AviSettings,
        tools: List<GeminiTool>
    ): Round {
        val keys = settings.geminiKeyList()
        if (keys.isEmpty()) return Round.Gagal(0, "API key kosong")
        val model = settings.geminiModel.trim()
        if (model.isEmpty()) return Round.Gagal(0, "Model belum dipilih")

        var kodeTerakhir = 0
        var pesanTerakhir = ""

        for (key in keys) {
            when (val h = putaranAgenSatuKunci(contents, settings, tools, model, key)) {
                is HasilAgenKunci.Sukses -> return h.round
                is HasilAgenKunci.Putus -> return Round.Gagal(0, h.pesan)
                is HasilAgenKunci.Gagal -> {
                    kodeTerakhir = h.code
                    pesanTerakhir = h.pesan
                    if (!bolehRotasiKunci(h.code, h.pesan)) break
                }
            }
        }

        val akhir = humanizeError(kodeTerakhir, pesanTerakhir, model) +
            if (keys.size > 1) " (Semua ${keys.size} kunci sudah dicoba otomatis, pak)" else ""
        return Round.Gagal(kodeTerakhir, akhir)
    }

    /** Model yang mungkin berpikir: keluarga 2.5 apa pun bentuk namanya + alias -latest. */
    private fun mungkinBerpikir(model: String): Boolean {
        val m = model.lowercase()
        return m.contains("2.5") || m.contains("-latest") || m.contains("thinking")
    }

    private fun buildBody(
        history: List<ChatMessage>,
        settings: AviSettings,
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
            val keys = settings.geminiKeyList()
            if (keys.isEmpty()) {
                callback.onError("API key Gemini belum diisi pak. Buka Pusat Kendali dan masukkan kuncinya ya.")
                return@withContext
            }
            val model = settings.geminiModel.trim()
            if (model.isEmpty()) {
                callback.onError(
                    "Model belum terpasang, pak. AVI memasang otomatis saat pesan dikirim — " +
                        "kalau pesan ini berulang, buka Pusat Kendali → OTAK → SIMPAN & MUAT DAFTAR MODEL " +
                        "lalu pilih model berbadge ✓."
                )
                return@withContext
            }

            // P48 — ROTASI KUNCI: kunci utama dulu; kuota habis / kunci ditolak →
            // kunci berikutnya otomatis, tanpa pemilik melakukan apa-apa.
            var kodeTerakhir = 0
            var pesanTerakhir = ""

            for (key in keys) {
                when (val h = cobaSatuKunciChat(history, settings, model, key, callback)) {
                    HasilKunci.Sukses, HasilKunci.Berhenti -> return@withContext
                    is HasilKunci.Rotasi -> {
                        kodeTerakhir = h.code
                        pesanTerakhir = h.pesan
                    }
                }
            }

            callback.onError(
                humanizeError(kodeTerakhir, pesanTerakhir, model) +
                    if (keys.size > 1) " (Semua ${keys.size} kunci sudah dicoba otomatis, pak)" else ""
            )
        }
    }

    /**
     * P48 — seluruh tangga bentuk (K17) untuk SATU kunci pada jalur chat.
     * Sukses  = jawaban sudah terkirim via onDelta/onComplete.
     * Rotasi  = kunci kuota-habis/ditolak → pemanggil lanjut kunci berikutnya.
     * Berhenti= bukan urusan kunci (jaringan/model/lokasi) → pesan sudah dilapor.
     */
    private fun cobaSatuKunciChat(
        history: List<ChatMessage>,
        settings: AviSettings,
        model: String,
        key: String,
        callback: AiStreamCallback
    ): HasilKunci {
        // Tangga K17 (tanpa riset web): (thinkingOff, minimal)
        val tangga = listOf(
            Pair(true, false),
            Pair(false, true)
        )

        var kodeTerakhir = 0
        var pesanTerakhir = ""
        for ((index, step) in tangga.withIndex()) {
            val (thinkingOff, minimal) = step
            val body = buildBody(history, settings, thinkingOff, minimal)

            val response = try {
                Http.client.newCall(request(model, key, body)).execute()
            } catch (e: Exception) {
                // Jaringan putus — ganti kunci tak menolong (bukan penolakan kunci)
                callback.onError(
                    "Koneksi ke Google terputus (${e.message ?: "unknown"}). " +
                        "Cek internet, VPN, lalu coba lagi ya pak."
                )
                return HasilKunci.Berhenti
            }

            response.use {
                val code = it.code
                val errBody = if (it.isSuccessful) null else
                    try { it.body?.string() } catch (_: Exception) { null }
                val message = extractError(errBody, "HTTP $code")
                kodeTerakhir = code
                pesanTerakhir = message

                if (!it.isSuccessful) {
                    // K17 — tidak ada request ber-tools, jadi 429 pasti kuota sungguhan.
                    val retriable = code == 400 || code == 404 || code == 500 || code == 503
                    val lokasiDitolak = code == 400 &&
                        message.lowercase().contains("location is not supported")
                    if (index < tangga.lastIndex && retriable && !lokasiDitolak) return@use // turun tangga

                    if (bolehRotasiKunci(code, message)) return HasilKunci.Rotasi(code, message)
                    callback.onError(humanizeError(code, message, model))
                    return HasilKunci.Berhenti
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
                    return HasilKunci.Sukses
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
                        "Kedua bentuk permintaan (termasuk bentuk curl bukti Pak) sudah dicoba. " +
                        "Coba UJI SEMUA MODEL di Pusat Kendali lalu pilih model berbadge ✓ ya, pak."
                )
                return HasilKunci.Berhenti
            }
        }

        // Seluruh tangga gagal → putuskan rotasi vs berhenti
        return if (bolehRotasiKunci(kodeTerakhir, pesanTerakhir)) {
            HasilKunci.Rotasi(kodeTerakhir, pesanTerakhir)
        } else {
            callback.onError(humanizeError(kodeTerakhir, pesanTerakhir, model))
            HasilKunci.Berhenti
        }
    }
}
