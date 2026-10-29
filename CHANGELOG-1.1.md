# VypeBrowser TV 1.1 (test build)

- targetSdk 35 for Android TV 14.
- Fullscreen HTML5 video via WebChromeClient custom views.
- Remote-controlled pointer mode: toolbar ⌖; arrows move pointer; OK taps.
- Loading/stop toggle: × while loading, ⟳ when idle.
- Third-party cookies for website login compatibility.

Limitations: Site-owned video control overlays cannot be forced to show/hide on a timer. DRM video (Widevine), site-specific anti-WebView restrictions, and Play Protect verdicts cannot be guaranteed. This source has not been tested on the actual TV.

Update GitHub files `app/src/main/java/com/vype/browser/MainActivity.java`, `app/build.gradle`, and `.github/workflows/build-apk.yml`; run Actions. For a new release use v1.1.0. Debug APK signatures from different build machines may not match, requiring uninstalling the old test build (back up bookmarks first).
