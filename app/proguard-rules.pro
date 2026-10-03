# Preserve line numbers and source file for readable crash traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Data Models used with Gson reflection
-keep class com.cylonid.nativealpha.model.** { *; }
-keepclassmembers class com.cylonid.nativealpha.model.** { *; }

# WebView JavaScript Interfaces
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# JNI Bindings for Adblock Core
-keep class io.github.edsuns.adblockclient.** { *; }
-keepclassmembers class io.github.edsuns.adblockclient.** { *; }

