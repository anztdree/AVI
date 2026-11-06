package com.anztdree.avi.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anztdree.avi.R
import com.anztdree.avi.ui.theme.AviCode
import com.anztdree.avi.ui.theme.AviHead
import com.anztdree.avi.ui.theme.AviHud
import com.anztdree.avi.ui.theme.AviPalette
import com.anztdree.avi.ui.theme.LocalAviPalette

/**
 * K21 — WIDGET PUSAT KENDALI (versi bersih):
 * Tab misi dirampingkan jadi 4 (OTAK · SUARA · GAYA · SISTEM) — tab TAMPILAN
 * dihapus total atas arahan pemilik (detail tampilan bukan prioritas), slider
 * kecepatan/nada suara dibuang, memori dirombak jadi INGATAN. Widget di sini
 * tetap satu keluarga dgn layar obrolan (panel kaca, tipografi HUD).
 */

/** Tab misi Pusat Kendali — K22: tab AGENT masuk (mesin function calling) */
enum class HudTab { OTAK, AGENT, SUARA, GAYA, SISTEM }

private val HudTab.icon: ImageVector
    get() = when (this) {
        HudTab.OTAK -> Icons.Filled.Psychology
        HudTab.AGENT -> Icons.Filled.SmartToy
        HudTab.SUARA -> Icons.Filled.GraphicEq
        HudTab.GAYA -> Icons.Filled.Face
        HudTab.SISTEM -> Icons.Filled.Tune
    }

// ============================================================
// Bar navigasi tab — indikator meluncur halus antar tab
// ============================================================

@Composable
fun HudTabBar(
    selected: HudTab,
    onSelect: (HudTab) -> Unit,
    hapticsEnabled: Boolean,
    palette: AviPalette = LocalAviPalette.current
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val tabW = maxWidth / HudTab.entries.size
        val idx = HudTab.entries.indexOf(selected)
        val indicatorX by animateDpAsState(
            targetValue = tabW * idx,
            animationSpec = tween(280),
            label = "tabInd"
        )
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.panel.copy(alpha = 0.92f))
                .border(1.dp, palette.accent.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
        ) {
            Row(Modifier.fillMaxWidth()) {
                HudTab.entries.forEach { tab ->
                    val sel = tab == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (hapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSelect(tab)
                            }
                            .padding(vertical = 9.dp)
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.name,
                            tint = if (sel) palette.accent else palette.textDim,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            tab.name,
                            color = if (sel) palette.accent else palette.textDim,
                            fontSize = 9.sp,
                            fontFamily = AviHud,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 1.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            // indikator tab aktif — garis aksen meluncur
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = indicatorX)
                    .width(tabW)
                    .height(2.dp)
                    .background(palette.accent)
            )
        }
    }
}

// ============================================================
// K22 — Sakelar HUD mini (perkakas agen aktif/nonaktif)
// ============================================================

@Composable
fun HudToggle(
    on: Boolean,
    enabled: Boolean = true,
    palette: AviPalette = LocalAviPalette.current,
    onClick: () -> Unit
) {
    val trackW = 38.dp
    val knob = 15.dp
    val offX by animateDpAsState(
        targetValue = if (on) trackW - knob - 3.dp else 3.dp,
        animationSpec = tween(180),
        label = "tog"
    )
    Box(
        Modifier
            .width(trackW)
            .height(22.dp)
            .clip(RoundedCornerShape(50))
            .background(
                when {
                    on && enabled -> palette.accent.copy(alpha = 0.32f)
                    else -> palette.panelLight.copy(alpha = 0.6f)
                }
            )
            .border(
                1.dp,
                if (on && enabled) palette.accent else palette.textDim.copy(alpha = 0.35f),
                RoundedCornerShape(50)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .offset(x = offX)
                .size(knob)
                .clip(CircleShape)
                .background(if (on && enabled) palette.accent else palette.textDim.copy(alpha = 0.7f))
        )
    }
}

// ============================================================
// Inti plasma header — cincin tick berputar + inti bernapas
// ============================================================

