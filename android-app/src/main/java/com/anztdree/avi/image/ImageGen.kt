package com.anztdree.avi.image

import java.net.URLEncoder

/**
 * Pembuat gambar gratis via Pollinations.ai (tanpa API key).
 * Menghasilkan URL gambar yang bisa dimuat langsung (mis. dengan Coil).
 */
object ImageGen {

    fun buildUrl(prompt: String): String {
        val encoded = URLEncoder.encode(prompt.trim(), "UTF-8")
        val seed = (0..999999).random()
        return "https://image.pollinations.ai/prompt/$encoded?width=1024&height=1024&nologo=true&seed=$seed"
    }

    /** Ekstrak tag [GAMBAR: ...] dari jawaban model. Kembalikan pasangan (teks bersih, prompt gambar). */
    fun extractImageTag(reply: String): Pair<String, String?> {
        val regex = Regex("\\[GAMBAR:\\s*(.+?)]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val match = regex.find(reply) ?: return reply.trim() to null
        val prompt = match.groupValues[1].trim()
        val cleaned = reply.replace(regex, " ").replace(Regex("\\s+"), " ").trim()
        return cleaned to prompt
    }
}
