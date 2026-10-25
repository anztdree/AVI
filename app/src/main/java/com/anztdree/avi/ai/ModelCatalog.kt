package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import kotlinx.coroutines.Dispatchers
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
     * Uji satu model dengan 1 permintaan kecil (maks 8 token).
     * Membuktikan model benar-benar bisa dipakai dengan API key pemilik —
     * hasilnya ditampilkan langsung di Pusat Kendali.
     */
    suspend fun testModel(apiKey: String, model: String): ModelTestResult = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty()) return@withContext ModelTestResult(false, "API key kosong")
        try {
            val body = jsonParser.encodeToString(
                GeminiRequest(
                    contents = listOf(
                        GeminiContent(role = "user", parts = listOf(GeminiPart(text = "ping")))
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.0, maxOutputTokens = 8)
                )
            )
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/$model:generateContent?key=$key")
                .header("Content-Type", "application/json")
                .post(body.toRequestBody(Http.jsonMedia))
                .build()
            Http.client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    ModelTestResult(testing = false, ok = true, message = "SIAP DIPAKAI")
                } else {
                    val errBody = try { response.body?.string() } catch (_: Exception) { null }
                    ModelTestResult(testing = false, ok = false, message = extractError(errBody, "HTTP ${response.code}"))
                }
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
