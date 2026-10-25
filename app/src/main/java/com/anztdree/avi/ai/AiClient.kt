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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Klien untuk uji model — cepat & tegas, jangan biarkan pemilik menunggu lama */
    val testClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .callTimeout(35, TimeUnit.SECONDS)
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
 * Pesan error berbahasa Indonesia untuk pemilik.
 * Khusus 429 (kuota): sertakan retryDelay dari Google bila ada.
 * Khusus 404: model mungkin sudah dipensiunkan Google / butuh versi API lain.
 */
fun humanizeError(httpCode: Int, raw: String, model: String = ""): String {
    val short = model.removePrefix("models/")
    return when (httpCode) {
        429 -> {
            val retry = Regex("retryDelay\"?\\s*:\\s*\"(\\d+)s")
                .find(raw)?.groupValues?.takeIf { it.isNotEmpty() }?.get(1)
            val tunggu = if (retry != null) "Tunggu ±$retry detik lalu coba lagi, pak" else
                "Tunggu sebentar lalu coba lagi, pak"
            "Kuota Gemini habis untuk saat ini (HTTP 429). $tunggu. " +
                "Kalau sering terjadi: kurangi mode riset/pencarian web, atau cek kuota gratis di AI Studio."
        }
        404 -> "Model '$short' tidak tersedia (HTTP 404). Kemungkinan model ini sudah " +
            "dipensiunkan Google atau butuh versi API lain — pilih model lain di Pusat Kendali ya pak. " +
            "Detail: $raw"
        400 -> "Permintaan ditolak Gemini (HTTP 400): $raw — coba pilih model lain di Pusat Kendali, pak."
        401, 403 -> "API key ditolak (HTTP $httpCode): $raw — pastikan key dari aistudio.google.com/apikey masih aktif, pak."
        else -> "Gemini (HTTP $httpCode): $raw"
    }
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
