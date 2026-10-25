package com.anztdree.avi.speech

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.concurrent.thread

/**
 * Perekam mikrofon mentah PCM 16 kHz mono — untuk Live Voice Mode.
 * Mengalirkan chunk PCM + level amplitudo (0..1) untuk waveform HUD.
 * Izin RECORD_AUDIO dicek pemanggil.
 */
class MicStreamer {

    companion object {
        const val SAMPLE_RATE = 16000
        private const val CHUNK_SAMPLES = 1600 // 100 ms
    }

    @Volatile private var record: AudioRecord? = null
    @Volatile private var running = false

    val isRunning: Boolean get() = running

    @SuppressLint("MissingPermission")
    fun start(
        onChunk: (ByteArray, Float) -> Unit,
        onError: (String) -> Unit
    ) {
        if (running) return
        try {
            val minBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) {
                onError("Mikrofon tidak tersedia di perangkat ini.")
                return
            }
            val rec = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf * 2
            )
            record = rec
            rec.startRecording()
            running = true

            thread(name = "avi-mic") {
                val buffer = ByteArray(CHUNK_SAMPLES * 2)
                while (running) {
                    try {
                        val read = rec.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            val chunk = buffer.copyOf(read)
                            onChunk(chunk, amplitudeOf(chunk))
                        }
                    } catch (_: Exception) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            running = false
            onError("Gagal mengakses mikrofon: ${e.message ?: "unknown"}")
        }
    }

    fun stop() {
        running = false
        try { record?.stop() } catch (_: Exception) {}
        try { record?.release() } catch (_: Exception) {}
        record = null
    }

    private fun amplitudeOf(block: ByteArray): Float {
        var max = 0
        var i = 0
        while (i + 1 < block.size) {
            val lo = block[i].toInt() and 0xFF
            val hi = block[i + 1].toInt()
            val sample = (hi shl 8) or lo
            val v = if (sample >= 0x8000) sample - 0x10000 else sample
            val abs = if (v < 0) -v else v
            if (abs > max) max = abs
            i += 2
        }
        return (max / 32768f).coerceIn(0f, 1f)
    }
}
