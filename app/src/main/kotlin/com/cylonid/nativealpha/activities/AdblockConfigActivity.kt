package com.cylonid.nativealpha.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cylonid.nativealpha.ui.adblock.AdblockConfigScreen
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme

class AdblockConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NativeAlphaTheme {
                AdblockConfigScreen(
                    onNavigateBack = { finish() },
                )
            }
        }
    }
}
