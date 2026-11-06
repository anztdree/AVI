package com.anztdree.avi.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/**
 * Suara AVI memakai TextToSpeech bawaan Android (bahasa Indonesia).
 * Ringan untuk HP spesifikasi rendah karena engine-nya di perangkat, bukan di cloud.
 */
class AviTts(
    context: Context,
    private val onReady: (Boolean) -> Unit,
    private val onSpeakDone: () -> Unit
) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingLocaleOk = false

    @Volatile var speaking = false
        private set

    init {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                // Coba "id-ID" lalu fallback "in-ID" (kode lama Indonesia)
                val setResult = tts?.setLanguage(Locale("id", "ID"))
                pendingLocaleOk = setResult == TextToSpeech.LANG_AVAILABLE ||
                    setResult == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                    setResult == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
                if (!pendingLocaleOk) {
                    val legacy = tts?.setLanguage(Locale("in", "ID"))
                    pendingLocaleOk = legacy == TextToSpeech.LANG_AVAILABLE ||
                        legacy == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                        legacy == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
                }
            }
            onReady(ready && pendingLocaleOk)
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                speaking = true
            }

            override fun onDone(utteranceId: String?) {
                speaking = false
                onSpeakDone()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                speaking = false
                onSpeakDone()
            }
        })
    }

    fun configure(rate: Float, pitch: Float) {
        if (!ready) return
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) {
            if (!ready) onSpeakDone()
            return
        }
        speaking = true
        val id = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        speaking = false
        tts?.stop()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        ready = false
    }
}
