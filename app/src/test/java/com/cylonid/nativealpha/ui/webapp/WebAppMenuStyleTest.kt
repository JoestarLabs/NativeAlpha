package com.cylonid.nativealpha.ui.webapp

import com.cylonid.nativealpha.model.GlobalSettings
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class WebAppMenuStyleTest {
    @Test
    fun testFromIdMapping() {
        assertEquals(WebAppMenuStyle.BOTTOM_SHEET, WebAppMenuStyle.fromId(0))
        assertEquals(WebAppMenuStyle.FLOATING_DIALOG, WebAppMenuStyle.fromId(1))
        assertEquals(WebAppMenuStyle.CLASSIC_DROPDOWN, WebAppMenuStyle.fromId(2))
    }

    @Test
    fun testFromIdFallbackToDefault() {
        assertEquals(WebAppMenuStyle.BOTTOM_SHEET, WebAppMenuStyle.fromId(-1))
        assertEquals(WebAppMenuStyle.BOTTOM_SHEET, WebAppMenuStyle.fromId(999))
    }

    @Test
    fun testGlobalSettingsDefaultValue() {
        val settings = GlobalSettings()
        assertEquals(WebAppMenuStyle.BOTTOM_SHEET.id, settings.webAppMenuStyle)
        assertEquals(WebAppMenuStyle.BOTTOM_SHEET, WebAppMenuStyle.fromId(settings.webAppMenuStyle))
    }

    @Test
    fun testGlobalSettingsSerializationRoundTrip() {
        val gson = Gson()
        val original = GlobalSettings(webAppMenuStyle = WebAppMenuStyle.FLOATING_DIALOG.id)
        val json = gson.toJson(original)

        val restored = gson.fromJson(json, GlobalSettings::class.java)
        assertEquals(WebAppMenuStyle.FLOATING_DIALOG.id, restored.webAppMenuStyle)
        assertEquals(WebAppMenuStyle.FLOATING_DIALOG, WebAppMenuStyle.fromId(restored.webAppMenuStyle))
    }

    @Test
    fun testLegacyJsonWithoutMenuStyleDefaultsGracefully() {
        val gson = Gson()
        // Simulate a legacy backup/preferences JSON string created before webAppMenuStyle was introduced
        val legacyJson = """{"isClearCache":true,"themeId":1,"clear_cookies":false}"""

        val restored = gson.fromJson(legacyJson, GlobalSettings::class.java)
        assertEquals(
            "Legacy JSON missing webAppMenuStyle should gracefully default to 0 (BOTTOM_SHEET)",
            WebAppMenuStyle.BOTTOM_SHEET.id,
            restored.webAppMenuStyle,
        )
        assertEquals(WebAppMenuStyle.BOTTOM_SHEET, WebAppMenuStyle.fromId(restored.webAppMenuStyle))
    }
}
