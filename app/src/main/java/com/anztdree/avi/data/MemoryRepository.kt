package com.anztdree.avi.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
data class MemoryItem(
    val id: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Memori jangka panjang AVI — fakta tentang pemilik yang tersimpan permanen
 * dan otomatis disuntikkan ke kepribadian AVI di setiap percakapan.
 */
class MemoryRepository(context: Context) {

    private val file = File(context.filesDir, "avi_memory.json")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = false }

    companion object {
        private const val MAX_MEMORY = 100
    }

    @Volatile
    private var cache: List<MemoryItem> = emptyList()

    suspend fun load(): List<MemoryItem> = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) {
                cache = json.decodeFromString<List<MemoryItem>>(file.readText())
            } else {
                cache = emptyList()
            }
        } catch (_: Exception) {
            cache = emptyList()
        }
        cache
    }

    val current: List<MemoryItem> get() = cache

    suspend fun add(text: String): MemoryItem = withContext(Dispatchers.IO) {
        val item = MemoryItem(
            id = UUID.randomUUID().toString(),
            text = text.trim().take(240)
        )
        cache = (cache + item).takeLast(MAX_MEMORY)
        persist()
        item
    }

    suspend fun remove(id: String) = withContext(Dispatchers.IO) {
        cache = cache.filterNot { it.id == id }
        persist()
    }

    /** Teks gabungan untuk disuntikkan ke prompt */
    fun joinedText(): String =
        cache.joinToString("\n") { "- ${it.text}" }

    private fun persist() {
        try {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(cache))
        } catch (_: Exception) {
        }
    }
}
