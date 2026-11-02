package com.cylonid.nativealpha.ui.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

interface PdfRendererEngine : AutoCloseable {
    val pageCount: Int

    suspend fun getPageDimensions(pageIndex: Int): Pair<Int, Int>

    suspend fun renderPage(
        pageIndex: Int,
        targetWidth: Int,
    ): Bitmap?
}

open class PdfRendererHelper(
    val file: File,
) : PdfRendererEngine {
    private val parcelFileDescriptor: ParcelFileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer: PdfRenderer =
        try {
            PdfRenderer(parcelFileDescriptor)
        } catch (t: Throwable) {
            try {
                parcelFileDescriptor.close()
            } catch (_: Exception) {
            }
            throw t
        }

    @Volatile
    private var isClosed = false

    override val pageCount: Int
        get() = if (isClosed) 0 else renderer.pageCount

    private val mutex = Mutex()
    private val dimensionCache = mutableMapOf<Int, Pair<Int, Int>>()

    private val maxCacheMemory =
        ((Runtime.getRuntime().maxMemory() / 8).toInt())
            .coerceIn(16 * 1024 * 1024, 48 * 1024 * 1024)

    private val bitmapCache =
        object : LruCache<Int, Bitmap>(maxCacheMemory) {
            override fun sizeOf(
                key: Int,
                value: Bitmap,
            ): Int = value.byteCount
        }

    override suspend fun getPageDimensions(pageIndex: Int): Pair<Int, Int> =
        withContext(Dispatchers.IO) {
            if (pageIndex < 0 || pageIndex >= pageCount) {
                return@withContext Pair(1, 1)
            }
            mutex.withLock {
                if (isClosed) return@withLock Pair(1, 1)
                dimensionCache[pageIndex]?.let { return@withLock it }
                val page = renderer.openPage(pageIndex)
                val dims =
                    try {
                        Pair(page.width, page.height)
                    } finally {
                        page.close()
                    }
                dimensionCache[pageIndex] = dims
                dims
            }
        }

    override suspend fun renderPage(
        pageIndex: Int,
        targetWidth: Int,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            if (pageIndex < 0 || pageIndex >= pageCount || targetWidth <= 0) {
                return@withContext null
            }

            bitmapCache.get(pageIndex)?.let { cached ->
                if (!cached.isRecycled) {
                    return@withContext cached
                }
            }

            mutex.withLock {
                if (isClosed) return@withLock null
                bitmapCache.get(pageIndex)?.let { cached ->
                    if (!cached.isRecycled) {
                        return@withLock cached
                    }
                }

                val page = renderer.openPage(pageIndex)
                try {
                    val pageW = page.width.coerceAtLeast(1)
                    val pageH = page.height.coerceAtLeast(1)
                    val targetHeight = ((targetWidth.toFloat() / pageW) * pageH).toInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmapCache.put(pageIndex, bitmap)
                    bitmap
                } finally {
                    page.close()
                }
            }
        }

    override fun close() {
        isClosed = true
        try {
            bitmapCache.evictAll()
        } catch (_: Exception) {
        }

        try {
            kotlinx.coroutines.runBlocking {
                mutex.withLock {
                    try {
                        renderer.close()
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
            try {
                renderer.close()
            } catch (_: Exception) {
            }
        } finally {
            try {
                parcelFileDescriptor.close()
            } catch (_: Exception) {
            }
        }
    }
}
