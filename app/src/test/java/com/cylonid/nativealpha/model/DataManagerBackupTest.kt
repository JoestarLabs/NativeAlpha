package com.cylonid.nativealpha.model

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.util.Const
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataManagerBackupTest {
    private lateinit var context: Context
    private lateinit var dataManager: DataManager
    private val tempFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataManager = DataManager.getInstance()
        dataManager.websites.clear()
        dataManager.saveWebAppData()
    }

    @After
    fun tearDown() {
        for (file in tempFiles) {
            if (file.exists()) {
                file.delete()
            }
        }
        dataManager.websites.clear()
        dataManager.saveWebAppData()
    }

    @Test
    fun testSaveAndLoadSharedPreferencesFileRoundTrip() {
        // Setup initial rich WebApps and GlobalSettings
        val webApp1 =
            WebApp("https://reddit.com", 1, Const.getDefaultAdBlockConfig()).apply {
                title = "Reddit"
                isOverrideGlobalSettings = true
                isAllowJs = true
                containerId = 2
                isUseContainer = true
                isForceDarkMode = true
            }

        val webApp2 =
            WebApp("https://github.com", 2, Const.getDefaultAdBlockConfig()).apply {
                title = "GitHub"
                isUseAdblock = true
                order = 2
                isBiometricProtection = true
            }

        dataManager.websites.clear()
        dataManager.websites.add(webApp1)
        dataManager.websites.add(webApp2)
        dataManager.saveWebAppData()

        dataManager.settings.isClearCache = true
        dataManager.settings.clear_cookies = true
        dataManager.settings.themeId = 1
        dataManager.saveGlobalSettings()

        // Create temporary backup file
        val tempFile = File.createTempFile("backup_roundtrip", ".b64", context.cacheDir)
        tempFiles.add(tempFile)
        val uri = Uri.fromFile(tempFile)

        // 1. Export backup to file URI
        val saveSuccess = dataManager.saveSharedPreferencesToFile(uri)
        assertTrue("Backup save to URI should succeed", saveSuccess)
        assertTrue("Backup file should be non-empty", tempFile.length() > 0)

        // 2. Wipe current in-memory & SharedPreferences state
        dataManager.websites.clear()
        dataManager.saveWebAppData()
        dataManager.settings = GlobalSettings()
        dataManager.saveGlobalSettings()

        assertEquals("Websites should be empty after wiping", 0, dataManager.websites.size)
        assertFalse("ClearCache should be reset to default false", dataManager.settings.isClearCache)

        // 3. Import backup from file URI
        val loadSuccess = dataManager.loadSharedPreferencesFromFile(uri)
        assertTrue("Backup restore from URI should succeed", loadSuccess)

        // 4. Reload app data from restored preferences
        dataManager.loadAppData()

        // 5. Verify restored state fidelity
        assertEquals("Should restore exactly 2 WebApps", 2, dataManager.websites.size)

        val restored1 = dataManager.getWebApp(0)
        assertNotNull("WebApp 1 should be restored", restored1)
        assertEquals("Reddit", restored1.title)
        assertTrue("Override global settings should be true", restored1.isOverrideGlobalSettings)
        assertTrue("Allow JS should be true", restored1.isAllowJs)
        assertTrue("Use container should be true", restored1.isUseContainer)
        assertEquals(2, restored1.containerId)
        assertTrue("Force dark mode should be true", restored1.isForceDarkMode)

        val restored2 = dataManager.getWebApp(1)
        assertNotNull("WebApp 2 should be restored", restored2)
        assertEquals("GitHub", restored2.title)
        assertTrue("Biometric protection should be true", restored2.isBiometricProtection)

        assertTrue("Restored clearCache should be true", dataManager.settings.isClearCache)
        assertTrue("Restored clear_cookies should be true", dataManager.settings.clear_cookies)
        assertEquals(1, dataManager.settings.themeId)
    }

    @Test
    fun testStreamLevelBackupAndRestore() {
        val webApp =
            WebApp("https://example.com", 10, Const.getDefaultAdBlockConfig()).apply {
                title = "Example App"
                isEnableZooming = true
            }
        dataManager.websites.clear()
        dataManager.websites.add(webApp)
        dataManager.saveWebAppData()

        val outputStream = ByteArrayOutputStream()
        val saveSuccess = dataManager.saveSharedPreferences(outputStream)
        assertTrue("Stream save should succeed", saveSuccess)
        val backupBytes = outputStream.toByteArray()
        assertTrue("Output stream should contain backup bytes", backupBytes.isNotEmpty())

        // Clear state
        dataManager.websites.clear()
        dataManager.saveWebAppData()

        // Restore from stream
        val inputStream = ByteArrayInputStream(backupBytes)
        val loadSuccess = dataManager.loadSharedPreferences(inputStream)
        assertTrue("Stream load should succeed", loadSuccess)

        dataManager.loadAppData()
        assertEquals(1, dataManager.websites.size)
        assertEquals("Example App", dataManager.getWebApp(0).title)
        assertTrue(dataManager.getWebApp(0).isEnableZooming)
    }

    @Test
    fun testTamperedChecksumFailsToRestore() {
        val webApp =
            WebApp("https://secure.org", 5, Const.getDefaultAdBlockConfig()).apply {
                title = "Secure Site"
            }
        dataManager.websites.clear()
        dataManager.websites.add(webApp)
        dataManager.saveWebAppData()

        val outputStream = ByteArrayOutputStream()
        dataManager.saveSharedPreferences(outputStream)
        val originalBytes = outputStream.toByteArray()

        // Tamper with serialized bytes in the middle of payload
        val tamperedBytes = originalBytes.clone()
        if (tamperedBytes.size > 50) {
            tamperedBytes[45] = (tamperedBytes[45].toInt() xor 0xFF).toByte()
            tamperedBytes[46] = (tamperedBytes[46].toInt() xor 0xAA).toByte()
        }

        val tamperedStream = ByteArrayInputStream(tamperedBytes)
        val loadSuccess = dataManager.loadSharedPreferences(tamperedStream)
        assertFalse("Tampered backup must be rejected by checksum validation", loadSuccess)
    }

    @Test
    fun testCorruptedDataDoesNotWipeExistingSettings() {
        dataManager.settings.themeId = 42
        dataManager.saveGlobalSettings()

        val garbageBytes = "Not a valid java serialized base64 payload".toByteArray()
        val garbageStream = ByteArrayInputStream(garbageBytes)

        val loadSuccess = dataManager.loadSharedPreferences(garbageStream)
        assertFalse("Garbage input must return false", loadSuccess)

        // Existing settings must remain unaffected
        dataManager.loadAppData()
        assertEquals(42, dataManager.settings.themeId)
    }

    @Test
    fun testNonExistentFileUriReturnsFalseGracefully() {
        val nonExistentFile = File(context.cacheDir, "non_existent_file.b64")
        if (nonExistentFile.exists()) nonExistentFile.delete()
        val nonExistentUri = Uri.fromFile(nonExistentFile)

        val loadSuccess = dataManager.loadSharedPreferencesFromFile(nonExistentUri)
        assertFalse("Loading non-existent file URI should return false gracefully without crashing", loadSuccess)
    }
}