@Composable
fun CoreBadge(palette: AviPalette = LocalAviPalette.current, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "coreBadge")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "spin"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(modifier.size(46.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(46.dp)) {
            drawCircle(
                color = palette.accent.copy(alpha = 0.30f),
                radius = size.minDimension / 2f - 1.dp.toPx(),
                style = Stroke(width = 1.2f)
            )
            rotate(spin) {
                repeat(12) { i ->
                    rotate(i * 30f) {
                        drawLine(
                            color = if (i % 3 == 0) palette.gold else palette.accent,
                            start = Offset(center.x, 2.dp.toPx()),
                            end = Offset(center.x, 5.dp.toPx()),
                            strokeWidth = 1.6f,
                            alpha = 0.75f
                        )
                    }
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.tex_core),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(34.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                .clip(CircleShape)
                .border(1.dp, palette.gold.copy(alpha = 0.55f), CircleShape)
        )
    }
}

// ============================================================
// Petak status kecil — strip status hidup di header
// ============================================================

@Composable
fun StatTile(
    value: String,
    label: String,
    active: Boolean,
    palette: AviPalette = LocalAviPalette.current,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.panel.copy(alpha = 0.82f))
            .border(
                1.dp,
                if (active) palette.accent.copy(alpha = 0.35f) else palette.panelLight.copy(alpha = 0.7f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (active) palette.accent else palette.textDim.copy(alpha = 0.5f))
            )
            Spacer(Modifier.width(4.dp))
            Text(
                value,
                color = if (active) palette.text else palette.textDim,
                fontSize = 11.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Text(
            label,
            color = palette.textDim,
            fontSize = 7.5.sp,
            fontFamily = AviHud,
            letterSpacing = 0.8.sp,
            maxLines = 1
        )
    }
}

// ============================================================
// Judul panel dengan badge kanan
// ============================================================

@Composable
fun PanelTitle(
    title: String,
    badge: String? = null,
    palette: AviPalette = LocalAviPalette.current
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(Modifier.width(3.dp).height(13.dp).background(palette.accent))
        Spacer(Modifier.width(7.dp))
        Text(
            title,
            color = palette.text,
            fontSize = 12.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        if (badge != null) {
            Spacer(Modifier.width(7.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(palette.accent.copy(alpha = 0.13f))
                    .border(1.dp, palette.accent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    badge,
                    color = palette.accent,
                    fontSize = 8.sp,
                    fontFamily = AviHud,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

// ============================================================
// Pemilih pil (chip) gaya HUD
// ============================================================

@Composable
fun HudChip(
    text: String,
    selected: Boolean,
    palette: AviPalette = LocalAviPalette.current,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) palette.accent.copy(alpha = 0.18f) else palette.panelLight.copy(alpha = 0.55f))
            .border(1.dp, if (selected) palette.accent else palette.panelLight, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text,
            color = if (selected) palette.accent else palette.textDim,
            fontSize = 12.sp
        )
    }
}

// ============================================================
// Pemisah gradien tipis
// ============================================================

@Composable
fun HudDivider(palette: AviPalette = LocalAviPalette.current) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, palette.accent.copy(alpha = 0.25f), Color.Transparent)
                )
            )
    )
}

// ============================================================
// Baris info label-nilai
// ============================================================

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    palette: AviPalette = LocalAviPalette.current
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = palette.textDim,
            fontSize = 11.sp,
            fontFamily = AviHud,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            color = valueColor ?: palette.text,
            fontSize = 11.sp,
            fontFamily = AviCode,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

// ============================================================
// Tombol aksi cepat — petak ikon utk grid
// ============================================================

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    danger: Boolean = false,
    palette: AviPalette = LocalAviPalette.current,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = if (danger) palette.danger else palette.accent
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.07f))
            .border(1.dp, color.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            color = color,
            fontSize = 9.sp,
            fontFamily = AviHud,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================
// Kartu modul antrean (fitur masa depan, badge SEGERA)
// ============================================================

@Composable
fun ModuleCard(
    icon: ImageVector,
    name: String,
    desc: String,
    palette: AviPalette = LocalAviPalette.current,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.panelLight.copy(alpha = 0.45f))
            .border(1.dp, palette.textDim.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = palette.textDim, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                name,
                color = palette.text,
                fontSize = 11.sp,
                fontFamily = AviHead,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(desc, color = palette.textDim, fontSize = 9.sp, lineHeight = 12.sp, maxLines = 2)
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(palette.gold.copy(alpha = 0.14f))
                .border(1.dp, palette.gold.copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                "SEGERA",
                color = palette.gold,
                fontSize = 7.5.sp,
                fontFamily = AviHud,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

