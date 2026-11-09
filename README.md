# GlassCalc 📱✨

**GlassCalc** is a modern, visually stunning Android calculator built entirely with Jetpack Compose. It features a unique **Liquid Glass** design language, offering a premium, fluid, and highly customizable user experience.

> ⚠️ **Note:** This project is currently in an experimental phase. The core code was generated with the assistance of AI (Google AI Studio) and has been iteratively curated, tested, and refined by the developer.

## 🚀 Features (Version 1.1)

*   **Liquid Glass UI:** Beautifully blurred, semi-transparent buttons and panels for a modern aesthetic.
*   **Advanced Scientific Mode:** Toggleable scientific keypad including trigonometry (sin, cos, tan), logarithms, roots, powers, physical constants, derivatives (d/dx), and integrals (∫).
*   **Interactive Graphing:** Plot mathematical functions dynamically with a responsive coordinate system.
*   **Extended Math Capabilities:** Supports inverse trigonometric functions (arcsin, arccos, arctan), hyperbolic functions (sinh, cosh, tanh), absolute values (|x|), modulo, and custom log bases.
*   **Material You (Dynamic Colors):** Fully supports Android 12+ dynamic theming, adapting the app's colors to your device's wallpaper.
*   **Multiple Themes:** Includes Automatic (System), White Mode, Dark Mode, and a true **AMOLED Dark Mode** (pure black for battery saving).
*   **Unit Conversion:** Seamlessly convert between Metric and US/Imperial units (m↔ft, cm↔in, kg↔lb, etc.) with a sleek, rounded input field.
*   **Adaptive Layout:** Optimized for both smartphones and tablets, in portrait and landscape orientations.
*   **Haptic Feedback:** Tactile response on button presses, graph interactions, sliders, and switches (with OEM-specific fallbacks for Samsung, Xiaomi, etc.).
*   **Information & Licensing Hub:** A dedicated "Information" tab in the settings to view the GitHub repository, the app's license, and third-party licenses.
*   **Developer Options:** Built-in debug menu for testing API levels, layout sizes, and cache management.

## 🌍 Localization / Sprache

**Current Language: German (Deutsch)**
Please note that the current version of GlassCalc is available **exclusively in German**. All buttons, menus, and descriptions are in German.

**Future Update:** English support is currently **planned for a future update**. If you are interested in helping with the translation, feel free to reach out!

## 🛠️ Tech Stack & App Information

*   **Package Name / Application ID:** `com.limonatalp4.glasscalcproject`
*   **Version:** 1.1
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Local Storage:** DataStore (Preferences)
*   **Minimum SDK:** Android 8.0 (API 26)
*   **Target SDK:** Android 17 (API 37)
*   **ABI Support:** `arm64-v8a` and `armeabi-v7a`

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