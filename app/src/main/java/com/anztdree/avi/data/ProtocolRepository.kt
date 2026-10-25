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
data class Protocol(
    val id: String,
    val name: String,
    val phrase: String,          // kata pemicu lisan, mis. "protokol kerja"
    val steps: List<String>      // perintah berurutan yang dijalankan AVI
)

/**
 * Protokol AVI — makro ala "House Party Protocol": satu frase memicu
 * rangkaian perintah berurutan. Bisa ditambah/dihapus pemilik.
 */
class ProtocolRepository(context: Context) {

    private val file = File(context.filesDir, "avi_protocols.json")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    companion object {
        fun defaults(): List<Protocol> = listOf(
            Protocol(
                id = "proto-kerja",
                name = "Protokol Kerja",
                phrase = "protokol kerja",
                steps = listOf(
                    "Berikan briefing singkat: cuaca hari ini di kota saya dan 2 berita utama Indonesia hari ini.",
                    "Sampaikan satu kalimat motivasi kerja bergaya AVI."
                )
            ),
            Protocol(
                id = "proto-malam",
                name = "Protokol Malam",
                phrase = "protokol malam",
                steps = listOf(
                    "Rangkum 2 berita terpenting hari ini secara singkat.",
                    "Ucapkan satu kalimat penutup malam yang hangat untuk pemilik."
                )
            )
        )
    }

    @Volatile
    private var cache: List<Protocol> = defaults()

    suspend fun load(): List<Protocol> = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) {
                cache = json.decodeFromString<List<Protocol>>(file.readText())
            }
        } catch (_: Exception) {
            cache = defaults()
        }
        cache
    }

    val current: List<Protocol> get() = cache

    suspend fun save(p: Protocol) = withContext(Dispatchers.IO) {
        val cleaned = p.copy(
            name = p.name.trim().ifBlank { "Protokol" },
            phrase = p.phrase.trim().lowercase(),
            steps = p.steps.map { it.trim() }.filter { it.isNotBlank() }
        )
        cache = if (cache.any { it.id == cleaned.id }) {
            cache.map { if (it.id == cleaned.id) cleaned else it }
        } else {
            cache + cleaned
        }
        persist()
    }

    suspend fun remove(id: String) = withContext(Dispatchers.IO) {
        cache = cache.filterNot { it.id == id }
        persist()
    }

    fun matchPhrase(text: String): Protocol? {
        val t = text.lowercase().trim()
        return cache.firstOrNull { t.contains(it.phrase) }
    }

    private fun persist() {
        try {
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(cache))
        } catch (_: Exception) {
        }
    }
}
