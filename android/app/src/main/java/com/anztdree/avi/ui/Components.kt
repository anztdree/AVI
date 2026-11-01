package com.anztdree.avi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.anztdree.avi.ui.theme.AviAviBorder
import com.anztdree.avi.ui.theme.AviAviBubble
import com.anztdree.avi.ui.theme.AviCyan
import com.anztdree.avi.ui.theme.AviDanger
import com.anztdree.avi.ui.theme.AviGold
import com.anztdree.avi.ui.theme.AviGoldSoft
import com.anztdree.avi.ui.theme.AviPanel
import com.anztdree.avi.ui.theme.AviText
import com.anztdree.avi.ui.theme.AviTextDim
import com.anztdree.avi.ui.theme.AviUserBorder
import com.anztdree.avi.ui.theme.AviUserBubble

/** Label status kecil bergaya HUD */
@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Gelembung pesan user / AVI */
@Composable
fun MessageBubble(
    role: String,
    text: String,
    imageUrl: String?,
    streaming: Boolean = false
) {
    val isUser = role == "user"
    val bubbleBg = if (isUser) AviUserBubble else AviAviBubble
    val bubbleBorder = if (isUser) AviUserBorder else AviAviBorder

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleBg)
                .border(1.dp, bubbleBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = if (streaming) "$text ▌" else text,
                color = if (isUser) AviGoldSoft else AviText,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }

        if (!imageUrl.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            GeneratedImageCard(imageUrl)
        }
    }
}

/** Kartu gambar hasil AI via Pollinations */
@Composable
fun GeneratedImageCard(url: String) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = "Gambar dibuat AVI",
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .height(210.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AviCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        loading = {
            Box(
                modifier = Modifier.background(AviPanel),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AviCyan, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            }
        },
        error = {
            Box(
                modifier = Modifier.background(AviPanel),
                contentAlignment = Alignment.Center
            ) {
                Text("Gagal memuat gambar", color = AviTextDim, fontSize = 12.sp)
            }
        }
    )
}

/** Banner error sementara */
@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AviDanger.copy(alpha = 0.12f))
            .border(1.dp, AviDanger.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onDismiss() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⚠", color = AviDanger, fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            message,
            color = AviDanger,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            maxLines = 3
        )
    }
}

/** Kartu panduan setup API key (tampil kalau otak AVI belum dipasang) */
@Composable
fun SetupCard(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AviPanel)
            .border(1.dp, AviGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            "⚡ Pasang otak AVI dulu, Pak",
            color = AviGold,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "AVI butuh satu API key Google Gemini (gratis) untuk berpikir. Caranya:\n" +
                "1. Buka aistudio.google.com/apikey\n" +
                "2. Login akun Google → Create API key\n" +
                "3. Salin kuncinya → buka Pengaturan di AVI → tempel\n\n" +
                "Sekali pasang, AVI langsung siap ngobrol!",
            color = AviTextDim,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                        )
                    } catch (_: Exception) {
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AviGold, contentColor = Color(0xFF0A0E14))
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Buka AI Studio", fontSize = 12.sp)
            }
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = AviCyan, contentColor = Color(0xFF0A0E14))
            ) {
                Text("Ke Pengaturan", fontSize = 12.sp)
            }
        }
    }
}

/** Chip kecil untuk info di bar atas */
@Composable
fun TopBarChip(
    label: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.10f))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun IconButtonCompat(
    icon: @Composable () -> Unit,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) { icon() }
}
