package com.anztdree.avi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Tema HUD AVI — parameter [mark] memilih palet Mark (MK1/MK2/MK3), fallback MK1.
 * Default "MK1" supaya pemanggil lama tanpa argumen tetap kompilasi.
 */
@Composable
fun AviTheme(mark: String = "MK1", content: @Composable () -> Unit) {
    val palette = MarkPalettes[mark] ?: MarkPalettes.getValue("MK1")
    CompositionLocalProvider(LocalAviPalette provides palette) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = palette.accent,
                secondary = palette.gold,
                background = palette.bg,
                surface = palette.panel,
                error = palette.danger,
                outline = palette.panelLight,
                onBackground = palette.text,
                onSurface = palette.text,
                onPrimary = palette.bg,
                onSecondary = palette.bg,
                onSurfaceVariant = palette.textDim
            ),
            content = content
        )
    }
}
