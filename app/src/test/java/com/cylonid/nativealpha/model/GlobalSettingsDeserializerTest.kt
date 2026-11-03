package com.cylonid.nativealpha.model

import com.cylonid.nativealpha.model.deserializer.GlobalSettingsDeserializer
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GlobalSettingsDeserializerTest {
    private lateinit var gson: com.google.gson.Gson

    @Before
    fun setup() {
        gson =
            GsonBuilder()
                .registerTypeAdapter(GlobalSettings::class.java, GlobalSettingsDeserializer())
                .create()
    }

    @Test
    fun testDeserializeWhenGlobalWebAppIsNull() {
        val json =
            """
            {
                "isClearCache": true,
                "globalWebApp": null,
                "webAppMenuStyle": 2
            }
            """.trimIndent()

        val settings = gson.fromJson(json, GlobalSettings::class.java)
        assertNotNull(settings)
        assertTrue(settings.isClearCache)
        assertNotNull(settings.globalWebApp)
        assertEquals(2, settings.webAppMenuStyle)
    }

    @Test
    fun testDeserializeWhenGlobalWebAppIsMissing() {
        val json =
            """
            {
                "isClearCache": false,
                "webAppMenuStyle": 1
            }
            """.trimIndent()

        val settings = gson.fromJson(json, GlobalSettings::class.java)
        assertNotNull(settings)
        assertNotNull(settings.globalWebApp)
        assertEquals(1, settings.webAppMenuStyle)
    }

    @Test
    fun testDeserializeMalformedJsonReturnsDefault() {
        val json =
            """
            "just_a_string_not_an_object"
            """.trimIndent()

        val settings = gson.fromJson(json, GlobalSettings::class.java)
        assertNotNull(settings)
        assertNotNull(settings.globalWebApp)
    }

    @Test
    fun testDeserializePdfViewerDefaultsAndExplicitValues() {
        // Missing key -> defaults to true
        val jsonMissing = "{}"
        val settingsMissing = gson.fromJson(jsonMissing, GlobalSettings::class.java)
        assertTrue(settingsMissing.isUseBuiltinPdfViewer)

        // Explicit null -> defaults to true
        val jsonNull = """{"isUseBuiltinPdfViewer": null}"""
        val settingsNull = gson.fromJson(jsonNull, GlobalSettings::class.java)
        assertTrue(settingsNull.isUseBuiltinPdfViewer)

        // Explicit false -> preserves false
        val jsonFalse = """{"isUseBuiltinPdfViewer": false}"""
        val settingsFalse = gson.fromJson(jsonFalse, GlobalSettings::class.java)
        org.junit.Assert.assertFalse(settingsFalse.isUseBuiltinPdfViewer)

        // Explicit true -> preserves true
        val jsonTrue = """{"isUseBuiltinPdfViewer": true}"""
        val settingsTrue = gson.fromJson(jsonTrue, GlobalSettings::class.java)
        assertTrue(settingsTrue.isUseBuiltinPdfViewer)
    }
}
