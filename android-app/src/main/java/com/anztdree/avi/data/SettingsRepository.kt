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
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.aviDataStore: DataStore<Preferences> by preferencesDataStore(name = "avi_settings")

/**
 * P49 — DUA TIPE AI SAJA (umpan Pak: "cuma ada 2 tipe ai .. Gemini dan NVDIA NIM"):
 *  - GEMINI : otak utama gratis (aistudio.google.com/apikey)
 *  - NIM    : NVIDIA NIM (build.nvidia.com) — API kompatibel OpenAI di
 *             https://integrate.api.nvidia.com/v1 — kunci nvapi-…
 * ID lama "OPENAI_COMPAT" otomatis dimigrasikan ke NIM supaya setelan lama
 * tidak rusak, dan pilihan model TETAP tersimpan per-tipe (ganti bolak-balik
 * Gemini ↔ NIM tidak menggugurkan pilihan otak masing-masing).
 */
enum class AiProvider(val id: String, val label: String) {
    GEMINI("GEMINI", "Google Gemini"),
    NIM("NIM", "NVIDIA NIM");

    companion object {
        const val NIM_BASE_URL = "https://integrate.api.nvidia.com/v1"
        fun from(id: String): AiProvider = when (id) {
            "NIM", "OPENAI_COMPAT" -> NIM   // OPENAI_COMPAT lama → NIM
            else -> GEMINI
        }
    }
}

/**
 * Seluruh pengaturan AVI. Field memoryText / recap HANYA runtime
 * (diisi ViewModel sebelum memanggil AI, tidak pernah disimpan).
 */
