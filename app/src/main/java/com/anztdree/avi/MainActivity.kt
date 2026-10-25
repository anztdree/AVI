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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.anztdree.avi.ui.BootScreen
import com.anztdree.avi.ui.ChatScreen
import com.anztdree.avi.ui.SettingsScreen
import com.anztdree.avi.ui.theme.AviTheme
import com.anztdree.avi.ui.theme.LocalAviPalette

class MainActivity : ComponentActivity() {

    private val vm: AviViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.createStt()
        setContent {
            val settings by vm.settings.collectAsState()
            var booted by remember { mutableStateOf(false) }

            AviTheme(mark = settings.themeMark) {
                val palette = LocalAviPalette.current
                Surface(modifier = Modifier.fillMaxSize(), color = palette.bg) {
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
                }

                // Boot sequence sinematik (bisa dimatikan di Pusat Kendali)
                if (!booted && settings.bootEnabled && !settings.powerSave) {
                    BootScreen(palette = palette, onFinish = { booted = true })
                }
            }
        }
    }
}
