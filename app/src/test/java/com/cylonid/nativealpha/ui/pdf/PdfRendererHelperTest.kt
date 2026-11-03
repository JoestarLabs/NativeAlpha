package com.cylonid.nativealpha.ui.pdf

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PdfRendererHelperTest {
    private fun createMinimalPdfFile(): File {
        val pdfBytes =
            """%PDF-1.4
1 0 obj
<< /Type /Catalog /Pages 2 0 R >>
endobj
2 0 obj
<< /Type /Pages /Kids [3 0 R] /Count 1 >>
endobj
3 0 obj
<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] >>
endobj
xref
0 4
0000000000 65535 f
0000000009 00000 n
0000000058 00000 n
0000000115 00000 n
trailer
<< /Size 4 /Root 1 0 R >>
startxref
190
%%EOF
""".toByteArray()

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
        org.junit.Assert.assertTrue("Initial page count should be non-negative", initialCount >= 0)

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

        // Spawn threads reading pageCount
        for (i in 0 until 5) {
            threads.add(
                thread {
                    latch.await()
                    val count = helper.pageCount
                    synchronized(pageCounts) {
                        pageCounts.add(count)
                    }
                },
            )
        }

        // Spawn a thread closing helper
        threads.add(
            thread {
                latch.await()
                helper.close()
            },
        )

        latch.countDown()
        threads.forEach { it.join(5000) }

        // After all threads finish, pageCount must be 0
        assertEquals(0, helper.pageCount)
        // All recorded pageCounts during concurrent execution must be either 1 (before close) or 0 (after close)
        synchronized(pageCounts) {
            pageCounts.forEach { count ->
                org.junit.Assert.assertTrue("Page count should be 0 or 1, but was $count", count == 0 || count == 1)
            }
        }

        file.delete()
    }
}