data class AviSettings(
    val provider: AiProvider = AiProvider.GEMINI,
    val geminiKey: String = "",
    // P48 — KUNCI CADANGAN GEMINI (satu string multi-baris, terenkripsi):
    // kunci ke-2, ke-3, dst. Kalau kuota kunci utama habis (429) atau kunci
    // ditolak (403 / API key invalid), AVI otomatis lanjut ke kunci berikutnya
    // tanpa pemilik melakukan apa-apa.
    val geminiKeysExtra: String = "",
    // P49 — otak NVIDIA NIM (sebelumnya "OpenAI compat" generik)
    val ocBaseUrl: String = AiProvider.NIM_BASE_URL,
    val ocKey: String = "",
    val ocModel: String = "",   // kosong = AVI memilih sendiri dari daftar NIM
    // model dinamis dari Google (TIDAK BOLEH hardcode)
    val geminiModel: String = "",
    // suara
    val autoListen: Boolean = true,
    // P44 — VOKAL JAWABAN: saklar nyata (di preview: #tgVoice) — kalau mati,
    // jawaban tetap tampil di chat tapi TIDAK dibacakan
    val ttsEnabled: Boolean = true,
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
    // K19 — bingkai HUD kelas Jarvis (bracket sudut + rel samping + sapuan hologram)
    val hudFrame: Boolean = true,
    val powerSave: Boolean = false,
    // kepribadian & mode
    val style: String = "santai",
    val userName: String = "Pak",
    // K21 — bahasa pengenalan suara (dengaran) — nyata, disambungkan ke AviStt
    val sttLanguage: String = "id-ID",
    // K22 — mesin AGEN (function calling, bukti DIAG-AGENT v2)
    val agentMode: String = "AUTO",        // AUTO | SELALU | MATI
    val agentAuthority: String = "BAKU",   // AMAN | BAKU | PENUH
    val agentToolsOff: Set<String> = emptySet(),   // perkakas yang dilarang pemilik
    // P50 — WIZARD PERTAMA KALI INSTALL: false = wizard sambutan tampil
    // (selamat datang → nama → izin mikrofon → otak AI → siap). Sekali
    // tuntas, tidak pernah muncul lagi kecuali data dihapus.
    val wizardDone: Boolean = false,
    // runtime-only
    val memoryText: String = "",
    val recap: String = ""
) {
    /**
     * P48 — daftar SEMUA kunci Gemini terpasang: kunci utama dulu, lalu
     * cadangan baris per baris. Sudah dibersihkan (trim), dibuang yang kosong,
     * dan dideduplikasi — urutan tetap sesuai yang pemilik susun.
     */
    fun geminiKeyList(): List<String> =
        (listOf(geminiKey) + geminiKeysExtra.lines())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val provider = stringPreferencesKey("provider")
        val geminiKey = stringPreferencesKey("gemini_key")
        val geminiKeysExtra = stringPreferencesKey("gemini_keys_extra")
        val ocBaseUrl = stringPreferencesKey("oc_base_url")
        val ocKey = stringPreferencesKey("oc_key")
        val ocModel = stringPreferencesKey("oc_model")
        val geminiModel = stringPreferencesKey("gemini_model")
        val autoListen = booleanPreferencesKey("auto_listen")
        val ttsEnabled = booleanPreferencesKey("tts_enabled")
        val speechRate = floatPreferencesKey("speech_rate")
        val pitch = floatPreferencesKey("pitch")
        val cloudTtsEnabled = booleanPreferencesKey("cloud_tts_enabled")
        val cloudTtsModel = stringPreferencesKey("cloud_tts_model")
        val cloudVoice = stringPreferencesKey("cloud_voice")
        val themeMark = stringPreferencesKey("theme_mark")
        val bootEnabled = booleanPreferencesKey("boot_enabled")
        val parallaxEnabled = booleanPreferencesKey("parallax_enabled")
        val hapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val hudFrame = booleanPreferencesKey("hud_frame")
        val powerSave = booleanPreferencesKey("power_save")
        val style = stringPreferencesKey("style")
        val userName = stringPreferencesKey("user_name")
        val sttLanguage = stringPreferencesKey("stt_language")
        val agentMode = stringPreferencesKey("agent_mode")
        val agentAuthority = stringPreferencesKey("agent_authority")
        val agentToolsOff = stringSetPreferencesKey("agent_tools_off")
        val wizardDone = booleanPreferencesKey("wizard_done")
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
        geminiKeysExtra = SecureStore.decrypt(p[Keys.geminiKeysExtra] ?: "") ?: (p[Keys.geminiKeysExtra] ?: ""),
        ocBaseUrl = p[Keys.ocBaseUrl]?.takeIf { it.isNotBlank() }
            // migrasi: bawaan lama openrouter → alamat resmi NVIDIA NIM
            ?.let { if (it.contains("openrouter.ai")) AiProvider.NIM_BASE_URL else it }
            ?: AiProvider.NIM_BASE_URL,
        ocKey = SecureStore.decrypt(p[Keys.ocKey] ?: "") ?: (p[Keys.ocKey] ?: ""),
        ocModel = p[Keys.ocModel] ?: "",
        geminiModel = p[Keys.geminiModel] ?: "",
        autoListen = p[Keys.autoListen] ?: true,
        ttsEnabled = p[Keys.ttsEnabled] ?: true,
        speechRate = p[Keys.speechRate] ?: 1.0f,
        pitch = p[Keys.pitch] ?: 1.0f,
        cloudTtsEnabled = p[Keys.cloudTtsEnabled] ?: false,
        cloudTtsModel = p[Keys.cloudTtsModel] ?: "",
        cloudVoice = p[Keys.cloudVoice] ?: "Kore",
        themeMark = p[Keys.themeMark] ?: "MK1",
        bootEnabled = p[Keys.bootEnabled] ?: true,
        parallaxEnabled = p[Keys.parallaxEnabled] ?: true,
        hapticsEnabled = p[Keys.hapticsEnabled] ?: true,
        hudFrame = p[Keys.hudFrame] ?: true,
        powerSave = p[Keys.powerSave] ?: false,
        style = p[Keys.style] ?: "santai",
        userName = p[Keys.userName] ?: "Pak",
        sttLanguage = p[Keys.sttLanguage] ?: "id-ID",
        agentMode = p[Keys.agentMode] ?: "AUTO",
        agentAuthority = p[Keys.agentAuthority] ?: "BAKU",
        agentToolsOff = p[Keys.agentToolsOff] ?: emptySet(),
        wizardDone = p[Keys.wizardDone] ?: false
    )

    val settings: Flow<AviSettings> = context.aviDataStore.data.map { load(it) }

    suspend fun save(transform: (AviSettings) -> AviSettings) {
        context.aviDataStore.edit { p ->
            val next = transform(load(p))
            p[Keys.provider] = next.provider.id
            p[Keys.geminiKey] = SecureStore.encrypt(next.geminiKey) ?: next.geminiKey
            p[Keys.geminiKeysExtra] = SecureStore.encrypt(next.geminiKeysExtra) ?: next.geminiKeysExtra
            p[Keys.ocBaseUrl] = next.ocBaseUrl.ifBlank { AiProvider.NIM_BASE_URL }
            p[Keys.ocKey] = SecureStore.encrypt(next.ocKey) ?: next.ocKey
            p[Keys.ocModel] = next.ocModel
            p[Keys.geminiModel] = next.geminiModel
            p[Keys.autoListen] = next.autoListen
            p[Keys.ttsEnabled] = next.ttsEnabled
            p[Keys.speechRate] = next.speechRate
            p[Keys.pitch] = next.pitch
            p[Keys.cloudTtsEnabled] = next.cloudTtsEnabled
            p[Keys.cloudTtsModel] = next.cloudTtsModel
            p[Keys.cloudVoice] = next.cloudVoice
            p[Keys.themeMark] = next.themeMark
            p[Keys.bootEnabled] = next.bootEnabled
            p[Keys.parallaxEnabled] = next.parallaxEnabled
            p[Keys.hapticsEnabled] = next.hapticsEnabled
            p[Keys.hudFrame] = next.hudFrame
            p[Keys.powerSave] = next.powerSave
            p[Keys.style] = next.style
            p[Keys.userName] = next.userName.ifBlank { "Pak" }
            p[Keys.sttLanguage] = next.sttLanguage.ifBlank { "id-ID" }
            p[Keys.agentMode] = next.agentMode
            p[Keys.agentAuthority] = next.agentAuthority
            p[Keys.agentToolsOff] = next.agentToolsOff
            p[Keys.wizardDone] = next.wizardDone
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
