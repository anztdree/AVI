package com.anztdree.avi.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Pendengar AVI memakai SpeechRecognizer bawaan Android (bahasa Indonesia),
 * tampil dalam HUD kustom aplikasi (tanpa dialog Google bawaan).
 */
class AviStt(
    context: Context,
    private val callbacks: Listener
) : RecognitionListener {

    interface Listener {
        fun onListeningStarted()
        fun onRms(level: Float)
        fun onPartial(text: String)
        fun onResult(text: String)
        fun onError(message: String)
    }

    private val recognizer: SpeechRecognizer? =
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null

    init {
        recognizer?.setRecognitionListener(this)
    }

    val available: Boolean get() = recognizer != null

    fun start(language: String = "id-ID") {
        val r = recognizer ?: run {
            callbacks.onError("Fitur pengenalan suara tidak tersedia di perangkat ini.")
            return
        }
        val lang = language.ifBlank { "id-ID" }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang)
        }
        try {
            r.startListening(intent)
        } catch (e: Exception) {
            callbacks.onError("Gagal memulai perekaman: ${e.message}")
        }
    }

    fun stop() {
        try {
            recognizer?.stopListening()
        } catch (_: Exception) {
        }
    }

    fun destroy() {
        try {
            recognizer?.destroy()
        } catch (_: Exception) {
        }
    }

    // ===== RecognitionListener =====

    override fun onReadyForSpeech(params: Bundle?) {
        callbacks.onListeningStarted()
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        // rmsdB kira-kira -2 .. 10
        val level = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
        callbacks.onRms(level)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val msg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "Maaf pak, saya tidak menangkap suaranya. Coba lagi dekat mikrofon ya."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tidak ada suara terdeteksi pak."
            SpeechRecognizer.ERROR_AUDIO -> "Ada masalah audio di perangkat."
            SpeechRecognizer.ERROR_CLIENT -> "Klien pengenalan suara bermasalah."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Izin mikrofon belum diberikan."
            SpeechRecognizer.ERROR_NETWORK -> "Koneksi internet bermasalah untuk pengenalan suara."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Koneksi internet lambat, coba lagi ya pak."
            SpeechRecognizer.ERROR_SERVER -> "Server pengenalan suara bermasalah. Coba lagi ya pak."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Pengenalan suara sedang sibuk, coba sebentar lagi."
            else -> "Pengenalan suara gagal (kode $error)."
        }
        callbacks.onError(msg)
    }

    override fun onResults(results: Bundle?) {
        val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = list?.firstOrNull()?.trim().orEmpty()
        if (text.isNotEmpty()) callbacks.onResult(text)
        else callbacks.onError("Maaf pak, saya tidak menangkap suaranya. Coba lagi ya.")
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = list?.firstOrNull().orEmpty()
        if (text.isNotEmpty()) callbacks.onPartial(text)
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
