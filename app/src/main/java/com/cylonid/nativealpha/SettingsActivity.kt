package com.cylonid.nativealpha

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cylonid.nativealpha.ui.settings.SettingsScreen
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NativeAlphaTheme {
                SettingsScreen(
                    onNavigateBack = { finish() },
                )
            }
        }
    }
}
