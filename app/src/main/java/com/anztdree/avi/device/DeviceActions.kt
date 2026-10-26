package com.anztdree.avi.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Potret kondisi HP — dibaca lokal, instan, tanpa internet */
data class DeviceDiagnostics(
    val batteryPct: Int = 0,
    val charging: Boolean = false,
    val ramUsedPct: Int = 0,
    val ramTotalGb: Float = 0f,
    val storageFreeGb: Float = 0f,
    val storageTotalGb: Float = 0f,
    val network: String = "OFFLINE",
    val device: String = "",
    val androidVersion: String = ""
)

data class AppEntry(val label: String, val packageName: String)

object DeviceActions {

    // ===== Diagnostik =====

    fun readDiagnostics(context: Context): DeviceDiagnostics {
        // Baterai (sticky broadcast, tanpa receiver berjalan)
        val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        // RAM
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mem)
        val ramTotalGb = mem.totalMem / (1024f * 1024f * 1024f)
        val ramUsedPct = if (mem.totalMem > 0) (((mem.totalMem - mem.availMem) * 100) / mem.totalMem).toInt() else 0

        // Penyimpanan
        val stat = try { StatFs(Environment.getDataDirectory().path) } catch (_: Exception) { null }
        val storageTotalGb = stat?.let { it.totalBytes / (1024f * 1024f * 1024f) } ?: 0f
        val storageFreeGb = stat?.let { it.availableBytes / (1024f * 1024f * 1024f) } ?: 0f

        // Jaringan
        val network = try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            when {
                caps == null -> "OFFLINE"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "DATA SELULER"
                else -> "TERHUBUNG"
            }
        } catch (_: Exception) {
            "TIDAK DIKETAHUI"
        }

        return DeviceDiagnostics(
            batteryPct = batteryPct,
            charging = charging,
            ramUsedPct = ramUsedPct,
            ramTotalGb = ramTotalGb,
            storageFreeGb = storageFreeGb,
            storageTotalGb = storageTotalGb,
            network = network,
            device = Build.MODEL,
            androidVersion = "Android ${Build.VERSION.RELEASE}"
        )
    }

    fun summary(d: DeviceDiagnostics, panggilan: String = "Pak"): String {
        val charge = if (d.charging) "sedang diisi" else "tidak diisi"
        return "Kondisi markas, $panggilan: baterai ${d.batteryPct} persen $charge, " +
            "RAM terpakai ${d.ramUsedPct} persen dari ${"%.1f".format(d.ramTotalGb)} gigabyte, " +
            "penyimpanan sisa ${"%.1f".format(d.storageFreeGb)} gigabyte dari ${"%.1f".format(d.storageTotalGb)} gigabyte, " +
            "jaringan ${d.network.lowercase()}, perangkat ${d.device}, ${d.androidVersion}."
    }

    // ===== Aplikasi terpasang =====

    fun installedApps(context: Context): List<AppEntry> = try {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        context.packageManager.queryIntentActivities(intent, 0)
            .map { AppEntry(it.loadLabel(context.packageManager).toString(), it.activityInfo.packageName) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
    } catch (_: Exception) {
        emptyList()
    }

    private fun normalize(s: String): String =
        s.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")

    /** Cari aplikasi berdasarkan nama (pencocokan pintar, hasil terurut terbaik dulu) */
    fun findApps(context: Context, query: String, limit: Int = 5): List<AppEntry> {
        val q = normalize(query)
        if (q.isEmpty()) return emptyList()
        return installedApps(context)
            .map { app ->
                val l = normalize(app.label)
                val score = when {
                    l == q -> 100
                    l.startsWith(q) -> 80
                    l.contains(q) -> 60
                    q.contains(l) && l.length >= 3 -> 40
                    else -> 0
                }
                app to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    fun openApp(context: Context, packageName: String): Boolean = try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } else false
    } catch (_: Exception) {
        false
    }

    // ===== Jawaban instan lokal (tanpa internet, tanpa API) =====

    private fun normalized(text: String): String =
        text.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9\\s]"), " ").trim()

    /**
     * Deteksi pertanyaan dasar yang bisa dijawab lokal: kondisi HP, baterai, RAM,
     * penyimpanan, jaringan, jam, tanggal. Return null kalau bukan.
     * K18 — seluruh jawaban memakai panggilan custom pemilik (bukan selalu "pak").
     */
    fun localAnswer(text: String, d: DeviceDiagnostics, panggilan: String = "Pak"): String? {
        val t = normalized(text)
        if (t.isEmpty()) return null

        val askJam = t.contains("jam berapa") || t.contains("pukul berapa")
        val askTanggal = t.contains("tanggal berapa") || t.contains("hari apa sekarang") ||
            t.contains("hari ini hari apa")
        val askBaterai = t.contains("baterai") || t.contains("battery")
        val askRam = t.contains("ram") || t.contains("memori hp")
        val askStorage = t.contains("penyimpanan") || t.contains("storage") || t.contains("memori internal")
        val askJaringan = t.contains("jaringan") || t.contains("internet") ||
            t.contains("wifi") || t.contains("sinyal")
        val askKondisi = t.contains("kondisi hp") || t.contains("kondisi handphone") ||
            t.contains("kondisi ponsel") || t.contains("status hp") || t.contains("cek hp") ||
            t.contains("kondisi markas") || t.contains("diagnostik")

        if (askJam) {
            val now = SimpleDateFormat("HH.mm", Locale("id")).format(Date())
            return "Sekarang jam $now, $panggilan."
        }
        if (askTanggal) {
            val now = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id")).format(Date())
            return "Hari ini $now, $panggilan."
        }
        if (askKondisi || askBaterai || askRam || askStorage || askJaringan) {
            if (askKondisi) return summary(d, panggilan)
            val parts = buildList {
                if (askBaterai) add(if (d.charging) "baterai ${d.batteryPct} persen, lagi diisi, santai $panggilan" else "baterai ${d.batteryPct} persen, $panggilan")
                if (askRam) add("RAM terpakai ${d.ramUsedPct} persen")
                if (askStorage) add("penyimpanan sisa ${"%.1f".format(d.storageFreeGb)} gigabyte")
                if (askJaringan) add("jaringan ${d.network.lowercase()}")
            }
            return "${parts.joinToString(", ")}, $panggilan."
        }
        return null
    }
}
