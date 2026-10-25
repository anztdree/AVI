package com.anztdree.avi.ai

import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * X1 — Live Voice Mode: obrolan suara↔suara real-time via WebSocket
 * BidiGenerateContent (model audio live Google, diambil dari daftar model).
 * Protokol diparse toleran (array JSON atau objek) agar tahan perubahan API.
 * Bila gagal, ViewModel otomatis fallback ke mode suara standar.
 */
class LiveClient(
    private val apiKey: String,
    private val model: String,
    private val systemPrompt: String,
    private val listener: Listener
) {

    interface Listener {
        fun onSetupComplete()
        fun onAudioPcm(pcm: ByteArray)          // PCM 16-bit mono 24 kHz
        fun onTranscript(text: String)          // teks jawaban model (bila dikirim)
        fun onInterrupted()                     // pemilik memotong bicara AVI
        fun onTurnComplete()
        fun onError(message: String)
        fun onClosed()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var ws: WebSocket? = null

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket: tanpa timeout baca
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    fun connect() {
        val request = Request.Builder()
            .url("wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=${apiKey.trim()}")
            .build()
        ws = client.newWebSocket(request, socketListener)
    }

    fun sendAudioPcm(pcm16k: ByteArray) {
        val b64 = Base64.encodeToString(pcm16k, Base64.NO_WRAP)
        send(
            listOf(
                JsonObject(
                    mapOf(
                        "realtimeInput" to JsonObject(
                            mapOf(
                                "mediaChunks" to JsonArray(
                                    listOf(
                                        JsonObject(
                                            mapOf(
                                                "mimeType" to kotlinx.serialization.json.JsonPrimitive("audio/pcm;rate=16000"),
                                                "data" to kotlinx.serialization.json.JsonPrimitive(b64)
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    fun close() {
        try { ws?.close(1000, "selesai") } catch (_: Exception) {}
        ws = null
        scope.cancel()
    }

    // ===== Protokol =====

    private fun sendSetup() {
        val setup = JsonObject(
            mapOf(
                "setup" to JsonObject(
                    mapOf(
                        "model" to kotlinx.serialization.json.JsonPrimitive(model),
                        "generationConfig" to JsonObject(
                            mapOf(
                                "responseModalities" to JsonArray(
                                    listOf(kotlinx.serialization.json.JsonPrimitive("AUDIO"))
                                ),
                                "temperature" to kotlinx.serialization.json.JsonPrimitive(0.7)
                            )
                        ),
                        "systemInstruction" to JsonObject(
                            mapOf(
                                "parts" to JsonArray(
                                    listOf(JsonObject(mapOf("text" to kotlinx.serialization.json.JsonPrimitive(systemPrompt))))
                                )
                            )
                        )
                    )
                )
            )
        )
        send(listOf(setup))
    }

    private fun send(objs: List<JsonElement>) {
        val payload = jsonParser.encodeToString(JsonArray.serializer(), JsonArray(objs))
        ws?.send(payload)
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            sendSetup()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            scope.launch { handle(text) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            listener.onError("Live terputus: ${t.message ?: "jaringan bermasalah"}")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            listener.onClosed()
        }
    }

    private suspend fun handle(raw: String) {
        try {
            val element = jsonParser.parseToJsonElement(raw)
            val objects: List<JsonObject> = when (element) {
                is JsonArray -> element.jsonArray.mapNotNull { it as? JsonObject }
                is JsonObject -> listOf(element)
                else -> emptyList()
            }
            for (obj in objects) {
                if (obj.containsKey("setupComplete")) listener.onSetupComplete()
                obj["serverContent"]?.let { sc ->
                    val server = sc as? JsonObject ?: return@let
                    (server["interrupted"] as? kotlinx.serialization.json.JsonPrimitive)?.content?.let {
                        if (it == "true") listener.onInterrupted()
                    }
                    (server["turnComplete"] as? kotlinx.serialization.json.JsonPrimitive)?.content?.let {
                        if (it == "true") listener.onTurnComplete()
                    }
                    (server["modelTurn"] as? JsonObject)?.let { turn ->
                        (turn["parts"] as? JsonArray)?.forEach { p ->
                            val part = p as? JsonObject ?: return@forEach
                            (part["text"] as? kotlinx.serialization.json.JsonPrimitive)?.content?.let {
                                if (it.isNotBlank()) listener.onTranscript(it)
                            }
                            (part["inlineData"] as? JsonObject)?.let { blob ->
                                val b64 = (blob["data"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                                if (!b64.isNullOrEmpty()) {
                                    val pcm = Base64.decode(b64, Base64.NO_WRAP)
                                    if (pcm.isNotEmpty()) listener.onAudioPcm(pcm)
                                }
                            }
                        }
                    }
                }
                obj["error"]?.let { err ->
                    val msg = (err as? JsonObject)?.get("message")?.jsonPrimitive?.content
                        ?: (err.jsonPrimitive?.content ?: "error tidak diketahui")
                    listener.onError("Live: $msg")
                }
            }
        } catch (_: Exception) {
            // pesan rusak: lewati, jangan matikan sesi
        }
    }
}
