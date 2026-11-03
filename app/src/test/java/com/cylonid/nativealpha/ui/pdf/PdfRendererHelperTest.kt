package com.cylonid.nativealpha.ui.pdf

import android.graphics.pdf.PdfRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.io.File
import java.io.FileOutputStream
import java.util.Collections
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@Implements(PdfRenderer::class)
class ShadowTestPdfRenderer {
    @Implementation
    fun getPageCount(): Int = 1
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [ShadowTestPdfRenderer::class])
class PdfRendererHelperTest {
    private fun createMinimalPdfFile(): File {
        val pdfBytes =
            (
                "%PDF-1.4\n" +
                    "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                    "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                    "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Resources <<>> >>\nendobj\n" +
                    "xref\n0 4\n" +
                    "0000000000 65535 f \n" +
                    "0000000009 00000 n \n" +
                    "0000000058 00000 n \n" +
                    "0000000115 00000 n \n" +
                    "trailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n202\n%%EOF\n"
            ).toByteArray(Charsets.US_ASCII)

        val tempFile = File.createTempFile("sample_test_", ".pdf")
        tempFile.deleteOnExit()
        FileOutputStream(tempFile).use { output ->
            output.write(pdfBytes)
        }
        return tempFile
    }

    @Test
    fun testLifecycleLockAndClosedState() {
        val file = createMinimalPdfFile()
        val helper = PdfRendererHelper(file)

        val initialCount = helper.pageCount
        assertEquals(1, initialCount)

        helper.close()
        assertEquals(0, helper.pageCount)

        // Idempotent close
        helper.close()
        assertEquals(0, helper.pageCount)

        file.delete()
    }

    @Test
    fun testConcurrentPageCountAndClose() {
        val file = createMinimalPdfFile()
        val helper = PdfRendererHelper(file)

        val latch = CountDownLatch(1)
        val threads = mutableListOf<Thread>()
        val pageCounts = mutableListOf<Int>()
        val workerErrors = Collections.synchronizedList(mutableListOf<Throwable>())

        // Spawn threads reading pageCount
        for (i in 0 until 5) {
            threads.add(
                thread {
                    try {
                        latch.await()
                        val count = helper.pageCount
                        synchronized(pageCounts) {
                            pageCounts.add(count)
                        }
                    } catch (t: Throwable) {
                        workerErrors.add(t)
                    }
                },
            )
        }

        // Spawn a thread closing helper
        threads.add(
            thread {
                try {
                    latch.await()
                    helper.close()
                } catch (t: Throwable) {
                    workerErrors.add(t)
                }
            },
        )

        latch.countDown()
        threads.forEach {
            it.join(5000)
            assertFalse("Thread ${it.name} should have terminated", it.isAlive)
        }

        if (workerErrors.isNotEmpty()) {
            throw AssertionError("Worker thread encountered error", workerErrors.first())
        }

        assertEquals("Expected exactly 5 reader results", 5, pageCounts.size)

        // After all threads finish, pageCount must be 0
        assertEquals(0, helper.pageCount)
        // All recorded pageCounts during concurrent execution must be either 1 (before close) or 0 (after close)
        synchronized(pageCounts) {
            pageCounts.forEach { count ->
                assertTrue("Page count should be 0 or 1, but was $count", count == 0 || count == 1)
            }
        }

        file.delete()
    }
}
