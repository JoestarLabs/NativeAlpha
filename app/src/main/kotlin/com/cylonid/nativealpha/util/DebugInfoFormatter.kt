package com.cylonid.nativealpha.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.webkit.WebViewCompat
import com.cylonid.nativealpha.BuildConfig
import com.cylonid.nativealpha.model.DataManager
import java.io.IOException

/**
 * Sanitized diagnostic snapshot of the app, device, OS, WebView, and aggregate settings for bug reports.
 */
data class DebugInfo(
    val appVersionName: String,
    val appVersionCode: Int,
    val buildFlavor: String,
    val buildType: String,
    val androidRelease: String,
    val sdkInt: Int,
    val securityPatch: String,
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val buildDisplay: String,
    val romVersion: String?,
    val webViewPackage: String?,
    val webViewVersion: String?,
    val appLocale: String,
    val themeMode: String,
    val dynamicColor: Boolean,
    val ignoringBatteryOptimizations: Boolean?,
    val activeWebAppCount: Int,
    val sandboxedWebAppCount: Int,
    val globalAdblockEnabled: Boolean,
    val globalBiometricEnabled: Boolean,
)

/**
 * Collects and formats non-PII diagnostic device, WebView, and app information for clipboard sharing.
 */
object DebugInfoFormatter {
    /**
     * Collects live diagnostic metadata from [context], [Build], [WebViewCompat], and [DataManager].
     * Safe to invoke off the main thread because it may query `getprop` for OEM ROM version.
     */
    fun collect(
        context: Context,
        getProperty: (String) -> String? = ::readSystemProperty,
    ): DebugInfo {
        val localeTag = resolveLocaleTag(context)
        val webViewPkg = runCatching { WebViewCompat.getCurrentWebViewPackage(context) }.getOrNull()
        val powerManager = context.getSystemService(PowerManager::class.java)
        val ignoringBattery =
            runCatching {
                powerManager?.isIgnoringBatteryOptimizations(context.packageName)
            }.getOrNull()

        val dataManager = DataManager.getInstance()
        val settings = runCatching { dataManager.settings }.getOrNull()
        val activeApps = runCatching { dataManager.activeWebsites }.getOrDefault(emptyList())
        val themeMode =
            when (settings?.themeId) {
                0 -> "System"
                1 -> "Dark"
                2 -> "Light"
                null -> "Unknown"
                else -> "Custom(${settings.themeId})"
            }

        return DebugInfo(
            appVersionName = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE,
            buildFlavor = BuildConfig.FLAVOR,
            buildType = BuildConfig.BUILD_TYPE,
            androidRelease = Build.VERSION.RELEASE.orEmpty(),
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH.orEmpty(),
            manufacturer = Build.MANUFACTURER.orEmpty(),
            brand = Build.BRAND.orEmpty(),
            model = Build.MODEL.orEmpty(),
            device = Build.DEVICE.orEmpty(),
            buildDisplay = Build.DISPLAY.orEmpty(),
            romVersion = detectRomVersion(Build.MANUFACTURER.orEmpty(), getProperty),
            webViewPackage = webViewPkg?.packageName,
            webViewVersion = webViewPkg?.versionName,
            appLocale = localeTag,
            themeMode = themeMode,
            dynamicColor = settings?.isDynamicColor ?: false,
            ignoringBatteryOptimizations = ignoringBattery,
            activeWebAppCount = activeApps.size,
            sandboxedWebAppCount = activeApps.count { it.isUseContainer },
            globalAdblockEnabled = settings?.globalWebApp?.isUseAdblock ?: false,
            globalBiometricEnabled = settings?.globalWebApp?.isBiometricProtection ?: false,
        )
    }

