package com.anztdree.avi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.aviDataStore: DataStore<Preferences> by preferencesDataStore(name = "avi_settings")

enum class AiProvider(val id: String, val label: String) {
    GEMINI("GEMINI", "Google Gemini (Gratis — direkomendasikan)"),
    OPENAI_COMPAT("OPENAI_COMPAT", "API Kompatibel OpenAI (OpenRouter, Groq, dll)");

    companion object {
        fun from(id: String): AiProvider =
            entries.firstOrNull { it.id == id } ?: GEMINI
    }
}

data class AviSettings(
    val provider: AiProvider = AiProvider.GEMINI,
    val geminiKey: String = "",
    val ocBaseUrl: String = "https://openrouter.ai/api/v1",
    val ocKey: String = "",
    val ocModel: String = "meta-llama/llama-3.3-70b-instruct:free",
    val autoSpeak: Boolean = true,
    val autoListen: Boolean = true,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val webSearch: Boolean = true,
    val userName: String = "Pak"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val provider = stringPreferencesKey("provider")
        val geminiKey = stringPreferencesKey("gemini_key")
        val ocBaseUrl = stringPreferencesKey("oc_base_url")
        val ocKey = stringPreferencesKey("oc_key")
        val ocModel = stringPreferencesKey("oc_model")
        val autoSpeak = booleanPreferencesKey("auto_speak")
        val autoListen = booleanPreferencesKey("auto_listen")
        val speechRate = floatPreferencesKey("speech_rate")
        val pitch = floatPreferencesKey("pitch")
        val webSearch = booleanPreferencesKey("web_search")
        val userName = stringPreferencesKey("user_name")
    }

    val settings: Flow<AviSettings> = context.aviDataStore.data.map { p ->
        AviSettings(
            provider = AiProvider.from(p[Keys.provider] ?: AiProvider.GEMINI.id),
            geminiKey = p[Keys.geminiKey] ?: "",
            ocBaseUrl = p[Keys.ocBaseUrl] ?: "https://openrouter.ai/api/v1",
            ocKey = p[Keys.ocKey] ?: "",
            ocModel = p[Keys.ocModel] ?: "meta-llama/llama-3.3-70b-instruct:free",
            autoSpeak = p[Keys.autoSpeak] ?: true,
            autoListen = p[Keys.autoListen] ?: true,
            speechRate = p[Keys.speechRate] ?: 1.0f,
            pitch = p[Keys.pitch] ?: 1.0f,
            webSearch = p[Keys.webSearch] ?: true,
            userName = p[Keys.userName] ?: "Pak"
        )
    }

    suspend fun save(transform: (AviSettings) -> AviSettings) {
        context.aviDataStore.edit { p ->
            val current = AviSettings(
                provider = AiProvider.from(p[Keys.provider] ?: AiProvider.GEMINI.id),
                geminiKey = p[Keys.geminiKey] ?: "",
                ocBaseUrl = p[Keys.ocBaseUrl] ?: "https://openrouter.ai/api/v1",
                ocKey = p[Keys.ocKey] ?: "",
                ocModel = p[Keys.ocModel] ?: "meta-llama/llama-3.3-70b-instruct:free",
                autoSpeak = p[Keys.autoSpeak] ?: true,
                autoListen = p[Keys.autoListen] ?: true,
                speechRate = p[Keys.speechRate] ?: 1.0f,
                pitch = p[Keys.pitch] ?: 1.0f,
                webSearch = p[Keys.webSearch] ?: true,
                userName = p[Keys.userName] ?: "Pak"
            )
            val next = transform(current)
            p[Keys.provider] = next.provider.id
            p[Keys.geminiKey] = next.geminiKey
            p[Keys.ocBaseUrl] = next.ocBaseUrl
            p[Keys.ocKey] = next.ocKey
            p[Keys.ocModel] = next.ocModel
            p[Keys.autoSpeak] = next.autoSpeak
            p[Keys.autoListen] = next.autoListen
            p[Keys.speechRate] = next.speechRate
            p[Keys.pitch] = next.pitch
            p[Keys.webSearch] = next.webSearch
            p[Keys.userName] = next.userName
        }
    }
}
