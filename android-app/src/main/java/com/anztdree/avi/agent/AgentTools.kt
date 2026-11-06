package com.anztdree.avi.agent

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import com.anztdree.avi.data.AviSettings
import com.anztdree.avi.data.MemoryRepository
import com.anztdree.avi.device.DeviceActions
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * K22 — PERKAKAS AGEN AVI (function calling).
 *
 * Bukti DIAG-AGENT v2 (runner AS, key pemilik): function calling free tier
 * JALAN — model 3.x mengirim functionCall + thoughtSignature, dan loop
 * functionResponse 200 OK. Berarti AVI benar-benar bisa menjadi AGEN:
 * model memilih perkakas, AVI mengeksekusi di perangkat, model membaca hasil.
 *
 * SEMUA perkakas di bawah NYATA (bukan prototipe) — dieksekusi lokal.
 * Tidak ada yang menyentuh Google grounding (kuota 0, K17).
 *
 * Otoritas (filter per tingkat, dipilih pemilik di Pusat Kendali):
 *   AMAN  — baca-saja & tak berdampak (diagnostik, jam, hitung, ingatan)
 *   BAKU  — + aksi nyata ringan (buka aplikasi, volume, senter, share…)
 *   PENUH — + komunikasi (telepon, SMS draft)
 */
enum class Otoritas(val label: String, val uraian: String) {
    AMAN("AMAN", "Baca-saja: diagnostik, jam, hitung, ingatan"),
    BAKU("BAKU", "AMAN + aksi ringan: buka aplikasi, volume, senter, share"),
    PENUH("PENUH", "BAKU + komunikasi: telepon & SMS (selalu lewat konfirmasi layar)");

    fun mengizinkan(level: Otoritas): Boolean = level.ordinal <= this.ordinal
}

/** Hasil eksekusi perkakas — pesan dikirim balik ke model (functionResponse). */
data class ToolResult(val ok: Boolean, val pesan: String)

data class AgentTool(
    val name: String,
    val label: String,
    val description: String,
    val parameters: JsonObject?,
    val level: Otoritas,
    val executor: suspend (Context, JsonObject, AviSettings) -> ToolResult
)

// ===== Pembangun skema JSON (bentuk REST v1beta — terbukti di DIAG-AGENT) =====

private fun sProp(type: String, desc: String): JsonObject = buildJsonObject {
    put("type", JsonPrimitive(type))
    put("description", JsonPrimitive(desc))
}

private fun sSchema(props: Map<String, JsonObject>, required: List<String> = emptyList()): JsonObject =
    buildJsonObject {
        put("type", JsonPrimitive("OBJECT"))
        put("properties", buildJsonObject { props.forEach { (k, v) -> put(k, v) } })
        if (required.isNotEmpty()) put("required", JsonArray(required.map { JsonPrimitive(it) }))
    }

private fun argString(args: JsonObject, key: String): String? =
    (args[key] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }

private fun argInt(args: JsonObject, key: String): Int? =
    (args[key] as? JsonPrimitive)?.let { it.intOrNull ?: it.contentOrNull?.toIntOrNull() }

private fun noArg(): JsonObject = sSchema(emptyMap())

/**
 * Registri perkakas AVI. Satu instance per ViewModel (butuh MemoryRepository
 * agar ingatan agen = ingatan asisten, satu sumber).
 */
class AgentTools(private val memory: MemoryRepository) {

