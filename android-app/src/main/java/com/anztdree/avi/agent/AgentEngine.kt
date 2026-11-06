package com.anztdree.avi.agent

import android.content.Context
import com.anztdree.avi.ai.GeminiClient
import com.anztdree.avi.ai.GeminiContent
import com.anztdree.avi.ai.GeminiFunctionDeclaration
import com.anztdree.avi.ai.GeminiFunctionResponse
import com.anztdree.avi.ai.GeminiPart
import com.anztdree.avi.ai.GeminiTool
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.ChatMessage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Locale

/**
 * K22 — MESIN AGEN AVI.
 *
 * Loop: PIKIR → AKSI → BACA HASIL → … → LAPOR.
 * Model memilih perkakas, AVI mengeksekusi di perangkat, hasil dikirim balik
 * (functionResponse), sampai model mengirim jawaban teks.
 *
 * Bukti DIAG-AGENT v2 (runner AS): loop ini 200 OK di free tier, dengan syarat
 * giliran model di-echo VERBATIM (thoughtSignature ikut). Bentuk request
 * penuh (thinkingOff+tools) gagal di flash-lite → tangga turun otomatis.
 *
 * JALAN TARIK AMAN: kalau mesin gagal karenanya (400/404/5xx, kosong), hasilnya
 * JatuhkanKeChat → ViewModel mengulang lewat jalur chat polos yang terbukti
 * selalu hidup. Fitur utama ASISTEN TIDAK PERNAH rusak karena agen.
 */
sealed class AgentEvent {
    /** Model sedang berpikir / menimbang langkah. */
    data object Berpikir : AgentEvent()

    /** AVI menjalankan perkakas. */
    data class Aksi(val tool: String, val label: String, val argumen: String) : AgentEvent()

    /** Hasil eksekusi perkakas. */
    data class Hasil(val tool: String, val ok: Boolean, val pesan: String) : AgentEvent()
}

sealed class AgentOutcome {
    /** Semua langkah selesai — jawaban final dari model. */
    data class Sukses(val jawaban: String) : AgentOutcome()

    /** Mesin tidak bisa jalan → ViewModel jatuh ke chat polos (jalur terbukti). */
    data object JatuhkanKeChat : AgentOutcome()

    /** Gagal bermakna (mis. kuota) — sampaikan pesan jujur. */
    data class Gagal(val pesan: String) : AgentOutcome()
}

