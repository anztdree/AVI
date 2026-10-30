package com.anztdree.avi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.aviDataStore: DataStore<Preferences> by preferencesDataStore(name = "avi_settings")

enum class AiProvider(val id: String, val label: String) {
    GEMINI("GEMINI", "Google Gemini (Gratis — direkomendasikan)"),
    OPENAI_COMPAT("OPENAI_COMPAT", "API Kompatibel OpenAI (OpenRouter, Groq, dll)");

    companion object {
        fun from(id: String): AiProvider =
            entries.firstOrNull { it.id == id } ?: GEMINI
    }
}

/**
 * Seluruh pengaturan AVI. Field memoryText / recap HANYA runtime
 * (diisi ViewModel sebelum memanggil AI, tidak pernah disimpan).
 */
data class AviSettings(
    val provider: AiProvider = AiProvider.GEMINI,
    val geminiKey: String = "",
    val ocBaseUrl: String = "https://openrouter.ai/api/v1",
    val ocKey: String = "",
    val ocModel: String = "meta-llama/llama-3.3-70b-instruct:free",
    // model dinamis dari Google (TIDAK BOLEH hardcode)
    val geminiModel: String = "",
    // suara
    val autoListen: Boolean = true,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val cloudTtsEnabled: Boolean = false,
    val cloudTtsModel: String = "",
    val cloudVoice: String = "Kore",
    // tampilan & efek HUD Mark II
    val themeMark: String = "MK1",
    val bootEnabled: Boolean = true,
    val parallaxEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val diagnosticsOrbit: Boolean = true,
    // K19 — bingkai HUD kelas Jarvis (bracket sudut + rel samping + sapuan hologram)
    val hudFrame: Boolean = true,
    val powerSave: Boolean = false,
    // kepribadian & mode
    val style: String = "santai",
    val userName: String = "Pak",
    // runtime-only
    val memoryText: String = "",
    val recap: String = ""
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val provider = stringPreferencesKey("provider")
        val geminiKey = stringPreferencesKey("gemini_key")
        val ocBaseUrl = stringPreferencesKey("oc_base_url")
        val ocKey = stringPreferencesKey("oc_key")
        val ocModel = stringPreferencesKey("oc_model")
        val geminiModel = stringPreferencesKey("gemini_model")
        val autoListen = booleanPreferencesKey("auto_listen")
        val speechRate = floatPreferencesKey("speech_rate")
        val pitch = floatPreferencesKey("pitch")
        val cloudTtsEnabled = booleanPreferencesKey("cloud_tts_enabled")
        val cloudTtsModel = stringPreferencesKey("cloud_tts_model")
        val cloudVoice = stringPreferencesKey("cloud_voice")
        val themeMark = stringPreferencesKey("theme_mark")
        val bootEnabled = booleanPreferencesKey("boot_enabled")
        val parallaxEnabled = booleanPreferencesKey("parallax_enabled")
        val hapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val diagnosticsOrbit = booleanPreferencesKey("diagnostics_orbit")
        val hudFrame = booleanPreferencesKey("hud_frame")
        val powerSave = booleanPreferencesKey("power_save")
        val style = stringPreferencesKey("style")
        val userName = stringPreferencesKey("user_name")
        val usageDate = stringPreferencesKey("usage_date")
        val usageCount = intPreferencesKey("usage_count")
        val lastBriefDate = stringPreferencesKey("last_brief_date")
        val lastGreetingAt = longPreferencesKey("last_greeting_at")
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun load(p: Preferences): AviSettings = AviSettings(
        provider = AiProvider.from(p[Keys.provider] ?: AiProvider.GEMINI.id),
        geminiKey = SecureStore.decrypt(p[Keys.geminiKey] ?: "") ?: (p[Keys.geminiKey] ?: ""),
        ocBaseUrl = p[Keys.ocBaseUrl] ?: "https://openrouter.ai/api/v1",
        ocKey = SecureStore.decrypt(p[Keys.ocKey] ?: "") ?: (p[Keys.ocKey] ?: ""),
        ocModel = p[Keys.ocModel] ?: "meta-llama/llama-3.3-70b-instruct:free",
        geminiModel = p[Keys.geminiModel] ?: "",
        autoListen = p[Keys.autoListen] ?: true,
        speechRate = p[Keys.speechRate] ?: 1.0f,
        pitch = p[Keys.pitch] ?: 1.0f,
        cloudTtsEnabled = p[Keys.cloudTtsEnabled] ?: false,
        cloudTtsModel = p[Keys.cloudTtsModel] ?: "",
        cloudVoice = p[Keys.cloudVoice] ?: "Kore",
        themeMark = p[Keys.themeMark] ?: "MK1",
        bootEnabled = p[Keys.bootEnabled] ?: true,
        parallaxEnabled = p[Keys.parallaxEnabled] ?: true,
        hapticsEnabled = p[Keys.hapticsEnabled] ?: true,
        diagnosticsOrbit = p[Keys.diagnosticsOrbit] ?: true,
        hudFrame = p[Keys.hudFrame] ?: true,
        powerSave = p[Keys.powerSave] ?: false,
        style = p[Keys.style] ?: "santai",
        userName = p[Keys.userName] ?: "Pak"
    )

    val settings: Flow<AviSettings> = context.aviDataStore.data.map { load(it) }

    suspend fun save(transform: (AviSettings) -> AviSettings) {
        context.aviDataStore.edit { p ->
            val next = transform(load(p))
            p[Keys.provider] = next.provider.id
            p[Keys.geminiKey] = SecureStore.encrypt(next.geminiKey) ?: next.geminiKey
            p[Keys.ocBaseUrl] = next.ocBaseUrl
            p[Keys.ocKey] = SecureStore.encrypt(next.ocKey) ?: next.ocKey
            p[Keys.ocModel] = next.ocModel
            p[Keys.geminiModel] = next.geminiModel
            p[Keys.autoListen] = next.autoListen
            p[Keys.speechRate] = next.speechRate
            p[Keys.pitch] = next.pitch
            p[Keys.cloudTtsEnabled] = next.cloudTtsEnabled
            p[Keys.cloudTtsModel] = next.cloudTtsModel
            p[Keys.cloudVoice] = next.cloudVoice
            p[Keys.themeMark] = next.themeMark
            p[Keys.bootEnabled] = next.bootEnabled
            p[Keys.parallaxEnabled] = next.parallaxEnabled
            p[Keys.hapticsEnabled] = next.hapticsEnabled
            p[Keys.diagnosticsOrbit] = next.diagnosticsOrbit
            p[Keys.hudFrame] = next.hudFrame
            p[Keys.powerSave] = next.powerSave
            p[Keys.style] = next.style
            p[Keys.userName] = next.userName.ifBlank { "Pak" }
        }
    }

    // ===== Pemakaian API per hari =====

    val usageToday: Flow<Int> = context.aviDataStore.data.map { p ->
        if (p[Keys.usageDate] == today()) p[Keys.usageCount] ?: 0 else 0
    }

    suspend fun bumpUsage(): Int {
        var result = 0
        context.aviDataStore.edit { p ->
            val count = if (p[Keys.usageDate] == today()) p[Keys.usageCount] ?: 0 else 0
            result = count + 1
            p[Keys.usageDate] = today()
            p[Keys.usageCount] = result
        }
        return result
    }

    // ===== Briefing harian =====

    suspend fun markBriefed() {
        context.aviDataStore.edit { it[Keys.lastBriefDate] = today() }
    }

    suspend fun isBriefedToday(): Boolean =
        context.aviDataStore.data.first()[Keys.lastBriefDate] == today()

    // ===== Greeting (jangan berulang terlalu sering) =====

    suspend fun greetingDue(): Boolean {
        val last = context.aviDataStore.data.first()[Keys.lastGreetingAt] ?: 0L
        return System.currentTimeMillis() - last > 5 * 60 * 1000L
    }

    suspend fun markGreeted() {
        context.aviDataStore.edit { it[Keys.lastGreetingAt] = System.currentTimeMillis() }
    }
}
