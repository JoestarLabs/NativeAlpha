package com.cylonid.nativealpha

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.ui.settings.SettingsScreen
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDynamicColor by remember {
                mutableStateOf(
                    runCatching { DataManager.getInstance().settings.isDynamicColor }.getOrDefault(false),
                )
            }

            NativeAlphaTheme(dynamicColor = isDynamicColor) {
                SettingsScreen(
                    onNavigateBack = { finish() },
                    onDynamicColorChanged = { isDynamicColor = it },
                )
            }
        }
    }
}
