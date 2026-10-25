package com.anztdree.avi

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anztdree.avi.ai.AiClientFactory
import com.anztdree.avi.ai.AiStreamCallback
import com.anztdree.avi.ai.CloudTts
import com.anztdree.avi.ai.LiveClient
import com.anztdree.avi.ai.ModelCatalog
import com.anztdree.avi.ai.GeminiModelInfo
import com.anztdree.avi.ai.SystemPrompts
import com.anztdree.avi.ai.cleanForSpeech
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.ChatHistoryRepository
import com.anztdree.avi.data.ChatMessage
import com.anztdree.avi.data.MemoryItem
import com.anztdree.avi.data.MemoryRepository
import com.anztdree.avi.data.Protocol
import com.anztdree.avi.data.ProtocolRepository
import com.anztdree.avi.data.SettingsRepository
import com.anztdree.avi.device.DeviceActions
import com.anztdree.avi.device.DeviceDiagnostics
import com.anztdree.avi.speech.AviStt
import com.anztdree.avi.speech.AviTts
import com.anztdree.avi.speech.MicStreamer
import com.anztdree.avi.speech.PcmPlayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Status mesin AVI yang ditampilkan di HUD Mark II */
enum class AviState { IDLE, LISTENING, THINKING, SPEAKING, LIVE }

class AviViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app)
    private val historyRepo = ChatHistoryRepository(app)
    private val memoryRepo = MemoryRepository(app)
    private val protocolRepo = ProtocolRepository(app)

    // ===== Flows (kontrak UI) =====

    private val _settings = MutableStateFlow(AviSettings())
    val settings: StateFlow<AviSettings> = _settings.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _state = MutableStateFlow(AviState.IDLE)
    val state: StateFlow<AviState> = _state.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    /** Level mikrofon 0..1 — gelombang nyata untuk ring HUD */
    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    /** Level suara AVI 0..1 — glow HUD saat AVI bicara */
    private val _voiceLevel = MutableStateFlow(0f)
    val voiceLevel: StateFlow<Float> = _voiceLevel.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _ttsReady = MutableStateFlow(false)
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    private val _conversationActive = MutableStateFlow(false)
    val conversationActive: StateFlow<Boolean> = _conversationActive.asStateFlow()

    private val _diagnostics = MutableStateFlow<DeviceDiagnostics?>(null)
    val diagnostics: StateFlow<DeviceDiagnostics?> = _diagnostics.asStateFlow()

    private val _models = MutableStateFlow<List<GeminiModelInfo>>(emptyList())
    val models: StateFlow<List<GeminiModelInfo>> = _models.asStateFlow()

    private val _modelsLoading = MutableStateFlow(false)
    val modelsLoading: StateFlow<Boolean> = _modelsLoading.asStateFlow()

    private val _modelsError = MutableStateFlow<String?>(null)
    val modelsError: StateFlow<String?> = _modelsError.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    private val _protocols = MutableStateFlow<List<Protocol>>(emptyList())
    val protocols: StateFlow<List<Protocol>> = _protocols.asStateFlow()

    private val _usageToday = MutableStateFlow(0)
    val usageToday: StateFlow<Int> = _usageToday.asStateFlow()

    // ===== Mesin internal =====

    private var streamJob: Job? = null
    private var stt: AviStt? = null
    private var testCallback: ((String) -> Unit)? = null
    private val pendingSteps = ArrayDeque<String>()
    private var briefingChecked = false
    private var liveWs: LiveClient? = null
    private val liveTranscript = StringBuilder()

    private val tts = AviTts(
        context = app,
        onReady = { ok -> _ttsReady.value = ok },
        onSpeakDone = { afterSpeak() }
    )

    private val cloudPlayer = PcmPlayer(
        onAmplitude = { _voiceLevel.value = it },
        onDone = { afterSpeak() }
    )

    private val livePlayer = PcmPlayer(
        onAmplitude = { _voiceLevel.value = it },
        onDone = {}
    )

    private val testPlayer = PcmPlayer()

    // ===== Init =====

    init {
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                _settings.value = s
                tts.configure(s.speechRate, s.pitch)
                if (!briefingChecked) {
                    briefingChecked = true
                    maybeBriefing(s)
                }
            }
        }
        viewModelScope.launch {
            val saved = historyRepo.load()
            _messages.value = if (saved.isEmpty()) {
                listOf(
                    ChatMessage(
                        role = "avi",
                        text = "Hay pak! AVI online — semua sistem normal. Sentuh reaktor untuk bicara, " +
                            "atau ketik di bawah ya, pak."
                    )
                )
            } else saved
        }
        viewModelScope.launch { _memories.value = memoryRepo.load() }
        viewModelScope.launch { _protocols.value = protocolRepo.load() }
        viewModelScope.launch {
            settingsRepo.usageToday.collect { _usageToday.value = it }
        }
        refreshDiagnostics()
    }

    // ===== STT =====

    /** Dipanggil dari Activity onCreate (main thread) */
    fun createStt() {
        if (stt != null) return
        stt = AviStt(getApplication(), object : AviStt.Listener {
            override fun onListeningStarted() {
                if (testCallback == null) setState(AviState.LISTENING)
                _partialText.value = ""
            }

            override fun onRms(level: Float) {
                _rmsLevel.value = level
            }

            override fun onPartial(text: String) {
                _partialText.value = text
            }

            override fun onResult(text: String) {
                _partialText.value = ""
                val cb = testCallback
                if (cb != null) {
                    testCallback = null
                    _state.value = AviState.IDLE
                    cb(text)
                } else {
                    handleUserInput(text)
                }
            }

            override fun onError(message: String) {
                _partialText.value = ""
                if (testCallback != null) {
                    testCallback = null
                    _state.value = AviState.IDLE
                    _error.value = message
                } else {
                    if (_state.value == AviState.LISTENING) setState(AviState.IDLE)
                    _error.value = message
                    _conversationActive.value = false
                }
            }
        })
    }

    fun dismissError() {
        _error.value = null
    }

    fun onMicPermissionGranted() = startVoiceConversation()

    fun toggleMic() {
        when (_state.value) {
            AviState.LISTENING -> {
                stt?.stop()
                setState(AviState.IDLE)
                _conversationActive.value = false
            }
            AviState.SPEAKING -> {
                tts.stop()
                cloudPlayer.stop()
                setState(AviState.IDLE)
                _conversationActive.value = false
            }
            AviState.LIVE -> stopLive()
            AviState.IDLE -> startVoiceConversation()
            AviState.THINKING -> { /* tombol dinonaktifkan saat berpikir */ }
        }
    }

    private fun startVoiceConversation() {
        val s = stt
        if (s == null || !s.available) {
            _error.value = "Pengenalan suara tidak tersedia di perangkat ini pak."
            return
        }
        _conversationActive.value = true
        viewModelScope.launch {
            if (settingsRepo.greetingDue()) {
                settingsRepo.markGreeted()
                speakDirect(greetingText())
            } else {
                s.start()
            }
        }
    }

    // ===== Input =====

    fun sendText(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        handleUserInput(t)
    }

    private fun handleUserInput(text: String) {
        if (_state.value == AviState.THINKING || _state.value == AviState.SPEAKING) return
        if (_state.value == AviState.LIVE) return
        if (handleLocal(text)) return
        sendToModel(text)
    }

    /** Aksi instan lokal: protokol, buka aplikasi, kondisi HP/jam/tanggal */
    private fun handleLocal(text: String): Boolean {
        val ctx = getApplication<Application>()

        protocolRepo.matchPhrase(text)?.let {
            _messages.value = _messages.value + ChatMessage(role = "user", text = text)
            runProtocol(it)
            return true
        }

        val lower = text.lowercase(Locale.ROOT).trim()
        if (lower.startsWith("buka ") || lower.startsWith("open ") ||
            (lower.contains(" buka ") && lower.length < 60)
        ) {
            val query = text.replace(Regex("(?i)(tolong|coba|sekarang|ya|dunk|dong|pak|bu|aplikasi|app|open|buka)"), " ")
                .replace(Regex("\\s+"), " ").trim()
            if (query.isNotEmpty()) {
                val apps = DeviceActions.findApps(ctx, query)
                if (apps.isNotEmpty()) {
                    val app = apps.first()
                    DeviceActions.openApp(ctx, app.packageName)
                    addLocalReply("Siap pak, ${app.label} sudah saya buka.")
                } else {
                    addLocalReply("Tidak ada aplikasi yang cocok dengan \"$query\" di HP, pak.")
                }
                return true
            }
        }

        DeviceActions.localAnswer(text, _diagnostics.value ?: DeviceActions.readDiagnostics(ctx))
            ?.let { addLocalReply(it); return true }

        return false
    }

    private fun addLocalReply(reply: String) {
        _messages.value = _messages.value + ChatMessage(role = "avi", text = reply)
        persist()
        if (_settings.value.autoSpeak && _settings.value.autoListen) {
            setState(AviState.SPEAKING)
            tts.speak(cleanForSpeech(reply))
        } else {
            maybeAutoListen()
        }
    }

    // ===== Alur ke model AI =====

    private fun sendToModel(userText: String) {
        if (_state.value == AviState.THINKING || _state.value == AviState.LIVE) return
        _error.value = null
        _messages.value = _messages.value + ChatMessage(role = "user", text = userText)
        setState(AviState.THINKING)
        _streamingText.value = ""

        val runtime = _settings.value.copy(
            memoryText = memoryRepo.joinedText(),
            recap = buildRecap(),
            researchMode = isResearchRequest(userText)
        )

        val history = historyRepo.contextWindow(_messages.value)
        val client = AiClientFactory.create(runtime)

        val callback = object : AiStreamCallback {
            override fun onDelta(text: String) {
                _streamingText.value += text
            }

            override fun onComplete(fullText: String) {
                viewModelScope.launch {
                    settingsRepo.bumpUsage()
                    finishReply(fullText)
                }
            }

            override fun onError(message: String) {
                setState(AviState.IDLE)
                _streamingText.value = ""
                _error.value = message
                _conversationActive.value = false
                pendingSteps.clear()
            }
        }

        streamJob = viewModelScope.launch {
            try {
                client.stream(history, runtime, callback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setState(AviState.IDLE)
                _streamingText.value = ""
                _error.value = "Gagal terhubung ke server AI: ${e.message ?: "unknown"}"
                _conversationActive.value = false
                pendingSteps.clear()
            }
        }
    }

    private fun isResearchRequest(text: String): Boolean {
        val t = text.lowercase(Locale.ROOT)
        return t.startsWith("riset") || t.contains("risetkan") || t.contains("riset tentang") ||
            t.contains("teliti tentang") || t.contains("cari tahu tentang")
    }

    /** X7 — kesinambungan topik: potongan obrolan sebelumnya (pesan lama) */
    private fun buildRecap(): String {
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val old = _messages.value.filter {
            SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(it.timestamp)) != today
        }
        if (old.isEmpty()) return ""
        val tail = old.takeLast(6).joinToString(" ") {
            (if (it.role == "user") "Pemilik: " else "AVI: ") + it.text
        }
        return tail.take(500)
    }

    private fun finishReply(raw: String) {
        val ctx = getApplication<Application>()

        // [INGAT: ...] → memori jangka panjang
        Regex("\\[INGAT\\s*:\\s*(.*?)]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .findAll(raw)
            .forEach { m ->
                val fact = m.groupValues[1].trim()
                if (fact.isNotEmpty()) {
                    viewModelScope.launch {
                        memoryRepo.add(fact)
                        _memories.value = memoryRepo.current
                    }
                }
            }

        // [BUKA: ...] → aksi buka aplikasi dari model
        Regex("\\[BUKA\\s*:\\s*(.*?)]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .findAll(raw)
            .forEach { m ->
                val apps = DeviceActions.findApps(ctx, m.groupValues[1])
                if (apps.isNotEmpty()) DeviceActions.openApp(ctx, apps.first().packageName)
            }

        var text = raw
            .replace(Regex("\\[(INGAT|BUKA|LUPAKAN|PROTOKOL|GAMBAR)\\s*:.*?]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
            .replace(Regex("\\[/?[A-Z_]*]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (text.isBlank()) text = "Siap pak."

        _messages.value = _messages.value + ChatMessage(role = "avi", text = text)
        _streamingText.value = ""
        persist()
        speakReply(text)
    }

    // ===== Suara =====

    private fun speakReply(text: String) {
        val speech = cleanForSpeech(text)
        val s = _settings.value
        if (!s.autoSpeak || speech.isBlank()) {
            afterSpeak()
            return
        }
        setState(AviState.SPEAKING)
        if (s.provider == com.anztdree.avi.data.AiProvider.GEMINI &&
            s.cloudTtsEnabled && s.cloudTtsModel.isNotBlank()
        ) {
            viewModelScope.launch {
                CloudTts.synthesize(speech, s)
                    .onSuccess { cloudPlayer.playPcm(it.pcm, it.sampleRate) }
                    .onFailure { tts.speak(speech) } // fallback suara Android
            }
        } else {
            tts.speak(speech)
        }
    }

    /** Sapaan langsung (selalu bersuara, abaikan autoSpeak) */
    private fun speakDirect(text: String) {
        setState(AviState.SPEAKING)
        val s = _settings.value
        if (s.provider == com.anztdree.avi.data.AiProvider.GEMINI &&
            s.cloudTtsEnabled && s.cloudTtsModel.isNotBlank()
        ) {
            viewModelScope.launch {
                CloudTts.synthesize(cleanForSpeech(text), s)
                    .onSuccess { cloudPlayer.playPcm(it.pcm, it.sampleRate) }
                    .onFailure { tts.speak(text) }
            }
        } else {
            tts.speak(text)
        }
    }

    private fun afterSpeak() {
        if (_state.value == AviState.SPEAKING) setState(AviState.IDLE)
        if (pendingSteps.isNotEmpty()) {
            processQueue()
            return
        }
        maybeAutoListen()
    }

    private fun maybeAutoListen() {
        if (_conversationActive.value && _settings.value.autoListen && _state.value == AviState.IDLE) {
            viewModelScope.launch {
                delay(600)
                if (_conversationActive.value && _state.value == AviState.IDLE) {
                    stt?.start()
                }
            }
        }
    }

    private fun greetingText(): String {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val salam = when (h) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..17 -> "Selamat sore"
            else -> "Selamat malam"
        }
        return "$salam, ${_settings.value.userName}. AVI mendengarkan — silakan bicara, pak."
    }

    // ===== Protokol & antrian =====

    fun runProtocol(p: Protocol) {
        if (p.steps.isEmpty()) {
            _error.value = "Protokol \"${p.name}\" belum punya langkah, pak."
            return
        }
        _messages.value = _messages.value + ChatMessage(role = "user", text = "▶ ${p.name} dijalankan")
        pendingSteps.clear()
        pendingSteps.addAll(p.steps)
        processQueue()
    }

    private fun processQueue() {
        if (_state.value != AviState.IDLE) return
        val next = pendingSteps.removeFirstOrNull() ?: return
        sendToModel(next)
    }

    /** X4 — briefing pagi proaktif */
    private fun maybeBriefing(s: AviSettings) {
        viewModelScope.launch {
            val configured = when (s.provider) {
                com.anztdree.avi.data.AiProvider.GEMINI ->
                    s.geminiKey.isNotBlank() && s.geminiModel.isNotBlank()
                else -> s.ocKey.isNotBlank()
            }
            if (!configured || settingsRepo.isBriefedToday()) return@launch
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour in 5..10) {
                settingsRepo.markBriefed()
                delay(2500)
                _messages.value = _messages.value + ChatMessage(
                    role = "avi",
                    text = "Selamat pagi, ${s.userName}. AVI siapkan briefing pagi Anda…"
                )
                pendingSteps.add(
                    "Briefing pagi singkat pakai pencarian web: cuaca hari ini, lalu 2 berita utama " +
                        "Indonesia hari ini. Jawaban maksimal 4 kalimat, tutup dengan satu kalimat semangat."
                )
                processQueue()
            }
        }
    }

    // ===== X1 — Live Voice Mode =====

    fun startLive() {
        val s = _settings.value
        if (liveWs != null || _state.value == AviState.LIVE) return
        if (s.provider != com.anztdree.avi.data.AiProvider.GEMINI || s.geminiKey.isBlank()) {
            _error.value = "Live mode butuh API key Gemini, pak."
            return
        }
        if (_models.value.isEmpty()) {
            _error.value = "Muat daftar model dulu di Pusat Kendali, pak — AVI perlu tahu model live terbaru."
            return
        }
        val model = ModelCatalog.pickLiveModel(_models.value, s.geminiModel)
        if (model == null) {
            _error.value = "Tidak ada model live di daftar terbaru, pak. Coba muat ulang daftar model."
            return
        }
        _conversationActive.value = false
        setState(AviState.LIVE)
        liveTranscript.clear()
        val prompt = SystemPrompts.build(s.copy(memoryText = memoryRepo.joinedText()))
        liveWs = LiveClient(s.geminiKey, model, prompt, object : LiveClient.Listener {
            override fun onSetupComplete() {}

            override fun onAudioPcm(pcm: ByteArray) {
                livePlayer.enqueue(pcm, 24000)
            }

            override fun onTranscript(text: String) {
                liveTranscript.append(text)
            }

            override fun onInterrupted() {
                livePlayer.stop()
            }

            override fun onTurnComplete() {
                val t = liveTranscript.toString().trim()
                liveTranscript.clear()
                if (t.isNotEmpty()) {
                    _messages.value = _messages.value + ChatMessage(role = "avi", text = t)
                    persist()
                }
            }

            override fun onError(message: String) {
                stopLive()
                _error.value = message
            }

            override fun onClosed() {
                if (_state.value == AviState.LIVE) stopLive()
            }
        }).also { it.connect() }

        mic.start(
            onChunk = { pcm, level ->
                _rmsLevel.value = level
                liveWs?.sendAudioPcm(pcm)
            },
            onError = { msg ->
                stopLive()
                _error.value = msg
            }
        )
    }

    fun stopLive() {
        mic.stop()
        liveWs?.close()
        liveWs = null
        livePlayer.stop()
        _voiceLevel.value = 0f
        if (_state.value == AviState.LIVE) setState(AviState.IDLE)
    }

    // ===== Katalog model (fetch dari Google) =====

    fun fetchModels() {
        val key = _settings.value.geminiKey.trim()
        if (key.isBlank()) {
            _modelsError.value = "Isi API key Gemini dulu, pak."
            return
        }
        _modelsLoading.value = true
        _modelsError.value = null
        viewModelScope.launch {
            ModelCatalog.fetch(key)
                .onSuccess { list ->
                    _models.value = list
                    _modelsLoading.value = false
                }
                .onFailure {
                    _modelsLoading.value = false
                    _modelsError.value = it.message
                }
        }
    }

    // ===== Ruang tes suara (Pusat Kendali) =====

    fun speakTest(text: String) {
        val s = _settings.value
        val speech = cleanForSpeech(text).ifBlank { text }
        viewModelScope.launch {
            if (s.provider == com.anztdree.avi.data.AiProvider.GEMINI &&
                s.cloudTtsEnabled && s.cloudTtsModel.isNotBlank()
            ) {
                CloudTts.synthesize(speech, s)
                    .onSuccess { testPlayer.playPcm(it.pcm, it.sampleRate) }
                    .onFailure { tts.speak(speech) }
            } else {
                tts.speak(speech)
            }
        }
    }

    /** Dengar sekali untuk tes STT — hasil dikirim ke callback, tidak masuk chat */
    fun listenOnce(onResult: (String) -> Unit) {
        val s = stt
        if (s == null || !s.available) {
            _error.value = "Pengenalan suara tidak tersedia di perangkat ini pak."
            return
        }
        testCallback = onResult
        s.start()
    }

    // ===== Memori =====

    fun addMemory(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            memoryRepo.add(t)
            _memories.value = memoryRepo.current
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            memoryRepo.remove(id)
            _memories.value = memoryRepo.current
        }
    }

    // ===== Protokol =====

    fun saveProtocol(p: Protocol) {
        viewModelScope.launch {
            protocolRepo.save(p)
            _protocols.value = protocolRepo.current
        }
    }

    fun deleteProtocol(id: String) {
        viewModelScope.launch {
            protocolRepo.remove(id)
            _protocols.value = protocolRepo.current
        }
    }

    // ===== Diagnostik =====

    fun refreshDiagnostics() {
        viewModelScope.launch {
            _diagnostics.value = DeviceActions.readDiagnostics(getApplication())
        }
    }

    // ===== Lainnya =====

    private fun persist() {
        viewModelScope.launch { historyRepo.save(_messages.value) }
    }

    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(role = "avi", text = "Riwayat sudah saya bersihkan, pak. Ada yang bisa saya bantu lagi?")
        )
        viewModelScope.launch { historyRepo.clear() }
    }

    fun updateSettings(transform: (AviSettings) -> AviSettings) {
        viewModelScope.launch { settingsRepo.save(transform) }
    }

    // ===== Haptic khas per state =====

    private fun setState(s: AviState) {
        _state.value = s
        val pattern = when (s) {
            AviState.LISTENING -> longArrayOf(0, 40)
            AviState.THINKING -> longArrayOf(0, 60, 80, 60)
            AviState.SPEAKING -> longArrayOf(0, 30, 60, 30)
            AviState.LIVE -> longArrayOf(0, 80, 60, 80)
            AviState.IDLE -> return
        }
        if (!_settings.value.hapticsEnabled || _settings.value.powerSave) return
        try {
            val v = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (v != null && v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION") v.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        stt?.destroy()
        stt = null
        mic.stop()
        liveWs?.close()
        liveWs = null
        cloudPlayer.stop()
        livePlayer.stop()
        testPlayer.stop()
        tts.shutdown()
    }
}
