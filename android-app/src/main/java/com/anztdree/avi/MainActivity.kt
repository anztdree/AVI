package com.anztdree.avi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anztdree.avi.ui.BootScreen
import com.anztdree.avi.ui.ChatScreen
import com.anztdree.avi.ui.SettingsScreen
import com.anztdree.avi.ui.WizardScreen
import com.anztdree.avi.ui.theme.AviTheme
import com.anztdree.avi.ui.theme.LocalAviPalette

class MainActivity : ComponentActivity() {

    private val vm: AviViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.createStt()
        setContent {
            val settings by vm.settings.collectAsState()
            val ready by vm.settingsReady.collectAsState()
            var booted by remember { mutableStateOf(false) }

            AviTheme(mark = settings.themeMark) {
                val palette = LocalAviPalette.current
                Surface(modifier = Modifier.fillMaxSize(), color = palette.bg) {

                    // P50 — tahan satu momen sampai DataStore kirim setelan
                    // pertama, supaya keputusan wizard vs utama akurat (tanpa
                    // wizard berkedip tiap launch). Emblem sudah tampil di
                    // splash sistem/windowBackground sebelum titik ini.
                    if (!ready) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(palette.bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(com.anztdree.avi.R.drawable.splash_emblem),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(140.dp)
                            )
                        }
                    } else if (settings.wizardDone) {
                        var showSettings by remember { mutableStateOf(false) }
                        // Transisi hologram antar layar
                        AnimatedContent(
                            targetState = showSettings,
                            transitionSpec = {
                                if (targetState) {
                                    (slideInHorizontally(tween(320)) { it / 3 } + fadeIn(tween(320))) togetherWith
                                        (slideOutHorizontally(tween(320)) { -it / 6 } + fadeOut(tween(320)))
                                } else {
                                    (slideInHorizontally(tween(320)) { -it / 3 } + fadeIn(tween(320))) togetherWith
                                        (slideOutHorizontally(tween(320)) { it / 6 } + fadeOut(tween(320)))
                                }
                            },
                            label = "nav-avi"
                        ) { show ->
                            if (show) SettingsScreen(vm = vm, onBack = { showSettings = false })
                            else ChatScreen(vm = vm, onOpenSettings = { showSettings = true })
                        }

                        // Boot sequence sinematik (bisa dimatikan di Pusat Kendali)
                        if (!booted && settings.bootEnabled && !settings.powerSave) {
                            BootScreen(palette = palette, onFinish = { booted = true }, mark = settings.themeMark)
                        }
                    } else {
                        // P50 — WIZARD PERTAMA KALI INSTALL
                        WizardScreen(vm = vm)
                    }
                }
            }
        }
    }
}