    val semua: List<AgentTool> = listOf(
        // ---------- AMAN — baca & tanpa dampak ----------
        AgentTool(
            name = "kondisi_perangkat",
            label = "Kondisi Perangkat",
            description = "Baca kondisi lengkap ponsel: baterai, RAM, penyimpanan, jaringan, tipe perangkat.",
            parameters = noArg(),
            level = Otoritas.AMAN
        ) { ctx, _, _ ->
            val d = DeviceActions.readDiagnostics(ctx)
            ToolResult(true, DeviceActions.summary(d))
        },
        AgentTool(
            name = "cek_baterai",
            label = "Cek Baterai",
            description = "Cek persentase baterai dan status pengisian daya.",
            parameters = noArg(),
            level = Otoritas.AMAN
        ) { ctx, _, _ ->
            val d = DeviceActions.readDiagnostics(ctx)
            ToolResult(
                true,
                if (d.charging) "Baterai ${d.batteryPct} persen, sedang diisi daya."
                else "Baterai ${d.batteryPct} persen, tidak sedang diisi."
            )
        },
        AgentTool(
            name = "jam_tanggal",
            label = "Jam & Tanggal",
            description = "Jam dan tanggal saat ini di perangkat.",
            parameters = noArg(),
            level = Otoritas.AMAN
        ) { _, _, _ ->
            val now = Date()
            val jam = SimpleDateFormat("HH.mm", Locale("id")).format(now)
            val tgl = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id")).format(now)
            ToolResult(true, "Sekarang jam $jam, $tgl.")
        },
        AgentTool(
            name = "cari_aplikasi",
            label = "Cari Aplikasi",
            description = "Cari aplikasi terpasang berdasarkan nama (tanpa membukanya). Untuk mengecek ada/tidaknya aplikasi.",
            parameters = sSchema(mapOf("nama" to sProp("STRING", "nama aplikasi yang dicari")), listOf("nama")),
            level = Otoritas.AMAN
        ) { ctx, args, _ ->
            val q = argString(args, "nama") ?: return@AgentTool ToolResult(false, "Nama aplikasi kosong")
            val apps = DeviceActions.findApps(ctx, q, limit = 6)
            if (apps.isEmpty()) ToolResult(false, "Tidak ada aplikasi yang cocok dengan \"$q\".")
            else ToolResult(true, "Ditemukan ${apps.size}: " + apps.joinToString(", ") { "${it.label} (${it.packageName})" })
        },
        AgentTool(
            name = "kalkulator",
            label = "Kalkulator",
            description = "Hitung ekspresi matematika: +, -, *, /, %, ^, kurung. Contoh: (12+8)*3.",
            parameters = sSchema(mapOf("ekspresi" to sProp("STRING", "ekspresi matematika")), listOf("ekspresi")),
            level = Otoritas.AMAN
        ) { _, args, _ ->
            val e = argString(args, "ekspresi") ?: return@AgentTool ToolResult(false, "Ekspresi kosong")
            val v = hitungEkspresi(e) ?: return@AgentTool ToolResult(false, "Ekspresi tidak bisa dihitung: $e")
            val tampil = if (v == Math.floor(v) && !v.isInfinite()) v.toLong().toString() else "%.6f".format(v)
            ToolResult(true, "$e = $tampil")
        },
        AgentTool(
            name = "simpan_ingatan",
            label = "Simpan Ingatan",
            description = "Simpan fakta penting tentang pemilik ke ingatan jangka panjang (permanen).",
            parameters = sSchema(mapOf("fakta" to sProp("STRING", "fakta singkat tentang pemilik")), listOf("fakta")),
            level = Otoritas.AMAN
        ) { _, args, _ ->
            val f = argString(args, "fakta") ?: return@AgentTool ToolResult(false, "Fakta kosong")
            memory.add(f)
            ToolResult(true, "Ingatan tersimpan: $f")
        },
        AgentTool(
            name = "baca_ingatan",
            label = "Baca Ingatan",
            description = "Baca seluruh fakta tersimpan tentang pemilik di ingatan jangka panjang.",
            parameters = noArg(),
            level = Otoritas.AMAN
        ) { _, _, _ ->
            val mem = memory.current.ifEmpty { memory.load() }
            if (mem.isEmpty()) ToolResult(true, "Ingatan masih kosong.")
            else ToolResult(true, "${mem.size} ingatan: " + mem.joinToString(" | ") { it.text })
        },

        // ---------- BAKU — aksi nyata ringan ----------
        AgentTool(
            name = "buka_aplikasi",
            label = "Buka Aplikasi",
            description = "Buka aplikasi di ponsel berdasarkan nama. Gunakan untuk semua perintah membuka aplikasi.",
            parameters = sSchema(mapOf("nama" to sProp("STRING", "nama aplikasi, mis. YouTube")), listOf("nama")),
            level = Otoritas.BAKU
        ) { ctx, args, _ ->
            val q = argString(args, "nama") ?: return@AgentTool ToolResult(false, "Nama aplikasi kosong")
            val apps = DeviceActions.findApps(ctx, q)
            if (apps.isEmpty()) return@AgentTool ToolResult(false, "Tidak menemukan aplikasi \"$q\" di ponsel.")
            val ok = DeviceActions.openApp(ctx, apps.first().packageName)
            if (ok) ToolResult(true, "Aplikasi ${apps.first().label} berhasil dibuka.")
            else ToolResult(false, "Gagal membuka ${apps.first().label}.")
        },
        AgentTool(
            name = "set_volume",
            label = "Atur Volume",
            description = "Atur volume media/musik dalam persen 0-100.",
            parameters = sSchema(mapOf("persen" to sProp("NUMBER", "0 sampai 100")), listOf("persen")),
            level = Otoritas.BAKU
        ) { ctx, args, _ ->
            val p = argInt(args, "persen") ?: return@AgentTool ToolResult(false, "Persen volume kosong")
            val am = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return@AgentTool ToolResult(false, "Layanan audio tidak tersedia")
            val maks = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val vol = p.coerceIn(0, 100) * maks / 100
            try {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
                ToolResult(true, "Volume media diatur ke $p persen (tingkat $vol dari $maks).")
            } catch (e: Exception) {
                ToolResult(false, "Gagal mengatur volume: ${e.message ?: "unknown"}")
            }
        },
        AgentTool(
            name = "senter",
            label = "Senter",
            description = "Nyalakan atau matikan lampu senter kamera.",
            parameters = sSchema(mapOf("status" to sProp("STRING", "\"nyala\" atau \"mati\"")), listOf("status")),
            level = Otoritas.BAKU
        ) { ctx, args, _ ->
            val s = (argString(args, "status") ?: "nyala").lowercase(Locale.ROOT)
            val nyala = s.contains("nyala") || s.contains("on") || s.contains("hidup")
            val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return@AgentTool ToolResult(false, "Layanan kamera tidak tersedia")
            val idLampu = cm.cameraIdList.firstOrNull { id ->
                val c = cm.getCameraCharacteristics(id)
                c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: return@AgentTool ToolResult(false, "Perangkat tidak punya lampu senter.")
            try {
                cm.setTorchMode(idLampu, nyala)
                ToolResult(true, if (nyala) "Senter menyala." else "Senter dimatikan.")
            } catch (e: Exception) {
                ToolResult(false, "Gagal mengatur senter: ${e.message ?: "unknown"}")
            }
        },
        AgentTool(
            name = "buka_url",
            label = "Buka Tautan",
            description = "Buka alamat web di browser ponsel.",
            parameters = sSchema(mapOf("url" to sProp("STRING", "alamat web lengkap")), listOf("url")),
            level = Otoritas.BAKU
        ) { ctx, args, _ ->
            var u = argString(args, "url") ?: return@AgentTool ToolResult(false, "URL kosong")
            if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://$u"
            return@AgentTool try {
                val uri = Uri.parse(u)
                if (uri.scheme !in listOf("http", "https")) ToolResult(false, "Skema URL tidak diizinkan.")
                else {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    ToolResult(true, "Tautan dibuka di browser: $u")
                }
            } catch (e: Exception) {
                ToolResult(false, "Gagal membuka tautan: ${e.message ?: "unknown"}")
            }
        },
        AgentTool(
            name = "cari_web",
            label = "Cari di Web",
            description = "Cari kata kunci di web lewat browser (default DuckDuckGo). Untuk info terkini.",
            parameters = sSchema(mapOf(
                "kata_kunci" to sProp("STRING", "kata kunci pencarian"),
                "mesin" to sProp("STRING", "opsional: duckduckgo atau google")
            ), listOf("kata_kunci")),
            level = Otoritas.BAKU
        ) { ctx, args, _ ->
            val q = argString(args, "kata_kunci") ?: return@AgentTool ToolResult(false, "Kata kunci kosong")
            val mesin = (argString(args, "mesin") ?: "duckduckgo").lowercase(Locale.ROOT)
            val url = if (mesin.contains("google")) "https://www.google.com/search?q="
            else "https://duckduckgo.com/?q="
            return@AgentTool try {
                ctx.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url + URLEncoder.encode(q, "UTF-8")))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                ToolResult(true, "Pencarian \"$q\" dibuka di browser ($mesin).")
            } catch (e: Exception) {
                ToolResult(false, "Gagal membuka pencarian: ${e.message ?: "unknown"}")
            }
        },
        AgentTool(
            name = "bagikan_teks",
            label = "Bagikan Teks",
            description = "Buka dialog berbagi Android untuk membagikan teks ke aplikasi lain.",
            parameters = sSchema(mapOf("teks" to sProp("STRING", "teks yang dibagikan")), listOf("teks")),
            level = Otoritas.BAKU
        ) { ctx, args, _ ->
            val t = argString(args, "teks") ?: return@AgentTool ToolResult(false, "Teks kosong")
            return@AgentTool try {
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, t)
                }
                ctx.startActivity(Intent.createChooser(i, "Bagikan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                ToolResult(true, "Dialog berbagi dibuka.")
            } catch (e: Exception) {
                ToolResult(false, "Gagal membuka dialog bagikan: ${e.message ?: "unknown"}")
            }
        },
        AgentTool(
            name = "buka_pengaturan",
            label = "Buka Pengaturan",
            description = "Buka halaman pengaturan Android: umum, wifi, bluetooth, tampilan, suara, atau baterai.",
            parameters = sSchema(mapOf("bagian" to sProp("STRING", "umum/wifi/bluetooth/tampilan/suara/baterai")), listOf("bagian")),
            level = Otoritas.BAKU
        ) { ctx, args, _ ->
            val b = (argString(args, "bagian") ?: "umum").lowercase(Locale.ROOT)
            val aksi = when {
                b.contains("wifi") || b.contains("wi-fi") -> Settings.ACTION_WIFI_SETTINGS
                b.contains("bluetooth") || b.contains("biru") -> Settings.ACTION_BLUETOOTH_SETTINGS
                b.contains("tampilan") || b.contains("display") || b.contains("layar") -> Settings.ACTION_DISPLAY_SETTINGS
                b.contains("suara") || b.contains("sound") -> Settings.ACTION_SOUND_SETTINGS
                b.contains("baterai") || b.contains("battery") -> Settings.ACTION_BATTERY_SAVER_SETTINGS
                else -> Settings.ACTION_SETTINGS
            }
            return@AgentTool try {
                ctx.startActivity(Intent(aksi).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                ToolResult(true, "Pengaturan ($b) dibuka.")
            } catch (e: Exception) {
                ToolResult(false, "Gagal membuka pengaturan: ${e.message ?: "unknown"}")
            }
        },

        // ---------- PENUH — komunikasi (selalu lewat konfirmasi layar) ----------
        AgentTool(
            name = "telepon",
            label = "Telepon",
            description = "Buka aplikasi telepon dengan nomor yang sudah diisi — PEMILIK yang menekan tombol panggil.",
            parameters = sSchema(mapOf("nomor" to sProp("STRING", "nomor telepon tujuan")), listOf("nomor")),
            level = Otoritas.PENUH
        ) { ctx, args, _ ->
            val n = argString(args, "nomor") ?: return@AgentTool ToolResult(false, "Nomor kosong")
            return@AgentTool try {
                ctx.startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$n")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                ToolResult(true, "Aplikasi telepon dibuka dengan nomor $n — menunggu pemilik menekan panggil.")
            } catch (e: Exception) {
                ToolResult(false, "Gagal membuka aplikasi telepon: ${e.message ?: "unknown"}")
            }
        },
        AgentTool(
            name = "kirim_sms",
            label = "SMS (draf)",
            description = "Siapkan draf SMS dengan nomor dan isi pesan — PEMILIK yang menekan tombol kirim.",
            parameters = sSchema(mapOf(
                "nomor" to sProp("STRING", "nomor tujuan"),
                "pesan" to sProp("STRING", "isi pesan")
            ), listOf("nomor", "pesan")),
            level = Otoritas.PENUH
        ) { ctx, args, _ ->
            val n = argString(args, "nomor") ?: return@AgentTool ToolResult(false, "Nomor kosong")
            val p = argString(args, "pesan") ?: return@AgentTool ToolResult(false, "Pesan kosong")
            return@AgentTool try {
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$n")).apply {
                    putExtra("sms_body", p)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(i)
                ToolResult(true, "Draf SMS ke $n disiapkan — menunggu pemilik menekan kirim.")
            } catch (e: Exception) {
                ToolResult(false, "Gagal menyiapkan SMS: ${e.message ?: "unknown"}")
            }
        }
    )

