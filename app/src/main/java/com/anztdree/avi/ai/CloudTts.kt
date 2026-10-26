package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import android.util.Base64
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * X2 — Suara natural cloud: sintesis suara via model TTS Google (dari daftar model).
 * Mengembalikan PCM 16-bit mono 24 kHz siap diputar PcmPlayer.
 * Fallback ke TTS Android ditangani pemanggil.
 */
object CloudTts {

    private const val SAMPLE_RATE = 24000

    data class Audio(val pcm: ByteArray, val sampleRate: Int)

    suspend fun synthesize(text: String, settings: AviSettings): Result<Audio> =
        withContext(Dispatchers.IO) {
            val key = settings.geminiKey.trim()
            val model = settings.cloudTtsModel.trim()
            if (key.isEmpty()) {
                return@withContext Result.failure(Exception("API key belum ada, pak."))
            }
            if (model.isEmpty()) {
                return@withContext Result.failure(Exception("Model suara cloud belum dipilih, pak."))
            }
            try {
                val body = GeminiRequest(
                    contents = listOf(
                        GeminiContent(role = "user", parts = listOf(GeminiPart(text = text)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        responseModalities = listOf("AUDIO"),
                        speechConfig = GeminiSpeechConfig(
                            voiceConfig = GeminiVoiceConfig(
                                prebuiltVoiceConfig = GeminiPrebuiltVoiceConfig(
                                    voiceName = settings.cloudVoice.ifBlank { "Kore" }
                                )
                            )
                        )
                    )
                )
                val request = okhttpAuth(Request.Builder(), key)
                    // K14 — auth lewat HEADER X-goog-api-key (key "AQ." baru tidak
                    // dijamin lewat ?key= query param)
                    .url("https://generativelanguage.googleapis.com/v1beta/$model:generateContent")
                    .header("Content-Type", "application/json")
                    .post(jsonParser.encodeToString(body).toRequestBody(Http.jsonMedia))
                    .build()

                Http.client.newCall(request).execute().use { response ->
                    val raw = try { response.body?.string() } catch (_: Exception) { null }
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Suara cloud gagal: ${extractError(raw, "HTTP ${response.code}")}")
                        )
                    }
                    val chunk = jsonParser.decodeFromString<GeminiChunk>(raw ?: "{}")
                    val b64 = chunk.candidates.firstOrNull()
                        ?.content?.parts
                        ?.firstOrNull { it.inlineData?.data != null }
                        ?.inlineData?.data
                    if (b64.isNullOrEmpty()) {
                        return@withContext Result.failure(Exception("Model suara tidak mengembalikan audio."))
                    }
                    val pcm = Base64.decode(b64, Base64.NO_WRAP)
                    if (pcm.isEmpty()) {
                        return@withContext Result.failure(Exception("Audio kosong dari model suara."))
                    }
                    Result.success(Audio(pcm, SAMPLE_RATE))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Suara cloud gagal: ${e.message ?: "unknown"}"))
            }
        }
}
