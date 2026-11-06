package com.anztdree.avi.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * K22 — LOG AKSI AGEN: jejak setiap perkakas yang pernah AVI jalankan.
 * Dilihat pemilik di Pusat Kendali → AGENT → LOG AKSI (transparansi penuh:
 * pemilik tahu persis apa saja yang dilakukan agen di perangkatnya).
 * Tersimpan lokal (filesDir), maksimal 80 entri terbaru.
 */
@Serializable
data class AgentLogItem(
    val waktu: Long,
    val tool: String,
    val label: String,
    val argumen: String = "",
    val ok: Boolean,
    val pesan: String = ""
)

class AgentLogRepository(context: Context) {

    private val file = File(context.filesDir, "avi_agent_log.json")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    companion object {
        private const val MAKS = 80
    }

    @Volatile
    private var cache: List<AgentLogItem> = emptyList()

    suspend fun load(): List<AgentLogItem> = withContext(Dispatchers.IO) {
        try {
            cache = if (file.exists()) {
                json.decodeFromString<List<AgentLogItem>>(file.readText())
            } else emptyList()
        } catch (_: Exception) {
            cache = emptyList()
        }
        cache
    }

    val current: List<AgentLogItem> get() = cache

    suspend fun add(item: AgentLogItem) = withContext(Dispatchers.IO) {
        cache = (cache + item).takeLast(MAKS)
        try {
            file.writeText(json.encodeToString(cache))
        } catch (_: Exception) { }
        cache
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        cache = emptyList()
        try { file.delete() } catch (_: Exception) { }
        cache
    }
}
