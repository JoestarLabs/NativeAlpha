package com.cylonid.nativealpha

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import com.cylonid.nativealpha.ui.webapp.WebAppSettingsScreen
import com.cylonid.nativealpha.util.Const

class WebAppSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val webappId = intent.getIntExtra(Const.INTENT_WEBAPPID, -1)

        setContent {
            NativeAlphaTheme {
                WebAppSettingsScreen(
                    webappId = webappId,
                    onNavigateBack = { finish() },
                )
            }
        }
    }
}
