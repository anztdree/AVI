package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * P49 — KATALOG NVIDIA NIM (umpan Pak: "cuma ada 2 tipe ai .. Gemini dan
 * NVDIA NIM .. fetch model masih gagal").
 *
 * NVIDIA NIM = API kompatibel OpenAI:
 *   DAFTAR MODEL : GET  {base}/models          header Authorization: Bearer <key>
 *   OBROLAN      : POST {base}/chat/completions (SSE bila stream=true)
 * Base resmi: https://integrate.api.nvidia.com/v1 — kunci dari build.nvidia.com
 * berawalan "nvapi-…".
 *
 * Bila daftar tidak bisa dijangkau (jaringan/kunci), AVI TETAP menawarkan
 * DAFTAR CADANGAN TERKURASI di bawah — pemilik tidak pernah menatap layar
 * kosong, dan model tetap bisa diganti-ganti bebas.
 */
object NimCatalog {

    @Serializable
    data class NimModelDto(val id: String? = null)

    @Serializable
    data class NimModelList(val data: List<NimModelDto> = emptyList())

    /** Model obrolan NIM yang sehat & populer — dipakai bila fetch gagal. */
    val fallback: List<String> = listOf(
        "meta/llama-3.3-70b-instruct",
        "meta/llama-3.1-8b-instruct",
        "nvidia/llama-3.1-nemotron-70b-instruct",
        "deepseek-ai/deepseek-r1",
        "qwen/qwen2.5-7b-instruct",
        "openai/gpt-oss-120b"
    )

    /** Model pilihan otomatis saat kunci baru dipasang (paling seimbang). */
    const val DEFAULT_MODEL = "meta/llama-3.3-70b-instruct"

    /** Nama tampil enak: "meta/llama-3.3-70b-instruct" → "llama-3.3-70b-instruct" */
    fun shortName(id: String): String = id.substringAfterLast('/', id)

    /**
     * Ambil daftar model langsung dari NVIDIA. Daftar diurutkan: model
     * fallback populer dulu, lalu sisanya abjad — supaya pilihan terbaik
     * selalu di atas.
     */
    suspend fun fetch(settings: AviSettings): Result<List<String>> = withContext(Dispatchers.IO) {
        val key = settings.ocKey.trim()
        if (key.isEmpty()) return@withContext Result.failure(Exception("Kunci NVIDIA NIM belum diisi, pak."))
        val base = settings.ocBaseUrl.trim().trimEnd('/')
        try {
            val request = Request.Builder()
                .url("$base/models")
                .header("Authorization", "Bearer $key")
                .header("Accept", "application/json")
                .get()
                .build()
            Http.client.newCall(request).execute().use { resp ->
                val body = try { resp.body?.string() } catch (_: Exception) { null }
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(
                        Exception(nimError(resp.code, extractError(body, "HTTP ${resp.code}")))
                    )
                }
                val parsed = try {
                    jsonParser.decodeFromString<NimModelList>(body ?: "{}")
                } catch (_: Exception) { null }
                val ids = parsed?.data?.mapNotNull { it.id?.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.distinct()
                    .orEmpty()
                if (ids.isEmpty()) {
                    return@withContext Result.failure(
                        Exception("Daftar model NIM kosong — pakai daftar cadangan di bawah ya pak.")
                    )
                }
                // urutkan: fallback dulu (urutan aslinya), lalu sisanya abjad
                val ranked = fallback.filter { it in ids } +
                    ids.filter { it !in fallback }.sorted()
                Result.success(ranked)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Koneksi ke NVIDIA gagal: ${e.message ?: "unknown"}"))
        }
    }

    /** Pesan error NIM yang enak dibaca + jalan keluar. */
    fun nimError(code: Int, raw: String): String = when {
        code == 401 || code == 403 ->
            "Kunci NVIDIA NIM ditolak (HTTP $code). Pastikan kunci \"nvapi-…\" dari " +
                "build.nvidia.com masih aktif, atau buat kunci baru di build.nvidia.com"
        code == 429 ->
            "Kuota NVIDIA NIM habis sementara (HTTP 429). Tunggu sebentar, atau pindah ke otak Gemini ya pak."
        code == 404 ->
            "Alamat server NIM tidak ditemukan (HTTP 404). Base URL harus berakhiran /v1 — cek kolom alamat server. Detail: $raw"
        else -> "NVIDIA NIM menolak (HTTP $code): $raw"
    }

    /**
     * Uji cepat satu model NIM — satu chat completion mini (non-stream,
     * maks 16 token) dengan timeout ketat. Dipanggil saat model diketuk.
     */
    suspend fun testModel(settings: AviSettings, model: String): ModelTestResult =
        withContext(Dispatchers.IO) {
            val key = settings.ocKey.trim()
            if (key.isEmpty()) return@withContext ModelTestResult(false, false, "Kunci NIM kosong")
            val base = settings.ocBaseUrl.trim().trimEnd('/')
            val body = jsonParser.encodeToString(
                OcRequest(
                    model = model,
                    messages = listOf(OcMessage("user", "ping")),
                    stream = false,     // uji: non-stream, jawaban utuh sekaligus
                    temperature = 0.0
                )
            )
            val request = Request.Builder()
                .url("$base/chat/completions")
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .post(body.toRequestBody(Http.jsonMedia))
                .build()
            try {
                Http.testClient.newCall(request).execute().use { resp ->
                    val errBody = if (resp.isSuccessful) null else
                        try { resp.body?.string() } catch (_: Exception) { null }
                    val err = extractError(errBody, "HTTP ${resp.code}")
                    when {
                        resp.isSuccessful -> ModelTestResult(false, true, "SIAP DIPAKAI")
                        resp.code == 429 -> ModelTestResult(
                            false, false,
                            "Kuota model ini sedang penuh (429) — coba lagi nanti atau pilih model lain."
                        )
                        else -> ModelTestResult(false, false, nimError(resp.code, err))
                    }
                }
            } catch (e: Exception) {
                ModelTestResult(false, false, "Koneksi gagal: ${e.message ?: "unknown"}")
            }
        }

    /** Base URL sedang menunjuk NVIDIA resmi? (untuk catatan UI) */
    fun isOfficialBase(settings: AviSettings): Boolean =
        settings.ocBaseUrl.trim().trimEnd('/') == AiProvider.NIM_BASE_URL
}
