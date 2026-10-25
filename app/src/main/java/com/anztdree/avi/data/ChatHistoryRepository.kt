package com.anztdree.avi.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Penyimpanan riwayat chat berbasis file (ringan, tanpa database).
 * Disimpan di filesDir/avi_history.json
 */
class ChatHistoryRepository(context: Context) {

    private val file = File(context.filesDir, "avi_history.json")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    companion object {
        private const val MAX_SAVED = 300
        private const val MAX_CONTEXT = 20
    }

    suspend fun load(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) emptyList()
            else json.decodeFromString<List<ChatMessage>>(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun save(messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(messages.takeLast(MAX_SAVED)))
        } catch (_: Exception) {
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        try { file.delete() } catch (_: Exception) {}
    }

    /** Ambil potongan pesan untuk dikirim sebagai konteks ke LLM */
    fun contextWindow(messages: List<ChatMessage>): List<ChatMessage> =
        messages.takeLast(MAX_CONTEXT)
}
