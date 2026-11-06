package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

/** Callback streaming dari klien AI */
interface AiStreamCallback {
    fun onDelta(text: String)
    fun onComplete(fullText: String)
    fun onError(message: String)
}

interface AiClient {
    /** Kirim percakapan, jawaban mengalir via callback */
    suspend fun stream(history: List<ChatMessage>, settings: AviSettings, callback: AiStreamCallback)
}

object AiClientFactory {
    fun create(settings: AviSettings): AiClient = when (settings.provider) {
        com.anztdree.avi.data.AiProvider.GEMINI -> GeminiClient()
        com.anztdree.avi.data.AiProvider.OPENAI_COMPAT -> OpenAiCompatClient()
    }
}

/** Klien HTTP bersama */
object Http {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(180, TimeUnit.SECONDS)   // K11: loading tidak boleh menggantung selamanya
        .build()

    /** Klien untuk uji model — cepat & tegas (20 dtk: ping normal 1–5 dtk; yang
     *  menggantung >20 dtk hampir pasti model yang sedang bermasalah) */
    val testClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    val jsonMedia = "application/json; charset=utf-8".toMediaType()
}

/**
 * Pembaca SSE (Server-Sent Events) bersama.
 * Membaca response baris demi baris, memanggil onEvent untuk tiap payload "data: ...".
 */
suspend fun readSse(
    response: Response,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onEvent: (String) -> Unit
): Unit = withContext(Dispatchers.IO) {
    val source = response.body?.source() ?: return@withContext
    while (coroutineScope.isActive && !source.exhausted()) {
        val line = source.readUtf8Line() ?: break
        if (line.startsWith("data:")) {
            val payload = line.removePrefix("data:").trim()
            if (payload.isNotEmpty() && payload != "[DONE]") {
                try {
                    onEvent(payload)
                } catch (_: Exception) {
                    // lewati chunk rusak, jangan matikan stream
                }
            }
        }
    }
}

/** Ambil pesan error yang enak dibaca dari response gagal */
fun extractError(body: String?, fallback: String): String {
    if (body.isNullOrBlank()) return fallback
    return try {
        val gemini = jsonParser.decodeFromString<GeminiErrorBody>(body)
        gemini.error?.message ?: body.take(300)
    } catch (e: Exception) {
        try {
            val oc = jsonParser.decodeFromString<OcErrorBody>(body)
            oc.error?.message ?: body.take(300)
        } catch (e2: Exception) {
            body.take(300)
        }
    }
}

/**
 * Pesan error berbahasa Indonesia untuk pemilik — K11: AKAR MASALAH DIUNGKAP.
 * Google punya beberapa penolakan tingkat-project yang membuat SEMUA model gagal.
 * Pesan di bawah menerjemahkan tiap kasus beserta JALAN KELUARnya, bukan sekadar
 * "gagal" — supaya pemilik tahu persis apa yang harus dilakukan.
 */