    /**
     * Formats [info] as a clean multi-line plain-text block suitable for pasting into issue trackers.
     */
    fun format(info: DebugInfo): String =
        buildString {
            appendLine("App: Native Alpha ${info.appVersionName} (${info.appVersionCode}, ${info.buildFlavor}/${info.buildType})")
            appendLine("Android: ${info.androidRelease} (API ${info.sdkInt})")
            if (info.securityPatch.isNotBlank()) {
                appendLine("Security Patch: ${info.securityPatch}")
            }
            appendLine("Device: ${info.manufacturer} ${info.model} (${info.brand}/${info.device})")
            if (!info.romVersion.isNullOrBlank()) {
                appendLine("ROM: ${info.romVersion}")
            }
            if (info.buildDisplay.isNotBlank()) {
                appendLine("Build: ${info.buildDisplay}")
            }
            if (!info.webViewPackage.isNullOrBlank()) {
                val versionSuffix =
                    info.webViewVersion
                        ?.takeIf { it.isNotBlank() }
                        ?.let { " ($it)" }
                        .orEmpty()
                appendLine("WebView: ${info.webViewPackage}$versionSuffix")
            }
            appendLine("Locale: ${info.appLocale} | Theme: ${info.themeMode} (DynamicColor=${info.dynamicColor})")
            if (info.ignoringBatteryOptimizations != null) {
                appendLine("Battery Optimization Ignored: ${info.ignoringBatteryOptimizations}")
            }
            append(
                "WebApps: ${info.activeWebAppCount} active (${info.sandboxedWebAppCount} sandboxed) | " +
                    "Global Adblock=${info.globalAdblockEnabled}, Biometric=${info.globalBiometricEnabled}",
            )
        }

    /**
     * Detects common OEM skin / custom ROM version strings from standard system properties.
     */
    internal fun detectRomVersion(
        manufacturer: String,
        getProperty: (String) -> String?,
    ): String? {
        val mfr = manufacturer.trim().lowercase()

        val oneUi = formatOneUiVersion(getProperty("ro.build.version.oneui"))
        if (oneUi != null) return oneUi
        if (mfr == "samsung") {
            getProperty("ro.build.PDA")?.takeIf { it.isNotBlank() }?.let { return "One UI ($it)" }
        }

        getProperty("ro.mi.os.version.name")?.takeIf { it.isNotBlank() }?.let { return "HyperOS $it" }
        getProperty("ro.miui.ui.version.name")?.takeIf { it.isNotBlank() }?.let { return "MIUI $it" }

        getProperty("ro.build.version.oplusrom")?.takeIf { it.isNotBlank() }?.let {
            val label = if (mfr == "oneplus") "OxygenOS" else "ColorOS"
            return "$label $it"
        }

        getProperty("ro.nothing.version.id")?.takeIf { it.isNotBlank() }?.let { return "Nothing OS $it" }
        getProperty("ro.lineage.version")?.takeIf { it.isNotBlank() }?.let { return "LineageOS $it" }

        return null
    }

    /**
     * Converts Samsung's integer `ro.build.version.oneui` property (e.g. `"80000"`, `"60101"`)
     * into a human-readable string (e.g. `"One UI 8.0"`, `"One UI 6.1.1"`).
     */
    internal fun formatOneUiVersion(rawVersion: String?): String? {
        val code = rawVersion?.trim()?.toIntOrNull() ?: return null
        if (code < 10000) return null
        val major = code / 10000
        val minor = (code % 10000) / 100
        val patch = code % 100
        return if (patch > 0) "One UI $major.$minor.$patch" else "One UI $major.$minor"
    }

    private fun resolveLocaleTag(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val appLocales = context.getSystemService(LocaleManager::class.java)?.applicationLocales
            if (appLocales != null && !appLocales.isEmpty) {
                return appLocales.toLanguageTags()
            }
        }
        val locales = context.resources.configuration.locales
        return if (!locales.isEmpty) locales[0].toLanguageTag() else "default"
    }

    private fun readSystemProperty(key: String): String? =
        try {
            val process =
                ProcessBuilder("getprop", key)
                    .redirectErrorStream(true)
                    .start()
            val output = process.inputStream.bufferedReader().use { it.readText().trim() }
            if (process.waitFor() == 0 && output.isNotEmpty()) output else null
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            null
        }
}
