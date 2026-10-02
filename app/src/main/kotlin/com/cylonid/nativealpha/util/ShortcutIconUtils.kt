package com.cylonid.nativealpha.util

import android.content.Context
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.cylonid.nativealpha.R
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import kotlin.math.abs

object ShortcutIconUtils {
    private const val ICONS_DIR = "webapp_icons"

    @JvmStatic
    fun getIconsDir(context: Context): File {
        val dir = File(context.filesDir, ICONS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    @JvmStatic
    fun getIconFile(
        context: Context,
        webAppId: Int,
    ): File = File(getIconsDir(context), "$webAppId.png")

    @JvmStatic
    fun saveIcon(
        context: Context,
        webAppId: Int,
        bitmap: Bitmap,
    ): Boolean =
        runCatching {
            val file = getIconFile(context, webAppId)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
            true
        }.getOrDefault(false)

    @JvmStatic
    fun getIcon(
        context: Context,
        webAppId: Int,
    ): Bitmap? {
        val file = getIconFile(context, webAppId)
        if (!file.exists() || file.length() == 0L) {
            return null
        }
        return runCatching {
            BitmapFactory.decodeFile(file.absolutePath)
        }.getOrNull()
    }

    @JvmStatic
    fun hasIcon(
        context: Context,
        webAppId: Int,
    ): Boolean {
        val file = getIconFile(context, webAppId)
        return file.exists() && file.length() > 0
    }

    @JvmStatic
    fun deleteIcon(
        context: Context,
        webAppId: Int,
    ): Boolean =
        runCatching {
            val file = getIconFile(context, webAppId)
            if (file.exists()) file.delete() else true
        }.getOrDefault(false)

    @JvmStatic
    fun deleteShortcuts(
        removableWebAppIds: List<Int>,
        context: Context,
    ) {
        val manager = context.getSystemService(ShortcutManager::class.java)
        if (manager != null) {
            for (info in manager.pinnedShortcuts) {
                val id =
                    info.intent?.getIntExtra(Const.INTENT_WEBAPPID, -1) ?: -1
                if (removableWebAppIds.contains(id)) {
                    manager.disableShortcuts(
                        listOf(info.id),
                        context.getString(R.string.webapp_already_deleted),
                    )
                }
            }
        }
        for (id in removableWebAppIds) {
            deleteIcon(context, id)
        }
    }

    @JvmStatic
    fun getWidthFromIcon(sizeString: String): Int {
        var xIndex = sizeString.indexOf("x")
        if (xIndex == -1) xIndex = sizeString.indexOf("×")
        if (xIndex == -1) xIndex = sizeString.indexOf("*")

        if (xIndex == -1) return 1
        val width = sizeString.substring(0, xIndex)

        return width.toIntOrNull() ?: 1
    }

    @JvmStatic
    fun getFallbackIconUrls(baseUrl: String): List<String> {
        val host =
            runCatching {
                val uri =
                    URI(
                        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                            "https://$baseUrl"
                        } else {
                            baseUrl
                        },
                    )
                uri.host?.removePrefix("www.")
            }.getOrNull() ?: return emptyList()

        if (host.isBlank()) return emptyList()

        return listOf(
            "https://$host/favicon.ico",
            "https://icons.duckduckgo.com/ip3/$host.ico",
            "https://www.google.com/s2/favicons?domain=$host&sz=128",
        )
    }

    @JvmStatic
    fun createMonogramIcon(
        label: String,
        sizePx: Int = 192,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val colors =
            intArrayOf(
                0xFF1E88E5.toInt(), // Blue
                0xFF43A047.toInt(), // Green
                0xFFE53935.toInt(), // Red
                0xFFFB8C00.toInt(), // Orange
                0xFF8E24AA.toInt(), // Purple
                0xFF00ACC1.toInt(), // Cyan
                0xFF3949AB.toInt(), // Indigo
                0xFFD81B60.toInt(), // Pink
                0xFF00897B.toInt(), // Teal
            )
        val cleanLabel = label.trim().ifEmpty { "W" }
        val colorIndex = abs(cleanLabel.hashCode()) % colors.size
        val bgColor = colors[colorIndex]

        val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColor
                style = Paint.Style.FILL
            }

        val radius = sizePx / 2f
        canvas.drawCircle(radius, radius, radius, bgPaint)

        val initial = cleanLabel.first().uppercaseChar().toString()
        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = sizePx * 0.48f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

        val bounds = Rect()
        textPaint.getTextBounds(initial, 0, initial.length, bounds)
        val yOffset = bounds.exactCenterY()
        canvas.drawText(initial, radius, radius - yOffset, textPaint)

        return bitmap
    }
}
