package com.cylonid.nativealpha.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
                java.io.RandomAccessFile(this, "rw").use { it.setLength(55L * 1024 * 1024) }
                setLastModified(1000L)
            }
        val file2 =
            File(cacheDir, "new_file.pdf").apply {
                java.io.RandomAccessFile(this, "rw").use { it.setLength(1024) }
                setLastModified(5000L)
            }

        assertTrue(file1.exists())
        assertTrue(file2.exists())

        PdfDownloader.cleanOldCacheFiles(context)

        // Exceeding 50MB limit: oldest file is deleted, newest file retained
        assertFalse(file1.exists())
        assertTrue(file2.exists())

        // Cleanup
        file2.delete()
    }

    @Test
    fun testIsSameOrigin() {
        // Same origin: scheme, host, port match
        assertTrue(PdfDownloader.isSameOrigin("https://example.com/doc.pdf", "https://example.com/other.pdf"))
        assertTrue(PdfDownloader.isSameOrigin("https://example.com:443/doc.pdf", "https://example.com/other.pdf"))
        assertTrue(PdfDownloader.isSameOrigin("http://example.com:80/doc.pdf", "http://example.com/other.pdf"))
        assertTrue(PdfDownloader.isSameOrigin("https://example.com:8443/doc.pdf", "https://example.com:8443/sub"))

        // Different scheme
        assertFalse(PdfDownloader.isSameOrigin("http://example.com/doc.pdf", "https://example.com/doc.pdf"))

        // Different host
        assertFalse(PdfDownloader.isSameOrigin("https://example.com/doc.pdf", "https://cdn.example.com/doc.pdf"))
        assertFalse(PdfDownloader.isSameOrigin("https://example.com/doc.pdf", "https://other.com/doc.pdf"))

        // Different port
        assertFalse(PdfDownloader.isSameOrigin("https://example.com:8443/doc.pdf", "https://example.com:8444/doc.pdf"))
        assertFalse(PdfDownloader.isSameOrigin("https://example.com:8443/doc.pdf", "https://example.com/doc.pdf"))

        // Malformed URLs
        assertFalse(PdfDownloader.isSameOrigin("not_a_url", "https://example.com"))
    }
}
