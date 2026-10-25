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

/** Bersihkan teks untuk TTS (buang simbol yang tidak perlu dibacakan) */
fun cleanForSpeech(text: String): String {
    return text
        .replace(Regex("\\[GAMBAR:.*?]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
        .replace(Regex("[*_#`~>\\[\\]]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
