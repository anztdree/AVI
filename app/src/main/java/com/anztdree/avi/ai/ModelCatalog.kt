package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Katalog model Gemini — di-FETCH langsung dari Google (ListModels).
 * Pemilik selalu memakai daftar model paling up-to-date, tanpa hardcode.
 */
data class GeminiModelInfo(
    val name: String,          // "models/gemini-2.5-flash"
    val displayName: String,   // "Gemini 2.5 Flash"
    val description: String,
    val contextLimit: Long,
    val canChat: Boolean,
    val canTts: Boolean,
    val canLive: Boolean
) {
    val shortName: String get() = name.removePrefix("models/")
    /** Model uji-coba / eksperimental — tampil di bawah dengan penanda */
    val isPreview: Boolean get() =
        name.contains("preview", true) || name.contains("exp", true) ||
            name.contains("deprecated", true) || name.contains("image", true)
}

/** Hasil uji cepat satu model (1 permintaan kecil ke Google) */
data class ModelTestResult(
    val testing: Boolean,
    val ok: Boolean,
    val message: String
)

object ModelCatalog {

    suspend fun fetch(apiKey: String): Result<List<GeminiModelInfo>> = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty()) {
            return@withContext Result.failure(Exception("API key belum diisi pak."))
        }
        try {
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models?key=$key&pageSize=1000")
                .get()
                .build()

            Http.client.newCall(request).execute().use { response ->
                val body = try { response.body?.string() } catch (_: Exception) { null }
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Gagal muat model: ${extractError(body, "HTTP ${response.code}")}")
                    )
                }
                val parsed = jsonParser.decodeFromString<ModelListResponse>(body ?: "{}")
                val list = parsed.models.mapNotNull { m ->
                    val raw = m.name ?: return@mapNotNull null
                    val methods = m.supportedGenerationMethods
                    GeminiModelInfo(
                        name = raw,
                        displayName = m.displayName ?: raw.removePrefix("models/"),
                        description = m.description?.take(160) ?: "",
                        contextLimit = m.inputTokenLimit ?: 0L,
                        canChat = methods.contains("generateContent"),
                        canTts = raw.contains("tts", ignoreCase = true),
                        canLive = methods.any { it.startsWith("bidi", ignoreCase = true) } ||
                            raw.contains("live", ignoreCase = true)
                    )
                }
                if (list.isEmpty()) {
                    return@withContext Result.failure(Exception("Daftar model kosong. Coba lagi ya pak."))
                }
                Result.success(list.sortedWith(modelOrder))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Koneksi ke Google gagal: ${e.message ?: "unknown"}"))
        }
    }

    /** Urutan tampil: model stabil & terbaru dulu, preview/eksperimental di bawah */
    private val modelOrder = compareBy<GeminiModelInfo>(
        { if (it.isPreview) 1 else 0 },
        { when {
            it.shortName.contains("flash-lite") -> 2
            it.shortName.contains("flash") -> 0      // flash tercepat — pilihan utama
            it.shortName.contains("pro") -> 1
            else -> 3
        } },
        { it.shortName }   // abjad — versi terbaru biasanya di bawah
    )

    /**
     * Uji satu model — CEPAT (thinking dimatikan di keluarga 2.5-flash) dan
     * tahan limit: kalau 429, tunggu sesuai retryDelay Google (maks 30 dtk)
     * lalu coba ulang SEKALI.
     */
    suspend fun testModel(apiKey: String, model: String): ModelTestResult = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty()) return@withContext ModelTestResult(testing = false, ok = false, message = "API key kosong")
        try {
            val m = model.lowercase()
            val body = jsonParser.encodeToString(
                GeminiRequest(
                    contents = listOf(
                        GeminiContent(role = "user", parts = listOf(GeminiPart(text = "ping")))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.0,
                        maxOutputTokens = 8,
                        thinkingConfig = if (m.contains("2.5-flash")) GeminiThinkingConfig(0) else null
                    )
                )
            )
            fun doCall(): Pair<Int, String> {
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/$model:generateContent?key=$key")
                    .header("Content-Type", "application/json")
                    .post(body.toRequestBody(Http.jsonMedia))
                    .build()
                Http.testClient.newCall(request).execute().use { response ->
                    val errBody = if (response.isSuccessful) null else
                        try { response.body?.string() } catch (_: Exception) { null }
                    return Pair(response.code, extractError(errBody, "HTTP ${response.code}"))
                }
            }

            var (code, err) = doCall()
            if (code == 429) {
                // Kuota sesaat habis — hormati retryDelay Google, coba ulang sekali
                val wait = Regex("retryDelay\"?\\s*:\\s*\"(\\d+)s")
                    .find(err)?.groupValues?.get(1)?.toIntOrNull()?.coerceIn(1, 30) ?: 12
                delay(wait * 1000L)
                val retry = doCall()
                code = retry.first
                err = retry.second
            }
            when {
                code in 200..299 -> ModelTestResult(testing = false, ok = true, message = "SIAP DIPAKAI")
                code == 429 -> ModelTestResult(
                    testing = false,
                    ok = false,
                    message = "Kuota gratis model ini sedang habis (429). Tunggu sejenak lalu uji lagi — atau pilih model lain. Detail: $err"
                )
                code == 404 -> ModelTestResult(
                    testing = false,
                    ok = false,
                    message = "Model ini tidak tersedia lagi untuk API v1beta (404) — pilih model lain. Detail: $err"
                )
                else -> ModelTestResult(testing = false, ok = false, message = "$err")
            }
        } catch (e: Exception) {
            ModelTestResult(testing = false, ok = false, message = "Koneksi gagal: ${e.message ?: "unknown"}")
        }
    }
    /** Suara bawaan yang tersedia di model TTS Google (stabil, sesuai dokumentasi) */
    val ttsVoices: List<String> = listOf(
        "Zephyr", "Puck", "Charon", "Kore", "Fenrir", "Leda", "Orus", "Aoede",
        "Callirrhoe", "Autonoe", "Enceladus", "Iapetus", "Umbriel", "Algieba",
        "Despina", "Erinome", "Algenib", "Rasalgethi", "Laomedeia", "Achernar",
        "Alnilam", "Schedar", "Gacrux", "Pulcherrima", "Achird", "Zubenelgenubi",
        "Vindemiatrix", "Sadachbia", "Sadaltager", "Sulafat"
    )

    /** Pilih model live terbaik dari daftar (model aktif jika mendukung, else pertama yang live) */
    fun pickLiveModel(models: List<GeminiModelInfo>, active: String): String? {
        models.firstOrNull { it.name == active && it.canLive }?.let { return it.name }
        return models.firstOrNull { it.canLive && it.canChat }?.name
            ?: models.firstOrNull { it.canLive }?.name
    }

    /** Pilih model TTS dari daftar */
    fun pickTtsModel(models: List<GeminiModelInfo>, active: String): String? {
        models.firstOrNull { it.name == active && it.canTts }?.let { return it.name }
        return models.firstOrNull { it.canTts }?.name
    }
}
