# <img src="graphics/logo.png" width="48px" alt="Native Alpha Logo" style="vertical-align: middle;"> Native Alpha

[![GitHub release](https://img.shields.io/github/v/release/JoestarLabs/NativeAlpha?color=blueviolet&style=flat)](https://github.com/JoestarLabs/NativeAlpha/releases)
[![License](https://img.shields.io/github/license/JoestarLabs/NativeAlpha?color=orange&style=flat)](LICENSE)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28-blue?style=flat)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-green?style=flat)](https://developer.android.com)

**Native Alpha** turns web applications and websites into distraction-free, borderless, full-screen native Android applications powered by Android System WebView.

Run your favorite web services with granular privacy controls, ad blocking, custom home screen shortcuts, and an edge-to-edge Material 3 experience.

---

## Features

- **Borderless Full-Screen**: Native, distraction-free web browsing with customizable navigation gestures.
- **Granular Privacy & Permissions**: Configure JavaScript, cookies, location, camera, and microphone per web app.
- **Integrated Ad Blocking**: Flexible ad-blocking engine with support for custom filter lists.
- **Material 3 Design**: Clean, modern interface supporting dynamic system dark mode and edge-to-edge layouts.
- **Shortcuts & Custom Icons**: Add web apps to the home screen with high-resolution icon caching and SVG support.
- **Sandboxed Sessions**: Keep web app data and cookies isolated across distinct sandboxed containers.
- **Biometric Protection**: Lock sensitive web apps with fingerprint or lockscreen PIN authentication.
- **Lightweight & Transparent**: Zero background bloatware, telemetry, or analytics.

---

## Download

### Install via Obtainium

Recommended for automated background updates directly from GitHub releases:

<a href="https://apps.obtainium.page/redirect?r=obtainium://add/https://github.com/JoestarLabs/NativeAlpha/releases">
  <img src=".github/assets/badge_obtainium.png" alt="Get it on Obtainium" height="85" />
</a>

### GitHub Releases

Download release APKs directly from the [GitHub Releases](https://github.com/JoestarLabs/NativeAlpha/releases) page:

- **Universal APK**: Works across all supported Android architectures.
- **ABI-specific APKs**: `arm64-v8a`, `armeabi-v7a` for optimized file sizes.

---

## Screenshots

<details>
<summary><b>Click to expand screenshots</b></summary>
<br>
<div align="center">
  <img src="graphics/screenshots/mainScreen.png" alt="Main Screen" width="230"/>
  <img src="graphics/screenshots/addWebApp.png" alt="Add Web App" width="230"/>
  <img src="graphics/screenshots/webAppSettings.png" alt="Web App Settings" width="230"/>
  <img src="graphics/screenshots/globalSettings.png" alt="Global Settings" width="230"/>
</div>
</details>

---

## FAQ

<details> 
<summary><b>Why use Native Alpha over a mobile browser?</b></summary>
<br>
Standard mobile browsers typically only offer fullscreen shortcuts if a website explicitly supplies a Progressive Web App (PWA) manifest. Native Alpha gives <i>every</i> website a native, borderless container while allowing independent permissions, cache controls, and adblock configurations per site.
</details>

<details> 
<summary><b>Does Native Alpha use its own browser engine?</b></summary>
<br>
No. Native Alpha runs on top of the built-in Android System WebView (Chromium). Keep your device's System WebView updated to benefit from the latest web standards, performance, and security fixes.
</details>

<details> 
<summary><b>Can I maintain separate logins for the same site?</b></summary>
<br>
Yes. Native Alpha supports sandboxed containers that isolate cookies, storage, and sessions across different instances of the same service.
</details>

<details> 
<summary><b>How are website icons retrieved?</b></summary>
<br>
Native Alpha probes the site's manifest, apple-touch-icon, and favicon tags (including SVG favicons) to find the highest available resolution. Custom icons can also be applied.
</details>

---

## Building from Source

### Prerequisites
- Android SDK (API Level 35)
- JDK 17+
- Gradle 8.10+

### Build Debug APK
```bash
./gradlew assembleStandardDebug
```

### Build Release APK
```bash
./gradlew assembleStandardRelease
```

---

## Upstream & Credits

Native Alpha was originally designed and built by [cylonid](https://github.com/cylonid) in [NativeAlphaForAndroid](https://github.com/cylonid/NativeAlphaForAndroid). This repository is a modernized continuation maintained under [JoestarLabs](https://github.com/JoestarLabs). We are deeply grateful to the original author for the foundational project and open-source work.

---

## License

Native Alpha is Free and Open Source Software licensed under the [GNU General Public License v3.0](LICENSE).
