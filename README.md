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

### 🔄 Intermediate Updates (v1.2.0 - v1.3.6)
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
*   **Advanced Haptics Integration (v1.2.8):** Complex haptic logic fully integrated into search bars, avoiding double vibrations during typing.
*   **System Keyboard Support (v1.2.9):** Added a new button in the main display to open the standard Android keyboard for direct formula entry, with smart filtering (`KeyboardType.Ascii` + Whitelist) to prevent crashes from foreign characters.
*   **UI & Design Consistency (v1.2.9):** Liquid Glass close buttons (X) in all dialogs, dynamic cursor matching the search bars, and smoother animations.
*   **Layout & Focus Fixes (v1.2.9):** Narrower dialogs, fixed keyboard overlap (IME Insets) in Settings/Unit Converter, and corrected focus management when opening the Hamburger menu.
*   **Search Improvements (v1.2.9):** Better keyword matching and relevance sorting in all three search bars.
*   **Graph System Overhaul (v1.3.0):** Completely rewrote the screen-to-math coordinate transformation for accurate point placement (P1/P2) and strict domain enforcement.
*   **Keyboard Toggle & Landscape Fix (v1.3.0):** The keyboard button now opens and closes the keyboard. Fixed landscape layout where the keyboard covered the display.
*   **UI Polish (v1.3.0):** Applied Liquid Glass pill styling to the graph's P1/P2 info panel, GitHub logo, and Apache License card.
*   **State Preservation (v1.3.1):** All dialogs (Settings, Graph, Unit Converter, History) and the Hamburger menu now remain open when the device is rotated.
*   **History Panel Redesign (v1.3.1):** The history panel is now a floating Liquid Glass pill with an X-button and can be closed by tapping outside.
*   **Performance Boost (v1.3.1):** Integrated Baseline Profiles, optimized Compose recomposition, and moved heavy calculations to background threads for smoother performance on all devices.
*   **Theme Logic & Consistency (v1.3.2):** Renamed "Deep Black" to "AMOLED", removed the redundant theme, and unified all border strokes and card shapes (pill design) across the app.
*   **Battery Saver Integration (v1.3.2):** The "Automatic" theme now automatically switches to the AMOLED theme when the device's battery saver is active (does not affect Material You).
*   **UI Hotfix & Polish (v1.3.3):** Fixed pill shapes in settings, clipped rectangular ripples, styled Hamburger menu search suggestions as Liquid Glass pills, and expanded the touch targets for calculator keys.
*   **Graph Control Pill (v1.3.4):** Wrapped the graph navigation and zoom buttons into a unified Liquid Glass pill container.
*   **Expanded Unit Categories (v1.3.5):** Massively expanded the Unit Converter with new electrical (Voltage, Current, Power, Charge), extended volume (ml, cl, m³, tsp, fl oz, etc.), and additional units for length (cm, mm, nm), mass (metric ton), and temperature (Kelvin). All units are fully localized across all 10 languages.
*   **Advanced Units, App Icons & Accordions (v1.3.6):** Added even more unit categories (Angle, Speed, Resistance, Frequency, Data Rate, Energy, Pressure). Introduced multiple selectable App Icons (including Material You support with API check) via `activity-alias` without restarting the app. Implemented collapsible accordion menus for all settings tabs and the Unit Converter.
*   **Custom Accent Colors & Consistency (v1.3.7) (Latest):** Introduced custom accent color selection via color palette and Hex code (exclusive to non-Material You themes). Fixed hardcoded text colors in number buttons and headers. Improved input performance (no more dropped keys during fast typing) and restored the cursor. Refined accordion layouts in the Info tab and updated all interactive icons (X, keyboard, copy, etc.) to respect the dynamic accent color.

## 🌍 Localization / Sprache

**Supported Languages:** German, English, Spanish, French, Japanese, Chinese, Russian, Portuguese, Hindi, and Klingon.
Since **Version 1.2.8**, GlassCalc fully supports all these languages. The app automatically follows your device's system language by default (with an English fallback). You can manually switch languages at runtime via the "Language" tab in the Settings.

## 🛠️ Tech Stack & App Information

*   **Package Name / Application ID:** `com.limonatalp4.glasscalcproject`
*   **Version:** 1.3.6
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Local Storage:** DataStore (Preferences)
*   **Minimum SDK:** Android 8.0 (API 26)
*   **Target SDK:** Android 17 (API 37)
*   **Compile SDK:** Android 17 (API 37)
*   **ABI Support:** `arm64-v8a` and `armeabi-v7a`
*   **Optimization:** R8 Minification (ProGuard) and Baseline Profiles enabled for release builds.

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
