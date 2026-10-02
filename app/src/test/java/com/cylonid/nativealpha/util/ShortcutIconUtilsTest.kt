package com.cylonid.nativealpha.util

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShortcutIconUtilsTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSaveGetAndHasIcon() {
        val webAppId = 999
        ShortcutIconUtils.deleteIcon(context, webAppId)
        assertFalse(ShortcutIconUtils.hasIcon(context, webAppId))
        assertNull(ShortcutIconUtils.getIcon(context, webAppId))

        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val saved = ShortcutIconUtils.saveIcon(context, webAppId, bitmap)
        assertTrue("Icon should be saved successfully", saved)

        assertTrue(ShortcutIconUtils.hasIcon(context, webAppId))
        val loaded = ShortcutIconUtils.getIcon(context, webAppId)
        assertNotNull("Loaded bitmap should not be null", loaded)
        assertEquals(64, loaded!!.width)
        assertEquals(64, loaded.height)

        val deleted = ShortcutIconUtils.deleteIcon(context, webAppId)
        assertTrue("Icon should be deleted successfully", deleted)
        assertFalse(ShortcutIconUtils.hasIcon(context, webAppId))
        assertNull(ShortcutIconUtils.getIcon(context, webAppId))
    }

    @Test
    fun testCreateMonogramIcon() {
        val monogram = ShortcutIconUtils.createMonogramIcon("GitHub", 128)
        assertNotNull(monogram)
        assertEquals(128, monogram.width)
        assertEquals(128, monogram.height)

        val emptyMonogram = ShortcutIconUtils.createMonogramIcon("", 96)
        assertNotNull(emptyMonogram)
        assertEquals(96, emptyMonogram.width)
        assertEquals(96, emptyMonogram.height)
    }

    @Test
    fun testGetWidthFromIcon() {
        assertEquals(192, ShortcutIconUtils.getWidthFromIcon("192x192"))
        assertEquals(48, ShortcutIconUtils.getWidthFromIcon("48*48"))
        assertEquals(64, ShortcutIconUtils.getWidthFromIcon("64×64"))
        assertEquals(1, ShortcutIconUtils.getWidthFromIcon("any"))
        assertEquals(1, ShortcutIconUtils.getWidthFromIcon(""))
    }

    @Test
    fun testGetFallbackIconUrls() {
        val urls = ShortcutIconUtils.getFallbackIconUrls("https://www.github.com/login")
        assertEquals(3, urls.size)
        assertTrue(urls.contains("https://github.com/favicon.ico"))
        assertTrue(urls.contains("https://icons.duckduckgo.com/ip3/github.com.ico"))
        assertTrue(urls.contains("https://www.google.com/s2/favicons?domain=github.com&sz=128"))

        val simpleUrls = ShortcutIconUtils.getFallbackIconUrls("example.org")
        assertEquals(3, simpleUrls.size)
        assertTrue(simpleUrls.contains("https://example.org/favicon.ico"))

        val emptyUrls = ShortcutIconUtils.getFallbackIconUrls("")
        assertTrue(emptyUrls.isEmpty())
    }
}
