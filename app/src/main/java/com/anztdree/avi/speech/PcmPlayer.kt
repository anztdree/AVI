package com.anztdree.avi.speech

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread

/**
 * Pemutar PCM 16-bit mono dengan antrean berkelanjutan.
 * - playPcm : mainkan satu blok (ganti pemutaran berjalan)
 * - enqueue : tambahkan blok ke antrean tanpa memotong (untuk Live Mode streaming)
 * Melaporkan amplitudo tiap blok agar HUD bereaksi ke suara AVI.
 */
class PcmPlayer(
    private val onAmplitude: (Float) -> Unit = {},
    private val onDone: () -> Unit = {}
) {

    private val queue = ArrayDeque<ByteArray>()
    @Volatile private var running = false
    @Volatile private var sampleRate = 24000
    private var worker: Thread? = null

    private fun ensureThread() {
        if (worker?.isAlive == true) return
        running = true
        worker = thread(name = "avi-pcm") {
            var t: AudioTrack? = null
            try {
                var currentRate = -1
                while (true) {
                    val block: ByteArray = synchronized(queue) { queue.removeFirstOrNull() } ?: break
                    if (currentRate != sampleRate) {
                        try { t?.stop(); t?.release() } catch (_: Exception) {}
                        t = buildTrack(sampleRate)
                        t.play()
                        currentRate = sampleRate
                    }
                    if (!running) break
                    onAmplitude(amplitudeOf(block))
                    t?.write(block, 0, block.size)
                }
            } catch (_: Exception) {
            } finally {
                try { t?.stop() } catch (_: Exception) {}
                try { t?.release() } catch (_: Exception) {}
                running = false
                onAmplitude(0f)
                onDone()
            }
        }
    }

    fun enqueue(pcm: ByteArray, rate: Int) {
        if (pcm.isEmpty()) return
        synchronized(queue) {
            sampleRate = rate
            queue.addLast(pcm)
        }
        ensureThread()
    }

    fun playPcm(pcm: ByteArray, rate: Int) {
        synchronized(queue) { queue.clear() }
        enqueue(pcm, rate)
    }

    fun stop() {
        synchronized(queue) { queue.clear() }
        running = false
        worker = null
        onAmplitude(0f)
    }

    private fun buildTrack(rate: Int): AudioTrack {
        val minBuf = AudioTrack.getMinBufferSize(
            rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(rate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(minBuf)
            .build()
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