    /** Perkakas aktif sesuai otoritas + larangan per-perkakas dari pemilik. */
    fun daftarAktif(settings: AviSettings): List<AgentTool> {
        val otor = when (settings.agentAuthority) {
            "AMAN" -> Otoritas.AMAN
            "PENUH" -> Otoritas.PENUH
            else -> Otoritas.BAKU
        }
        val off = settings.agentToolsOff
        return semua.filter { otor.mengizinkan(it.level) && it.name !in off }
    }

    fun byName(name: String): AgentTool? = semua.firstOrNull { it.name == name }

    /** Ringkasan argumen utk tampilan log & timeline: nama="YouTube" */
    fun ringkasArgumen(args: JsonObject?): String =
        args?.entries?.take(2)?.joinToString(", ") { (k, v) ->
            val s = (v as? JsonPrimitive)?.contentOrNull ?: v.toString()
            "$k=${s.take(24)}"
        }.orEmpty()

    // ===== Kalkulator aman — parser rekursif, TANPA eval =====
    // Sebagai KELAS privat (bukan fungsi lokal) supaya rekursi silang
    // primer↔tambah boleh (fungsi lokal Kotlin tidak boleh referensi-maju).

    private fun hitungEkspresi(raw: String): Double? {
        val s = raw.replace(",", ".")
            .replace("×", "*").replace("÷", "/").replace(" ", "")
        if (s.isEmpty() || s.length > 80) return null
        if (!s.all { it in "0123456789.+-*/%^()" }) return null
        return try {
            val p = ParserEkspresi(s)
            p.utuh()
        } catch (_: Exception) { null }
    }

