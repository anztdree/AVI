package com.anztdree.avi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.anztdree.avi.ui.ChatScreen
import com.anztdree.avi.ui.SettingsScreen
import com.anztdree.avi.ui.theme.AviTheme

class MainActivity : ComponentActivity() {

    private val vm: AviViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.createStt()
        setContent {
            AviTheme {
                var showSettings by remember { mutableStateOf(false) }
                if (showSettings) {
                    SettingsScreen(vm = vm, onBack = { showSettings = false })
                } else {
                    ChatScreen(vm = vm, onOpenSettings = { showSettings = true })
                }
            }
        }
    }
}
