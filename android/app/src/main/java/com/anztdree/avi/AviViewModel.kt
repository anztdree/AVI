package com.anztdree.avi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anztdree.avi.ai.AiClientFactory
import com.anztdree.avi.ai.AiStreamCallback
import com.anztdree.avi.ai.cleanForSpeech
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.ChatHistoryRepository
import com.anztdree.avi.data.ChatMessage
import com.anztdree.avi.data.SettingsRepository
import com.anztdree.avi.image.ImageGen
import com.anztdree.avi.speech.AviStt
import com.anztdree.avi.speech.AviTts
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Status mesin AVI yang ditampilkan di HUD */
enum class AviState { IDLE, LISTENING, THINKING, SPEAKING }

class AviViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app)
    private val historyRepo = ChatHistoryRepository(app)

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

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _ttsReady = MutableStateFlow(false)
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    /** true kalau sedang mode percakapan suara beruntun */
    private val _conversationActive = MutableStateFlow(false)
    val conversationActive: StateFlow<Boolean> = _conversationActive.asStateFlow()

    private var streamJob: Job? = null

    // STT dibuat terpisah oleh Activity (harus di main thread)
    private var stt: AviStt? = null

    private val tts = AviTts(
        context = app,
        onReady = { ok -> _ttsReady.value = ok },
        onSpeakDone = { onTtsDone() }
    )

    init {
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                _settings.value = s
                tts.configure(s.speechRate, s.pitch)
            }
        }
        viewModelScope.launch {
            val saved = historyRepo.load()
            _messages.value = if (saved.isEmpty()) {
                listOf(
                    ChatMessage(
                        role = "avi",
                        text = "Hay pak! Saya AVI, asisten pribadi Anda. Ada yang bisa saya bantu? " +
                            "Tap ikon mikrofon untuk ngobrol pakai suara ya."
                    )
                )
            } else {
                saved
            }
        }
    }

    /** Dipanggil dari Activity onCreate (main thread) */
    fun createStt() {
        if (stt != null) return
        stt = AviStt(getApplication(), object : AviStt.Listener {
            override fun onListeningStarted() {
                _state.value = AviState.LISTENING
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
                sendMessage(text)
            }

            override fun onError(message: String) {
                _partialText.value = ""
                if (_state.value == AviState.LISTENING) _state.value = AviState.IDLE
                _error.value = message
                _conversationActive.value = false
            }
        })
    }

    fun dismissError() {
        _error.value = null
    }

    /** Dipanggil setelah izin mikrofon diberikan */
    fun onMicPermissionGranted() = startVoiceConversation()

    fun toggleMic() {
        when (_state.value) {
            AviState.LISTENING -> {
                stt?.stop()
                _state.value = AviState.IDLE
                _conversationActive.value = false
            }
            AviState.SPEAKING -> {
                tts.stop()
                _state.value = AviState.IDLE
            }
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
        s.start()
    }

    fun sendText(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        sendMessage(t)
    }

    private fun sendMessage(text: String) {
        if (_state.value == AviState.THINKING || _state.value == AviState.SPEAKING) return
        _error.value = null
        _messages.value = _messages.value + ChatMessage(role = "user", text = text)
        _state.value = AviState.THINKING
        _streamingText.value = ""

        val history = historyRepo.contextWindow(_messages.value)
        val currentSettings = _settings.value
        val client = AiClientFactory.create(currentSettings)

        val callback = object : AiStreamCallback {
            override fun onDelta(text: String) {
                _streamingText.value += text
            }

            override fun onComplete(fullText: String) {
                finishReply(fullText)
            }

            override fun onError(message: String) {
                _state.value = AviState.IDLE
                _streamingText.value = ""
                _error.value = message
                _conversationActive.value = false
            }
        }

        streamJob = viewModelScope.launch {
            try {
                client.stream(history, currentSettings, callback)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = AviState.IDLE
                _streamingText.value = ""
                _error.value = "Gagal terhubung ke server AI: ${e.message ?: "unknown"}"
                _conversationActive.value = false
            }
        }
    }

    private fun finishReply(full: String) {
        val (cleanText, imagePrompt) = ImageGen.extractImageTag(full)
        val imageUrl = imagePrompt?.let { ImageGen.buildUrl(it) }
        val replyText = cleanText.ifBlank {
            if (imageUrl != null) "Siap pak, gambarnya sudah saya buatkan di bawah ini." else ""
        }
        _messages.value = _messages.value + ChatMessage(role = "avi", text = replyText, imageUrl = imageUrl)
        _streamingText.value = ""
        persist()

        val speech = cleanForSpeech(replyText)
        if (_settings.value.autoSpeak && speech.isNotBlank()) {
            _state.value = AviState.SPEAKING
            tts.speak(speech)
        } else {
            _state.value = AviState.IDLE
            maybeAutoListen()
        }
    }

    private fun onTtsDone() {
        if (_state.value == AviState.SPEAKING) _state.value = AviState.IDLE
        maybeAutoListen()
    }

    /** Lanjutkan mendengarkan jika mode percakapan suara aktif */
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

    private fun persist() {
        viewModelScope.launch { historyRepo.save(_messages.value) }
    }

    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(role = "avi", text = "Riwayat sudah saya bersihkan pak. Ada yang bisa saya bantu lagi?")
        )
        viewModelScope.launch { historyRepo.clear() }
    }

    fun updateSettings(transform: (AviSettings) -> AviSettings) {
        viewModelScope.launch { settingsRepo.save(transform) }
    }

    override fun onCleared() {
        super.onCleared()
        stt?.destroy()
        stt = null
        tts.shutdown()
    }
}
