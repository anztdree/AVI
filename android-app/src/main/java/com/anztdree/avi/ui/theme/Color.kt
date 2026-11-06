package com.anztdree.avi.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Palet lengkap satu tema "Mark" HUD AVI */
data class AviPalette(
    val bg: Color,
    val panel: Color,
    val panelLight: Color,
    val accent: Color,
    val accentSoft: Color,
    val gold: Color,
    val goldSoft: Color,
    val text: Color,
    val textDim: Color,
    val danger: Color,
    val userBubble: Color,
    val userBorder: Color,
    val aviBubble: Color,
    val aviBorder: Color,
    val coreGlow: Color
)

/** 4 tema Mark — MK-I cyan/emas (default), MK-II Hot Rod merah, MK-III Aureus emas,
 *  MK-IV Eidolon hijau-hologram (K19 — kelas lab Jarvis) */
val MarkPalettes: Map<String, AviPalette> = mapOf(
    "MK1" to AviPalette(
        bg = Color(0xFF070B12),
        panel = Color(0xFF0D141F),
        panelLight = Color(0xFF141E2E),
        accent = Color(0xFF22D3EE),
        accentSoft = Color(0xFF67E8F9),
        gold = Color(0xFFF5B54A),
        goldSoft = Color(0xFFFFD98A),
        text = Color(0xFFE8EEF5),
        textDim = Color(0xFF8A97A8),
        danger = Color(0xFFF87171),
        userBubble = Color(0xFF251D0E),
        userBorder = Color(0xFF8A6A2F),
        aviBubble = Color(0xFF0C2833),
        aviBorder = Color(0xFF1B5E70),
        coreGlow = Color(0xFF7DF3FF)
    ),
    "MK2" to AviPalette(
        bg = Color(0xFF0C0708),
        panel = Color(0xFF1A0D0F),
        panelLight = Color(0xFF241114),
        accent = Color(0xFFFF3B30),
        accentSoft = Color(0xFFFF6B61),
        gold = Color(0xFFF5B54A),
        goldSoft = Color(0xFFFFD98A),
        text = Color(0xFFE8EEF5),
        textDim = Color(0xFF8A97A8),
        danger = Color(0xFFF87171),
        userBubble = Color(0xFF24140F),
        userBorder = Color(0xFF8A4A2F),
        aviBubble = Color(0xFF2A0E0C),
        aviBorder = Color(0xFF7A2A22),
        coreGlow = Color(0xFFFF8A80)
    ),
    "MK3" to AviPalette(
        bg = Color(0xFF0B0A05),
        panel = Color(0xFF171407),
        panelLight = Color(0xFF201B0A),
        accent = Color(0xFFF5B54A),
        accentSoft = Color(0xFFFFD98A),
        gold = Color(0xFFFFE082),
        goldSoft = Color(0xFFFFF3C4),
        text = Color(0xFFE8EEF5),
        textDim = Color(0xFF8A97A8),
        danger = Color(0xFFF87171),
        userBubble = Color(0xFF221B08),
        userBorder = Color(0xFF8A7430),
        aviBubble = Color(0xFF1F1A06),
        aviBorder = Color(0xFF7A6828),
        coreGlow = Color(0xFFFFE9A8)
    ),
    "MK4" to AviPalette(
        bg = Color(0xFF050A07),
        panel = Color(0xFF0B1712),
        panelLight = Color(0xFF12241C),
        accent = Color(0xFF34D399),
        accentSoft = Color(0xFF6EE7B7),
        gold = Color(0xFFB8E356),
        goldSoft = Color(0xFFDCF5A0),
        text = Color(0xFFE5F2EA),
        textDim = Color(0xFF86988D),
        danger = Color(0xFFF87171),
        userBubble = Color(0xFF12200A),
        userBorder = Color(0xFF5F7A2F),
        aviBubble = Color(0xFF0A2419),
        aviBorder = Color(0xFF1F5E45),
        coreGlow = Color(0xFF9DF5C8)
    )
)

val LocalAviPalette = staticCompositionLocalOf<AviPalette> { MarkPalettes.getValue("MK1") }
