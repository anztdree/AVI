package com.anztdree.avi.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.anztdree.avi.R

/**
 * K12 — TIPOGRAFI PREMIUM AVI (bundel di APK, bekerja offline penuh):
 *  · AviDisplay — Orbitron (variabel): judul sinematik / identitas
 *  · AviHud     — Share Tech Mono: teks telemetri & label HUD
 *  · AviHead    — Rajdhani Bold: judul seksi, angka besar
 *  · AviBody    — Exo 2 (variabel): isi pesan & teks panjang
 *  · AviCode    — JetBrains Mono (variabel): nama model / data teknis
 */
val AviDisplay = FontFamily(
    Font(R.font.orbitron_variable, FontWeight.Normal),
    Font(R.font.orbitron_variable, FontWeight.Bold)
)

val AviHud = FontFamily(Font(R.font.share_tech_mono))

val AviHead = FontFamily(
    Font(R.font.rajdhani_medium, FontWeight.Normal),
    Font(R.font.rajdhani_bold, FontWeight.Bold)
)

val AviBody = FontFamily(
    Font(R.font.exo2_variable, FontWeight.Normal),
    Font(R.font.exo2_variable, FontWeight.Medium)
)

val AviCode = FontFamily(
    Font(R.font.jetbrains_mono_variable, FontWeight.Normal)
)
