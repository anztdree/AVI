package com.anztdree.avi.ai

import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Klien API kompatibel OpenAI (OpenRouter, Groq, OpenAI, dll) dengan streaming SSE.
 */
class OpenAiCompatClient : AiClient {

    override suspend fun stream(history: List<ChatMessage>, settings: AviSettings, callback: AiStreamCallback) {
        withContext(Dispatchers.IO) {
            val key = settings.ocKey.trim()
            if (key.isEmpty()) {
                callback.onError(
                    if (settings.provider == com.anztdree.avi.data.AiProvider.NIM)
                        "Kunci NVIDIA NIM belum diisi pak. Buka Pusat Kendali → OTAK → NVIDIA NIM ya."
                    else "API key belum diisi pak. Buka Pengaturan ya."
                )
                return@withContext
            }
            val base = settings.ocBaseUrl.trim().trimEnd('/')
            val messages = buildList {
                add(OcMessage("system", SystemPrompts.build(settings)))
                history.forEach { msg ->
                    add(OcMessage(if (msg.role == "user") "user" else "assistant", msg.text))
                }
            }
            val body = jsonParser.encodeToString(
                OcRequest(
                    model = settings.ocModel.ifBlank {
                        if (settings.provider == com.anztdree.avi.data.AiProvider.NIM)
                            com.anztdree.avi.ai.NimCatalog.DEFAULT_MODEL
                        else "gpt-4o-mini"
                    },
                    messages = messages
                )
            )
            val request = Request.Builder()
                .url("$base/chat/completions")
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .post(body.toRequestBody(Http.jsonMedia))
                .build()

            Http.client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = try { response.body?.string() } catch (_: Exception) { null }
                    val mentah = extractError(errBody, "HTTP ${response.code}")
                    val pesan =
                        if (settings.provider == com.anztdree.avi.data.AiProvider.NIM)
                            com.anztdree.avi.ai.NimCatalog.nimError(response.code, mentah)
                        else "API: $mentah"
                    callback.onError(pesan)
                    return@withContext
                }

                val sb = StringBuilder()
                readSse(response, CoroutineScope(currentCoroutineContext()), onEvent = { payload ->
                    try {
                        val chunk = jsonParser.decodeFromString<OcChunk>(payload)
                        val text = chunk.choices.firstOrNull()?.delta?.content
                        if (!text.isNullOrEmpty()) {
                            sb.append(text)
                            callback.onDelta(text)
                        }
                    } catch (_: Exception) {
                    }
                })

                val full = sb.toString().trim()
                if (full.isEmpty()) {
                    callback.onError("API tidak mengirim jawaban. Coba ulangi ya pak.")
                } else {
                    callback.onComplete(full)
                }
            }
        }
    }
}
