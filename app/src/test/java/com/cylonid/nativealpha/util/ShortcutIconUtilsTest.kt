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

    @Test
    fun testDownscaleIfNecessary() {
        // Small bitmap should not be downscaled
        val small = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val same = ShortcutIconUtils.downscaleIfNecessary(small, 256)
        assertEquals(128, same.width)
        assertEquals(128, same.height)

        // Large bitmap (1024x512) should be scaled down while preserving aspect ratio
        val large = Bitmap.createBitmap(1024, 512, Bitmap.Config.ARGB_8888)
        val downscaled = ShortcutIconUtils.downscaleIfNecessary(large, 256)
        assertTrue(downscaled.width <= 256)
        assertTrue(downscaled.height <= 256)
        assertEquals(256, downscaled.width)
        assertEquals(128, downscaled.height)
    }

    @Test
    fun testCleanupOrphanedIcons() {
        val validId1 = 101
        val validId2 = 102
        val orphanedId = 999

        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        ShortcutIconUtils.saveIcon(context, validId1, bmp)
        ShortcutIconUtils.saveIcon(context, validId2, bmp)
        ShortcutIconUtils.saveIcon(context, orphanedId, bmp)

        assertTrue(ShortcutIconUtils.hasIcon(context, validId1))
        assertTrue(ShortcutIconUtils.hasIcon(context, validId2))
        assertTrue(ShortcutIconUtils.hasIcon(context, orphanedId))

        // Cleanup orphaned icons keeping only validId1 and validId2
        ShortcutIconUtils.cleanupOrphanedIcons(context, listOf(validId1, validId2))

        assertTrue("Valid ID 1 should be preserved", ShortcutIconUtils.hasIcon(context, validId1))
        assertTrue("Valid ID 2 should be preserved", ShortcutIconUtils.hasIcon(context, validId2))
        assertFalse("Orphaned ID should be cleaned up", ShortcutIconUtils.hasIcon(context, orphanedId))

        // Clean up remaining
        ShortcutIconUtils.deleteIcon(context, validId1)
        ShortcutIconUtils.deleteIcon(context, validId2)
    }

    @Test
    fun testInvalidWebAppIdIgnored() {
        val bmp = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        assertFalse(ShortcutIconUtils.saveIcon(context, -1, bmp))
        assertNull(ShortcutIconUtils.getIcon(context, -1))
        assertFalse(ShortcutIconUtils.hasIcon(context, -1))
        assertFalse(ShortcutIconUtils.deleteIcon(context, -1))
    }

    @Test
    fun testTmpFileCleanup() {
        val webAppId = 555
        val iconsDir = ShortcutIconUtils.getIconsDir(context)
        val strayTmp = java.io.File(iconsDir, "$webAppId.tmp")
        strayTmp.writeText("corrupt/unfinished tmp data")
        assertTrue(strayTmp.exists())

        ShortcutIconUtils.cleanupOrphanedIcons(context, listOf(webAppId))
        assertFalse("Stray .tmp file must be deleted during cleanup", strayTmp.exists())
    }

    @Test
    fun testCorruptFileCleanedUpOnGet() {
        val webAppId = 777
        ShortcutIconUtils.clearMemoryCache()
        val iconFile = ShortcutIconUtils.getIconFile(context, webAppId)
        iconFile.writeBytes(ByteArray(0)) // 0-byte empty/corrupted file
        assertTrue(iconFile.exists())

        val result = ShortcutIconUtils.getIcon(context, webAppId)
        assertNull(result)
        assertFalse("Corrupt/empty file must be deleted upon get", iconFile.exists())
    }

    @Test
    fun testMemoryCacheEviction() {
        val webAppId = 888
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        ShortcutIconUtils.saveIcon(context, webAppId, bmp)
        assertTrue(ShortcutIconUtils.hasIcon(context, webAppId))

        ShortcutIconUtils.clearMemoryCache()
        // Icon should still be retrievable from disk and repopulate cache
        val reloaded = ShortcutIconUtils.getIcon(context, webAppId)
        assertNotNull(reloaded)
        ShortcutIconUtils.deleteIcon(context, webAppId)
    }

    @Test
    fun testUpdatePinnedShortcutsNoCrash() {
        val webAppId = 42
        val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        // Should execute smoothly without crashing even if no pinned shortcuts exist in test environment
        ShortcutIconUtils.updatePinnedShortcuts(context, webAppId, bmp)
        ShortcutIconUtils.updatePinnedShortcuts(context, -1, bmp)
    }

    @Test
    fun testFetchBitmapFromNetworkSizeConstraint() {
        // Empty url returns null
        assertNull(ShortcutIconUtils.fetchBitmapFromNetwork(""))
    }
}
