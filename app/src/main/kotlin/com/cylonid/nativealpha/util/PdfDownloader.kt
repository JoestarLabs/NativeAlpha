package com.cylonid.nativealpha.util

import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object PdfDownloader {
    private const val MAX_REDIRECTS = 5
    private const val CONNECT_TIMEOUT_MS = 15000
    private const val READ_TIMEOUT_MS = 30000
    private const val MAX_CACHE_SIZE_BYTES = 50 * 1024 * 1024L // 50 MB
    private const val CACHE_SUBDIR = "pdf_cache"

    fun getPdfCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_SUBDIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun cleanOldCacheFiles(context: Context) {
        try {
            val dir = getPdfCacheDir(context)
            val files = dir.listFiles()?.filter { it.isFile } ?: return
            val totalSize = files.sumOf { it.length() }
            if (totalSize > MAX_CACHE_SIZE_BYTES) {
                // Delete oldest files first
                files
                    .sortedBy { it.lastModified() }
                    .forEach { file ->
                        file.delete()
                    }
            }
        } catch (_: Exception) {
            // Best effort cache trimming
        }
    }

    suspend fun downloadPdf(
        context: Context,
        urlStr: String,
        preferredFileName: String? = null,
        userAgent: String? = null,
        onProgress: ((bytesDownloaded: Long, totalBytes: Long) -> Unit)? = null,
    ): Result<File> =
        withContext(Dispatchers.IO) {
            cleanOldCacheFiles(context)

            // Handle local content / file URIs directly
            if (urlStr.startsWith("content://") || urlStr.startsWith("file://")) {
                return@withContext copyLocalUriToCache(context, Uri.parse(urlStr), preferredFileName)
            }

            val cacheDir = getPdfCacheDir(context)
            val safeFileName = sanitizeFileName(preferredFileName ?: deriveFileNameFromUrl(urlStr))
            val targetFile = File(cacheDir, "${System.currentTimeMillis()}_$safeFileName")
            val tempFile = File(cacheDir, "${targetFile.name}.tmp")

            var connection: HttpURLConnection? = null
            var currentUrl = urlStr
            var redirectCount = 0

            try {
                while (redirectCount < MAX_REDIRECTS) {
                    val url = URL(currentUrl)
                    connection =
                        (url.openConnection() as HttpURLConnection).apply {
                            connectTimeout = CONNECT_TIMEOUT_MS
                            readTimeout = READ_TIMEOUT_MS
                            instanceFollowRedirects = false
                            requestMethod = "GET"

                            if (!userAgent.isNullOrBlank()) {
                                setRequestProperty("User-Agent", userAgent)
                            }

                            val cookie = CookieManager.getInstance().getCookie(currentUrl)
                            if (!cookie.isNullOrBlank()) {
                                setRequestProperty("Cookie", cookie)
                            }
                            setRequestProperty("Accept", "application/pdf,*/*")
                        }

                    val responseCode = connection.responseCode
                    if (responseCode in 300..399) {
                        val location = connection.getHeaderField("Location")
                        if (location.isNullOrBlank()) {
                            throw IOException("Redirect without Location header (HTTP $responseCode)")
                        }
                        currentUrl =
                            if (location.startsWith("http://") || location.startsWith("https://")) {
                                location
                            } else {
                                URL(URL(currentUrl), location).toString()
                            }
                        connection.disconnect()
                        redirectCount++
                    } else if (responseCode in 200..299) {
                        break
                    } else {
                        throw IOException("HTTP error response code: $responseCode")
                    }
                }

                if (redirectCount >= MAX_REDIRECTS) {
                    throw IOException("Too many redirects: $redirectCount")
                }

                val conn = connection ?: throw IOException("Failed to establish connection")
                val totalBytes = conn.contentLengthLong

                conn.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        copyStreamWithProgress(input, output, totalBytes, onProgress)
                    }
                }

                if (tempFile.renameTo(targetFile)) {
                    Result.success(targetFile)
                } else {
                    // Fallback copy if renameTo fails
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                    Result.success(targetFile)
                }
            } catch (e: Exception) {
                tempFile.delete()
                targetFile.delete()
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }

    private fun copyStreamWithProgress(
        input: InputStream,
        output: FileOutputStream,
        totalBytes: Long,
        onProgress: ((bytesDownloaded: Long, totalBytes: Long) -> Unit)?,
    ) {
        val buffer = ByteArray(8192)
        var bytesDownloaded = 0L
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            bytesDownloaded += bytesRead
            onProgress?.invoke(bytesDownloaded, totalBytes)
        }
        output.flush()
    }

    private fun copyLocalUriToCache(
        context: Context,
        uri: Uri,
        fileName: String?,
    ): Result<File> {
        return try {
            val cacheDir = getPdfCacheDir(context)
            val name = sanitizeFileName(fileName ?: "document.pdf")
            val targetFile = File(cacheDir, "${System.currentTimeMillis()}_$name")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(IOException("Could not open input stream for URI: $uri"))

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deriveFileNameFromUrl(url: String): String =
        try {
            val clean = url.substringBefore('?').substringBefore('#')
            val lastSegment = clean.substringAfterLast('/')
            if (lastSegment.isNotBlank() && lastSegment.contains('.')) {
                lastSegment
            } else {
                "document.pdf"
            }
        } catch (_: Exception) {
            "document.pdf"
        }

    fun sanitizeFileName(name: String): String {
        val sanitized = name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        return if (sanitized.lowercase().endsWith(".pdf")) {
            sanitized
        } else {
            "$sanitized.pdf"
        }
    }
}
