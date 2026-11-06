package com.anztdree.avi.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.anztdree.avi.R

/**
 * K12 — efek suara UI premium AVI (bundel di APK).
 * SoundPool = lag-free, dekode sekali, hemat memori. Diputar hanya saat
 * bukan mode hemat daya; kegagalan audio tidak pernah mengganggu UI.
 */
object UiSounds {

    private var pool: SoundPool? = null
    private val ids = HashMap<Int, Int>()     // resId → soundId
    private val ready = HashSet<Int>()        // soundId yang sudah ter-dekode

    private fun ensure(context: Context): SoundPool? {
        if (pool == null) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build()
            pool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) ready.add(sampleId)
            }
            listOf(
                R.raw.avi_boot, R.raw.avi_send, R.raw.avi_receive,
                R.raw.avi_error, R.raw.avi_wake, R.raw.avi_live
            ).forEach { res ->
                try { ids[res] = pool!!.load(context.applicationContext, res, 1) } catch (_: Exception) {}
            }
        }
        return pool
    }

    private fun play(context: Context, resId: Int, volume: Float = 1f) {
        try {
            val p = ensure(context) ?: return
            val sid = ids[resId] ?: return
            if (sid in ready) p.play(sid, volume, volume, 1, 0, 1f)
        } catch (_: Exception) {
        }
    }

    /** Nyalakan reaktor (boot screen) */
    fun boot(c: Context) = play(c, R.raw.avi_boot, 0.9f)

    /** Pesan dikirim — mulai berpikir */
    fun send(c: Context) = play(c, R.raw.avi_send)

    /** Jawaban mulai mengalir */
    fun receive(c: Context) = play(c, R.raw.avi_receive, 0.8f)

    /** Terjadi galat */
    fun error(c: Context) = play(c, R.raw.avi_error)

    /** Mikrofon menyala / memulai mendengar */
    fun wake(c: Context) = play(c, R.raw.avi_wake, 0.7f)

    /** Masuk mode live */
    fun live(c: Context) = play(c, R.raw.avi_live, 0.8f)
}
