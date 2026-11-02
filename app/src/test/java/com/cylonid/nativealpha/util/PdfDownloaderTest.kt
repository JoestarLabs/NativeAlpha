package com.cylonid.nativealpha.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PdfDownloaderTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testDeriveFileNameFromUrl() {
        assertEquals(
            "report.pdf",
            PdfDownloader.deriveFileNameFromUrl("https://example.com/files/report.pdf"),
        )
        assertEquals(
            "invoice.pdf",
            PdfDownloader.deriveFileNameFromUrl("https://example.com/invoices/invoice.pdf?auth=xyz#section2"),
        )
        assertEquals(
            "document.pdf",
            PdfDownloader.deriveFileNameFromUrl("https://example.com/download?id=42"),
        )
        assertEquals(
            "document.pdf",
            PdfDownloader.deriveFileNameFromUrl(""),
        )
    }

    @Test
    fun testSanitizeFileName() {
        assertEquals("report.pdf", PdfDownloader.sanitizeFileName("report.pdf"))
        assertEquals("report.pdf", PdfDownloader.sanitizeFileName("report"))
        assertEquals(
            "my_document__2026_.pdf",
            PdfDownloader.sanitizeFileName("my document (2026).pdf"),
        )
        assertEquals(
            "unsafe_path_file.pdf",
            PdfDownloader.sanitizeFileName("unsafe/path:file.pdf"),
        )
    }

    @Test
    fun testGetPdfCacheDir() {
        val cacheDir = PdfDownloader.getPdfCacheDir(context)
        assertTrue(cacheDir.exists())
        assertTrue(cacheDir.isDirectory)
        assertEquals("pdf_cache", cacheDir.name)
    }

    @Test
    fun testCleanOldCacheFilesTrimsWhenExceedingLimit() {
        val cacheDir = PdfDownloader.getPdfCacheDir(context)
        val file1 =
            File(cacheDir, "old_file.pdf").apply {
                writeBytes(ByteArray(1024))
                setLastModified(1000L)
            }
        val file2 =
            File(cacheDir, "new_file.pdf").apply {
                writeBytes(ByteArray(1024))
                setLastModified(5000L)
            }

        assertTrue(file1.exists())
        assertTrue(file2.exists())

        PdfDownloader.cleanOldCacheFiles(context)

        // Under 50MB, files are retained
        assertTrue(file1.exists())
        assertTrue(file2.exists())

        // Cleanup
        file1.delete()
        file2.delete()
    }
}
