# GlassCalc 📱✨

**GlassCalc** is a modern, visually stunning Android calculator built entirely with Jetpack Compose. It features a unique **Liquid Glass** design language, offering a premium, fluid, and highly customizable user experience.

> ⚠️ **Note:** This project is currently in an experimental phase. The core code was generated with the assistance of AI (Google AI Studio) and has been iteratively curated, tested, and refined by the developer.

## 🚀 Features & Changelog

Here is a comprehensive overview of all features included in GlassCalc, categorized by the version in which they were introduced.

### 📱 Core & Design (Since v1.0.0)
*   **Liquid Glass UI:** Beautifully blurred, semi-transparent buttons and panels for a modern aesthetic.
*   **Material You (Dynamic Colors):** Fully supports Android 12+ dynamic theming, adapting the app's colors to your device's wallpaper.
*   **Multiple Themes:** Includes Automatic (System), White Mode, Dark Mode, and a true **AMOLED Dark Mode** (pure black for battery saving).
*   **Adaptive Layout:** Optimized for both smartphones and tablets, in portrait and landscape orientations.
*   **Scientific Mode (SCI):** Toggleable scientific keypad including trigonometry, inverse functions, logarithms, roots, powers, and physical constants.
*   **Interactive Graphing:** Plot mathematical functions dynamically with a responsive coordinate system.

### 🔄 Intermediate Updates (v1.2.0 - v1.2.8)
*   **Developer Options (v1.2.0):** Built-in debug menu for testing API levels, layout sizes, and cache management.
*   **Unit Conversion (v1.2.0):** Seamlessly convert between Metric and US/Imperial units.
*   **Information & Licensing Hub (v1.2.0):** View the GitHub repository, app license, and third-party licenses.
*   **Smart Haptics (v1.2.2):** Intelligent hardware detection (LRA vs. ERM) with a safety fallback for devices without vibration motors or old APIs.
*   **Enhanced DEL Button (v1.2.2):** Long-pressing the DEL key now continuously deletes characters one by one.
*   **Standard Dark Theme (v1.2.2):** Added a new manually selectable theme matching the system's dark mode colors.
*   **Cursor Navigation (v1.2.6):** Blinking cursor in the display field for precise value insertion. (Replaces the "tap to copy" behavior, which is now handled via the dedicated Copy button).
*   **Fluid Value Transitions (v1.2.6):** Smooth fade/transition animations when numbers change in the display.
*   **Haptic Intensity Slider (v1.2.7):** 10-step intensity slider in the Developer Options (Default: Level 5). LRA motors adjust amplitude; ERM motors adjust duration.
*   **3-Button Navigation Support (v1.2.7):** Fixed UI bugs where the bottom row of keys was covered by classic 3-button navigation bars.
*   **Global Localization (v1.2.8):** Full support for 10 languages (including Klingon) with intelligent English fallback.
*   **Three New Search Bars (v1.2.8):** Added search functionality in Hamburger Menu (formulas), Settings, and Unit Converter with automatic tab synchronization.
*   **New SCI Keys & Integral Fix (v1.2.8):** Added `10^x`, `2^x`, `log_n`, `Ans`, `EXP`, and fixed the numerical integration for `∫`.
*   **Extended Unit Conversion (v1.2.8):** Added "Data" (Byte to TB) and "Time" (ms to Days) categories.
*   **Advanced Haptics Integration (v1.2.8) (Latest):** Complex haptic logic fully integrated into search bars, avoiding double vibrations during typing.

## 🌍 Localization / Sprache

**Supported Languages:** German, English, Spanish, French, Japanese, Chinese, Russian, Portuguese, Hindi, and Klingon.
Since **Version 1.2.8**, GlassCalc fully supports all these languages. The app automatically follows your device's system language by default (with an English fallback). You can manually switch languages at runtime via the "Language" tab in the Settings.

## 🛠️ Tech Stack & App Information

*   **Package Name / Application ID:** `com.limonatalp4.glasscalcproject`
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Local Storage:** DataStore (Preferences)
*   **Minimum SDK:** Android 8.0 (API 26)
*   **Target SDK:** Android 17 (API 37)
*   **Compile SDK:** Android 17 (API 37)
*   **ABI Support:** `arm64-v8a` and `armeabi-v7a`
*   **Optimization:** R8 Minification (ProGuard) enabled for release builds.

## ⚠️ Experimental Disclaimer

Please be aware that this app is an **experimental project**. Because it is actively being developed and relies on AI-assisted code generation, you may encounter unexpected bugs, visual glitches, or crashes. 

The app is provided **"as is"**, without warranty of any kind. Use it at your own risk. Bug reports and feedback are always welcome!

## 📦 Installation

Since this app is not currently on the Google Play Store, you can install it manually:
1. Download the latest `.apk` file from the **Releases** section (or build it yourself in Android Studio).
2. Transfer the APK to your Android device.
3. Enable "Install from unknown sources" in your device settings.
4. Install and enjoy!

## 🤝 Contributing

This project was primarily built for learning and experimentation. If you have ideas for improvements or find a bug, feel free to open an issue or submit a pull request!

## 📄 License & Third-Party Notices

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for more details.

**Copyright 2026 limonatalp4**

### Third-Party Libraries

This app utilizes the following open-source libraries. We thank the authors for their contributions:

*   **[Liquid Glass](https://github.com/nadeemiqbal/liquid-glass):** Licensed under the Apache License 2.0. (Used for UI blur and glass effects).
*   **[MathParser.org-mXparser](https://mathparser.org/):** Licensed under the **mXparser Dual License (Non-Commercial)**. (Used for parsing and evaluating mathematical expressions and formulas). The non-commercial use has been confirmed within the app's source code as required by the license.
*   **Google Play Services (OSS Licenses):** Licensed under the Apache License 2.0. (Used to automatically generate the third-party license menu within the app).
