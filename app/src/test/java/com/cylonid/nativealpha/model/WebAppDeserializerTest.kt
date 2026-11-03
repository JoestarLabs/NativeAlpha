package com.cylonid.nativealpha.model

import com.cylonid.nativealpha.model.deserializer.WebAppDeserializer
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebAppDeserializerTest {
    private lateinit var gson: com.google.gson.Gson

    @Before
    fun setup() {
        gson =
            GsonBuilder()
                .registerTypeAdapter(WebApp::class.java, WebAppDeserializer())
                .create()
    }

    @Test
    fun testDeserializePdfViewerDefaultsAndExplicitValues() {
        // Missing key -> defaults to true
        val jsonMissing = """{"baseUrl": "https://example.com", "ID": 1}"""
        val webAppMissing = gson.fromJson(jsonMissing, WebApp::class.java)
        assertNotNull(webAppMissing)
        assertTrue(webAppMissing.isUseBuiltinPdfViewer)

        // Explicit null -> defaults to true
        val jsonNull = """{"baseUrl": "https://example.com", "ID": 2, "isUseBuiltinPdfViewer": null}"""
        val webAppNull = gson.fromJson(jsonNull, WebApp::class.java)
        assertNotNull(webAppNull)
        assertTrue(webAppNull.isUseBuiltinPdfViewer)

        // Explicit false -> preserves false
        val jsonFalse = """{"baseUrl": "https://example.com", "ID": 3, "isUseBuiltinPdfViewer": false}"""
        val webAppFalse = gson.fromJson(jsonFalse, WebApp::class.java)
        assertNotNull(webAppFalse)
        assertFalse(webAppFalse.isUseBuiltinPdfViewer)

        // Explicit true -> preserves true
        val jsonTrue = """{"baseUrl": "https://example.com", "ID": 4, "isUseBuiltinPdfViewer": true}"""
        val webAppTrue = gson.fromJson(jsonTrue, WebApp::class.java)
        assertNotNull(webAppTrue)
        assertTrue(webAppTrue.isUseBuiltinPdfViewer)
    }

    @Test
    fun testLegacyAdblockMigration() {
        val jsonGlobal = """{"baseUrl": "https://example.com", "ID": 2147483647}"""
        val webAppGlobal = gson.fromJson(jsonGlobal, WebApp::class.java)
        assertNotNull(webAppGlobal)
        assertTrue(webAppGlobal.adBlockSettings.isNotEmpty())

        val jsonNormal = """{"baseUrl": "https://example.com", "ID": 10}"""
        val webAppNormal = gson.fromJson(jsonNormal, WebApp::class.java)
        assertNotNull(webAppNormal)
        assertEquals(0, webAppNormal.adBlockSettings.size)
    }
}