class AgentEngine(
    private val context: Context,
    private val client: GeminiClient,
    private val tools: AgentTools
) {

    companion object {
        /** Batas langkah loop — pelindung kuota free tier. */
        const val MAKS_LANGKAH = 6
    }

    suspend fun jalankan(
        userText: String,
        history: List<ChatMessage>,
        settings: AviSettings,
        onEvent: (AgentEvent) -> Unit
    ): AgentOutcome {
        val aktif = tools.daftarAktif(settings)
        if (aktif.isEmpty()) return AgentOutcome.JatuhkanKeChat

        val deklarasi = listOf(
            GeminiTool(aktif.map { GeminiFunctionDeclaration(it.name, it.description, it.parameters) })
        )

        // var — diperkaya di setiap putaran loop (giliran model + hasil perkakas)
        var contents = history.map { msg ->
            GeminiContent(
                role = if (msg.role == "user") "user" else "model",
                parts = listOf(GeminiPart(text = msg.text))
            )
        } + GeminiContent(role = "user", parts = listOf(GeminiPart(text = userText)))

        onEvent(AgentEvent.Berpikir)

        repeat(MAKS_LANGKAH) {
            when (val r = client.generateOnce(contents, settings, deklarasi)) {
                is GeminiClient.Round.Teks -> return AgentOutcome.Sukses(r.text)

                is GeminiClient.Round.Gagal -> {
                    // Kuota/hubungan = gagal bermakna; selain itu → chat polos.
                    return if (r.httpCode == 0 || r.httpCode == 429) AgentOutcome.Gagal(r.pesan)
                    else AgentOutcome.JatuhkanKeChat
                }

                is GeminiClient.Round.Panggilan -> {
                    // ECHO VERBATIM giliran model (thoughtSignature ikut) — bukti v2.
                    contents += r.modelContent

                    val balasan = mutableListOf<GeminiPart>()
                    for (call in r.calls) {
                        val nama = call.name ?: continue
                        val tool = aktif.firstOrNull { it.name == nama }
                        if (tool == null) {
                            onEvent(AgentEvent.Hasil(nama, false, "dibatasi otoritas pemilik"))
                            balasan += respons(nama, false, "Perkakas \"$nama\" tidak diizinkan oleh pemilik.")
                            continue
                        }

                        onEvent(AgentEvent.Aksi(nama, tool.label, tools.ringkasArgumen(call.args)))
                        val hasil = try {
                            tool.executor(context, call.args ?: JsonObject(emptyMap()), settings)
                        } catch (e: Exception) {
                            ToolResult(false, "Eksekusi gagal: ${e.message ?: "unknown"}")
                        }
                        onEvent(AgentEvent.Hasil(nama, hasil.ok, hasil.pesan))
                        balasan += respons(nama, hasil.ok, hasil.pesan)
                    }
                    contents += GeminiContent(role = "user", parts = balasan)
                }
            }
        }

        // Langkah habis — putaran terakhir TANPA perkakas, paksa jawaban teks.
        return when (val r = client.generateOnce(contents, settings, emptyList())) {
            is GeminiClient.Round.Teks -> AgentOutcome.Sukses(r.text)
            is GeminiClient.Round.Gagal ->
                if (r.httpCode == 0 || r.httpCode == 429) AgentOutcome.Gagal(r.pesan)
                else AgentOutcome.Gagal("Perintah terlalu panjang — agent berhenti di batas langkah.")
            is GeminiClient.Round.Panggilan ->
                AgentOutcome.Gagal("Perintah terlalu panjang — agent berhenti di batas langkah.")
        }
    }

    private fun respons(nama: String, ok: Boolean, pesan: String): GeminiPart {
        val isi = buildJsonObject {
            put("status", if (ok) "OK" else "GAGAL")
            put("pesan", pesan.take(400))
        }
        return GeminiPart(
            functionResponse = GeminiFunctionResponse(name = nama, response = isi)
        )
    }
}

/**
 * Router ASISTEN → AGEN.
 * Perintah sederhana tetap di jalur asisten yang terbukti (Pak: "asisten tetap");
 * perintah kompleks (multi-aksi / butuh perkakas) dibuka lewat mesin agen.
 * Semua kata kunci di sini KATA KERJA AKSI — bukan topik — supaya obrolan biasa
 * tidak tersedot ke agen.
 */
object AgentRouter {

    /** Penanda perintah berantai / multi-langkah. */
    private val kataGabung = listOf(
        " lalu ", " terus ", " kemudian ", " setelah itu", " abis itu",
        " habis itu", " sekaligus", " sekalian ", " sambil "
    )

    /** Kata kerja aksi perangkat yang butuh perkakas (di luar jalur lokal). */
    private val kataAksi = listOf(
        "atur volume", "volume ", "kecilkan", "besarkan", "keraskan", "pelankan",
        "naikkan", "turunkan", "nyalakan", "matikan", "set volume",
        "kirim ", "sms ", "telpon", "telepon", "hubungi ",
        "cari di web", "cari di internet", "googling", "browsing", "buka web",
        "buka situs", "buka tautan", "buka link",
        "hitung ", "kalkulasi", "kalkulator",
        "senter", "lampu depan", "torc",
        "bagikan ", "share ",
        "ingatkan saya", "ingat ya", "catat ", "simpan ingatan",
        "berapa hasil", "bukakan"
    )

    /** Ada penanda perintah berantai? (dipakai juga utk meloloskan dari jalur lokal) */
    fun adaGabung(text: String): Boolean {
        val t = " ${text.lowercase(Locale.ROOT).trim()} "
        return kataGabung.any { t.contains(it) }
    }

    /** True = perintah layak masuk mesin agen. */
    fun perluAgent(text: String): Boolean {
        val t = " ${text.lowercase(Locale.ROOT).trim()} "
        if (kataGabung.any { t.contains(it) }) return true
        if (kataAksi.any { t.contains(it) }) return true
        return false
    }
}
