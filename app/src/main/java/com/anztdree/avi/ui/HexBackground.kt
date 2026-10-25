package com.anztdree.avi.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.anztdree.avi.ui.theme.AviPalette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Latar hex grid samar ala HUD — dioptimalkan untuk kecepatan:
 * - Path heksagon digambar SEKALI ke layer GPU; parallax memakai graphicsLayer
 *   (translate murni GPU, TIDAK menggambar ulang path tiap frame).
 * - Overlay gradien statis pada canvas terpisah (sekali gambar).
 * - Loop smoothing tidur saat posisi stabil (hemat baterai/frame).
 */
@Composable
fun HexGridBackground(palette: AviPalette, enabled: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var targetX by remember { mutableStateOf(0f) } // -1..1
    var targetY by remember { mutableStateOf(0f) } // -1..1
    var offsetX by remember { mutableStateOf(0f) } // smoothing -1..1
    var offsetY by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    val maxOffPx = remember(density) { with(density) { 6.dp.toPx() } }

    if (enabled) {
        DisposableEffect(context) {
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val sensor = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.values.size >= 2) {
                        targetX = (event.values[0] / 10f).coerceIn(-1f, 1f)
                        targetY = (event.values[1] / 10f).coerceIn(-1f, 1f)
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            if (sm != null && sensor != null) {
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            }
            onDispose { sm?.unregisterListener(listener) }
        }
    }
    LaunchedEffect(enabled) {
        if (!enabled) {
            targetX = 0f; targetY = 0f
            offsetX = 0f; offsetY = 0f
            return@LaunchedEffect
        }
        var cx = 0f
        var cy = 0f
        while (true) {
            val nx = cx + (targetX - cx) * 0.06f
            val ny = cy + (targetY - cy) * 0.06f
            val settled = abs(nx - cx) < 0.0006f && abs(ny - cy) < 0.0006f &&
                abs(targetX - cx) < 0.0006f && abs(targetY - cy) < 0.0006f
            if (settled) {
                // posisi stabil — tidur panjang, nol frame terbuang
                cx = targetX; cy = targetY
                if (offsetX != cx || offsetY != cy) {
                    offsetX = cx
                    offsetY = cy
                }
                delay(150)
            } else {
                cx = nx; cy = ny
                offsetX = cx
                offsetY = cy
                delay(16)
            }
        }
    }

    val hexPath = remember(canvasSize, maxOffPx) {
        if (canvasSize == IntSize.Zero) {
            null
        } else {
            buildHexPath(
                w = canvasSize.width.toFloat(),
                h = canvasSize.height.toFloat(),
                r = (maxOffPx * 3f).coerceAtLeast(20f)
            )
        }
    }

    Box(modifier) {
        // ---- lapis hex: digambar sekali, digeser via GPU layer ----
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = offsetX * maxOffPx
                    translationY = offsetY * maxOffPx
                }
                .onSizeChanged { canvasSize = it }
        ) {
            hexPath?.let { p ->
                drawPath(
                    path = p,
                    color = palette.accent.copy(alpha = 0.06f),
                    style = Stroke(width = 1f)
                )
            }
        }
        // ---- overlay gradien statis agar teks terbaca ----
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.bg.copy(alpha = 0.10f),
                        palette.bg.copy(alpha = 0.78f)
                    ),
                    center = center,
                    radius = size.minDimension * 0.85f
                ),
                topLeft = Offset.Zero,
                size = size
            )
        }
    }
}

/** Lattice heksagonal pointy-top dalam satu Path (efisien: 1 drawPath) */
private fun buildHexPath(w: Float, h: Float, r: Float): Path {
    val path = Path()
    val hexW = sqrt(3f) * r
    val hexH = 2f * r
    var row = 0
    var y = -hexH
    while (y < h + hexH) {
        val rowOffset = if (row % 2 == 0) 0f else hexW / 2f
        var x = -hexW * 1.5f + rowOffset
        while (x < w + hexW) {
            addHex(path, x, y, r)
            x += hexW
        }
        y += hexH * 0.75f
        row++
    }
    return path
}

private fun addHex(path: Path, cx: Float, cy: Float, r: Float) {
    for (i in 0 until 6) {
        val ang = (60.0 * i - 30.0) * Math.PI / 180.0
        val px = cx + r * cos(ang).toFloat()
        val py = cy + r * sin(ang).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
}