fun humanizeError(httpCode: Int, raw: String, model: String = ""): String {
    val short = model.removePrefix("models/")
    val rawLower = raw.lowercase()
    return when {
        // ==== KASUS 1 — project tanpa free tier (penyebab "gagal semua model") ====
        httpCode == 403 && rawLower.contains("free tier is not available") ->
            "Project API key Pak tidak punya akses GRATIS dari Google (HTTP 403). " +
                "Ini penyebab semua model gagal — bukan salah model. JALAN KELUAR: " +
                "(1) buka Pusat Kendali → DIAGNOSA KONEKSI untuk panduan lengkap, " +
                "(2) aktifkan penagihan di aistudio.google.com/billing, atau " +
                "(3) buat API key baru di aistudio.google.com/apikey (project baru)."

        // ==== KASUS 2 — lokasi jaringan ditolak Google ====
        httpCode == 400 && rawLower.contains("location is not supported") ->
            "Google menolak lokasi jaringan ini (HTTP 400). " +
                "Coba matikan VPN/proxy, ganti WiFi↔seluler, lalu ulangi ya pak."

        // ==== KASUS 3 — model lama ditutup untuk key baru (contoh: 2.5 ditutup, era 3.x) ====
        httpCode == 404 && rawLower.contains("no longer available to new users") -> {
            val saran = Regex("use\\s+(models/)?([A-Za-z0-9._-]+)").find(raw)?.groupValues?.get(2)
            if (saran != null)
                "Model '$short' sudah ditutup Google untuk key baru (HTTP 404). " +
                    "Penggantinya: $saran — pilih model itu di Pusat Kendali ya pak."
            else
                "Model '$short' sudah ditutup Google untuk key baru (HTTP 404) — " +
                    "pilih model generasi terbaru di Pusat Kendali ya pak."
        }

        httpCode == 429 -> {
            val retry = Regex("retryDelay\"?\\s*:\\s*\"(\\d+)s")
                .find(raw)?.groupValues?.takeIf { it.isNotEmpty() }?.get(1)
            val tunggu = if (retry != null) "Tunggu ±$retry detik lalu coba lagi, pak" else
                "Tunggu sebentar lalu coba lagi, pak"
            "Kuota Gemini habis untuk saat ini (HTTP 429). $tunggu. " +
                "Key gratis punya jatah per menit dan per hari — kalau sering terjadi, " +
                "beri jeda antar pertanyaan atau cek kuota di AI Studio, pak."
        }
        httpCode == 404 -> "Model '$short' tidak tersedia (HTTP 404). Kemungkinan model ini sudah " +
            "dipensiunkan Google atau butuh versi API lain — pilih model lain di Pusat Kendali ya pak. " +
            "Detail: $raw"
        httpCode == 500 || httpCode == 503 -> "Model '$short' sedang bermasalah di sisi Google " +
            "(HTTP $httpCode — internal/kelebihan beban). Bukan salah app: ulangi beberapa saat, " +
            "atau pilih model lain di Pusat Kendali ya pak."
        httpCode == 400 -> "Permintaan ditolak Gemini (HTTP 400): $raw — coba pilih model lain di Pusat Kendali, pak."
        httpCode == 401 -> "API key ditolak (HTTP 401): $raw — pastikan key dari aistudio.google.com/apikey masih aktif, pak."
        httpCode == 403 -> "Akses ditolak Google untuk project key ini (HTTP 403): $raw — " +
            "buka Pusat Kendali → DIAGNOSA KONEKSI untuk panduan perbaikan, pak."
        else -> "Gemini (HTTP $httpCode): $raw"
    }
}

/**
 * K11 — klasifikasi error tingkat-project: kalau TRUE, mencoba model lain PERCUMA
 * (semua model pasti kena penolakan yang sama). Dipakai agar app berhenti cepat
 * dan langsung menunjukkan panduan, bukan loading panjang.
 */
fun isProjectLevelBlock(httpCode: Int, raw: String): Boolean {
    val r = raw.lowercase()
    return (httpCode == 403 && r.contains("free tier is not available")) ||
        (httpCode == 400 && r.contains("location is not supported")) ||
        (httpCode == 403 && r.contains("permission_denied"))
}

/**
 * K11 — DETEKSI KEY FORMAT BARU. Key AI Studio terbaru berawalan "AQ." — auth
 * WAJIB lewat header x-goog-api-key (key lama "AIza..." juga didukung header).
 */
fun okhttpAuth(builder: Request.Builder, key: String): Request.Builder {
    builder.header("x-goog-api-key", key.trim())
    return builder
}

/** Bersihkan tag sistem & simbol untuk TTS (jangan dibacakan) */
fun cleanForSpeech(text: String): String {
    return text
        .replace(Regex("\\[(INGAT|BUKA|LUPAKAN|PROTOKOL|GAMBAR)\\s*:.*?]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
        .replace(Regex("\\[/?[^]]*]"), " ")   // sisa tag bersimbol kurung
        .replace(Regex("[*_#`~>]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
