package com.cylonid.nativealpha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.ui.about.AboutScreen
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDynamicColor by remember {
                mutableStateOf(
                    runCatching { DataManager.getInstance().settings.isDynamicColor }.getOrDefault(false),
                )
            }

            NativeAlphaTheme(dynamicColor = isDynamicColor) {
                AboutScreen(onNavigateBack = { finish() })
            }
        }
    }
}