    private class ParserEkspresi(private val s: String) {
        private var pos = 0

        fun utuh(): Double? {
            val v = tambah()
            if (pos != s.length || v.isNaN() || v.isInfinite()) return null
            return v
        }

        private fun peek(): Char = if (pos < s.length) s[pos] else ' '

        private fun angka(): Double {
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
            if (start == pos) throw IllegalArgumentException("angka")
            return s.substring(start, pos).toDouble()
        }

        private fun primer(): Double {
            when (peek()) {
                '(' -> {
                    pos++
                    val v = tambah()
                    if (peek() == ')') pos++ else throw IllegalArgumentException("kurung")
                    return v
                }
                '-' -> { pos++; return -primer() }
                '+' -> { pos++; return primer() }
                else -> return angka()
            }
        }

        private fun pangkat(): Double {
            var v = primer()
            while (peek() == '^') { pos++; v = Math.pow(v, primer()) }
            return v
        }

        private fun kali(): Double {
            var v = pangkat()
            while (peek() == '*' || peek() == '/' || peek() == '%') {
                val op = peek(); pos++
                val r = pangkat()
                v = when (op) { '*' -> v * r; '/' -> v / r; else -> v % r }
            }
            return v
        }

        private fun tambah(): Double {
            var v = kali()
            while (peek() == '+' || peek() == '-') {
                val op = peek(); pos++
                val r = kali()
                v = if (op == '+') v + r else v - r
            }
            return v
        }
    }
}
