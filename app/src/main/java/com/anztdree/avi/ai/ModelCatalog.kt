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

/** K11 — satu langkah diagnosa (ListModels / ping model) */
data class DiagnoseStep(
    val label: String,
    val ok: Boolean,
    val detail: String
)

/** K11 — hasil diagnosa koneksi penuh + vonak + panduan perbaikan */
data class DiagnoseResult(
    val running: Boolean = false,
    val steps: List<DiagnoseStep> = emptyList(),
    val verdict: String = "",
    val verdictOk: Boolean = false,
    val fixGuide: List<String> = emptyList()
)

object ModelCatalog {

    suspend fun fetch(apiKey: String): Result<List<GeminiModelInfo>> = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty()) {
            return@withContext Result.failure(Exception("API key belum diisi pak."))
        }
        try {
            val request = okhttpAuth(Request.Builder(), key)
                .url("https://generativelanguage.googleapis.com/v1beta/models?pageSize=1000")
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

    /**
     * Urutan tampil — K11: SADAR GENERASI. Google berganti era (2.x → 3.x → …)
     * tanpa peringatan; model lama bisa ditutup untuk key baru. Jadi urutan
     * DITENTUKAN dari nomor generasi yang DI-PARSE dari nama model (bukan hardcode):
     * generasi tertinggi dulu → flash → flash-lite → pro → lainnya.
     * Preview/eksperimental selalu di paling bawah.
     */
    private val genNumber = { name: String ->
        Regex("gemini-(\\d+)").find(name.lowercase())?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    /**
     * Alias "-latest" (contoh: gemini-flash-latest) selalu menunjuk model generasi
     * berjalan yang TERBUKA untuk key baru — terbukti lewat uji langsung pemilik.
     * Alias ini diungkit ke puncak daftar supaya jadi pilihan default teraman.
     */
    private val effectiveGen = { info: GeminiModelInfo ->
        if (info.shortName.contains("-latest")) 99 else genNumber(info.shortName)
    }

    private val modelOrder = compareBy<GeminiModelInfo>(
        { if (it.isPreview) 1 else 0 },
        { -effectiveGen(it) },                        // generasi terbaru dulu; alias -latest paling atas
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
                val request = okhttpAuth(Request.Builder(), key)
                    .url("https://generativelanguage.googleapis.com/v1beta/$model:generateContent")
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
                code == 404 && err.lowercase().contains("no longer available to new users") -> {
                    val saran = Regex("use\\s+(models/)?([A-Za-z0-9._-]+)")
                        .find(err)?.groupValues?.get(2)
                    ModelTestResult(
                        testing = false,
                        ok = false,
                        message = if (saran != null)
                            "Ditutup Google untuk key baru (404) — pakai penggantinya: $saran"
                        else
                            "Ditutup Google untuk key baru (404) — pilih model generasi terbaru."
                    )
                }
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
    /**
     * K11 — DIAGNOSA KONEKSI: membedakan "salah model" vs "project diblokir Google"
     * dalam ±15 detik, TANPA menebak-nebak. Langkah: (1) ListModels, (2) ping 1
     * model. Vonak + panduan disiapkan untuk tiap jenis kegagalan yang dikenal.
     */
    suspend fun diagnose(apiKey: String): DiagnoseResult = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty()) {
            return@withContext DiagnoseResult(
                verdict = "API key kosong",
                verdictOk = false,
                fixGuide = listOf("Buka kolom API Key Gemini di atas, tempel key dari aistudio.google.com/apikey.")
            )
        }
        val steps = mutableListOf<DiagnoseStep>()

        // ---- LANGKAH 1: ListModels (bukti kunci diterima Google) ----
        var modelCount = 0
        var firstChatModel: String? = null
        try {
            val request = okhttpAuth(Request.Builder(), key)
                .url("https://generativelanguage.googleapis.com/v1beta/models?pageSize=1000")
                .get().build()
            Http.testClient.newCall(request).execute().use { resp ->
                val body = try { resp.body?.string() } catch (_: Exception) { null }
                val err = extractError(body, "HTTP ${resp.code}")
                if (resp.isSuccessful) {
                    val parsed = try { jsonParser.decodeFromString<ModelListResponse>(body ?: "{}") } catch (_: Exception) { null }
                    modelCount = parsed?.models?.size ?: 0
                    firstChatModel = parsed?.models
                        ?.filter { it.supportedGenerationMethods?.contains("generateContent") == true }
                        ?.mapNotNull { it.name }?.firstOrNull()
                    steps.add(DiagnoseStep("KUNCI DITERIMA GOOGLE", true, "ListModels OK — $modelCount model terbaca"))
                } else {
                    steps.add(DiagnoseStep("KUNCI DITERIMA GOOGLE", false, "HTTP ${resp.code} — $err"))
                    return@withContext buildVerdict(steps, resp.code, err, listFailed = true)
                }
            }
        } catch (e: Exception) {
            steps.add(DiagnoseStep("KUNCI DITERIMA GOOGLE", false, "Koneksi gagal: ${e.message ?: "unknown"}"))
            return@withContext DiagnoseResult(
                steps = steps,
                verdict = "TIDAK BISA MENJANGKAU GOOGLE",
                verdictOk = false,
                fixGuide = listOf(
                    "Cek internet HP (buka situs apa saja di browser).",
                    "Matikan VPN/proxy/private DNS lalu ulangi diagnosa.",
                    "Ganti jaringan: WiFi ↔ data seluler."
                )
            )
        }

        // ---- LANGKAH 2: ping 1 model (bukti hak pakai project) ----
        val target = firstChatModel ?: "models/gemini-flash-latest"
        try {
            val body = jsonParser.encodeToString(
                GeminiRequest(
                    contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = "ping")))),
                    generationConfig = GeminiGenerationConfig(maxOutputTokens = 8)
                )
            )
            val request = okhttpAuth(Request.Builder(), key)
                .url("https://generativelanguage.googleapis.com/v1beta/$target:generateContent")
                .header("Content-Type", "application/json")
                .post(body.toRequestBody(Http.jsonMedia))
                .build()
            Http.testClient.newCall(request).execute().use { resp ->
                val errBody = if (resp.isSuccessful) null else
                    try { resp.body?.string() } catch (_: Exception) { null }
                val err = extractError(errBody, "HTTP ${resp.code}")
                steps.add(
                    DiagnoseStep(
                        "HAK PAKAI MODEL (${target.removePrefix("models/")})",
                        resp.isSuccessful,
                        if (resp.isSuccessful) "Panggilan percakapan BERHASIL — otak siap dipakai"
                        else "HTTP ${resp.code} — $err"
                    )
                )
                if (resp.isSuccessful) {
                    DiagnoseResult(
                        steps = steps,
                        verdict = "KONEKSI SEHAT — TIDAK ADA BLOKIR",
                        verdictOk = true,
                        fixGuide = listOf(
                            "Kalau obrolan masih gagal, jalankan UJI MODEL pada tiap model (badge ✓ SIAP).",
                            "Hindari model berbadge UJI COBA — kuotanya paling sering habis."
                        )
                    )
                } else {
                    buildVerdict(steps, resp.code, err, listFailed = false)
                }
            }
        } catch (e: Exception) {
            steps.add(DiagnoseStep("HAK PAKAI MODEL", false, "Koneksi gagal: ${e.message ?: "unknown"}"))
            DiagnoseResult(
                steps = steps,
                verdict = "JARINGAN TERPUTUS SAAT MEMANGGIL MODEL",
                verdictOk = false,
                fixGuide = listOf(
                    "Daftar model berhasil tapi panggilan model gagal — koneksi tidak stabil.",
                    "Coba lagi dalam beberapa detik, atau ganti jaringan."
                )
            )
        }
    }

    /** Vonak diagnosa berdasarkan jenis penolakan Google + panduan JALAN KELUAR */
    private fun buildVerdict(steps: MutableList<DiagnoseStep>, code: Int, err: String, listFailed: Boolean): DiagnoseResult {
        val e = err.lowercase()
        val stage = if (listFailed) "DAFTAR MODEL" else "MODEL"
        return when {
            code == 403 && e.contains("free tier is not available") -> DiagnoseResult(
                steps = steps,
                verdict = "PROJECT KEY TIDAK PUNYA AKSES GRATIS (403) — INI PENYEBAB SEMUA MODEL GAGAL",
                verdictOk = false,
                fixGuide = listOf(
                    "PILIHAN 1 — Aktifkan penagihan: buka https://aistudio.google.com/billing di browser, tautkan akun penagihan Google ke project ini, lalu jalankan ulang DIAGNOSA.",
                    "PILIHAN 2 — Buat key baru: buka https://aistudio.google.com/apikey, tombol 'Create API key', pilih 'Create new project' supaya dapat project baru, tempel key barunya ke kolom API Key, lalu DIAGNOSA ulang.",
                    "PILIHAN 3 — Aktifkan API-nya manual: buka https://console.developers.google.com/apis/api/generativelanguage.googleapis.com lalu tekan ENABLE.",
                    "Setelah salah satu selesai, tekan MUAT DAFTAR MODEL lalu DIAGNOSA ulang."
                )
            )
            code == 400 && e.contains("location is not supported") -> DiagnoseResult(
                steps = steps,
                verdict = "LOKASI JARINGAN DITOLAK GOOGLE (400)",
                verdictOk = false,
                fixGuide = listOf(
                    "Matikan VPN/proxy dan private DNS.",
                    "Ganti jaringan: WiFi ↔ data seluler, lalu ulangi diagnosa.",
                    "Hindari jam-jam gangguan Google (coba lagi 5–10 menit)."
                )
            )
            code == 403 -> DiagnoseResult(
                steps = steps,
                verdict = "AKSES DITOLAK GOOGLE (403)",
                verdictOk = false,
                fixGuide = listOf(
                    "Key mungkin salah tempel/revoke — buat key baru di https://aistudio.google.com/apikey.",
                    "Pastikan tidak ada spasi di awal/akhir kolom API Key.",
                    "Detail Google: $err"
                )
            )
            code == 404 -> DiagnoseResult(
                steps = steps,
                verdict = "ENDPOINT/MODEL TIDAK DITEMUKAN (404)",
                verdictOk = false,
                fixGuide = listOf(
                    "Muat ulang daftar model lalu pilih model generasi terbaru (nomor paling besar).",
                    "Model lama sering ditutup untuk key baru — percayakan pada urutan daftar.",
                    "Detail Google: $err"
                )
            )
            else -> DiagnoseResult(
                steps = steps,
                verdict = "GOOGLE MENOLAK DENGAN HTTP $code ($stage)",
                verdictOk = false,
                fixGuide = listOf(
                    "Ulangi diagnosa beberapa saat lagi.",
                    "Kalau terus terjadi, cek status layanan: https://status.cloud.google.com",
                    "Detail Google: $err"
                )
            )
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
