package com.anztdree.avi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AviColorScheme = darkColorScheme(
    primary = AviCyan,
    onPrimary = AviBg,
    secondary = AviGold,
    onSecondary = AviBg,
    background = AviBg,
    onBackground = AviText,
    surface = AviPanel,
    onSurface = AviText,
    surfaceVariant = AviPanelLight,
    onSurfaceVariant = AviTextDim,
    error = AviDanger,
    outline = AviAviBorder
)

@Composable
fun AviTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AviColorScheme,
        content = content
    )
}
