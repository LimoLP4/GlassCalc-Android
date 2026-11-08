# GlassCalc - Ergebnis-Weiterverarbeitung, Zwischenablage & Haptik-Fix

## Title & Summary
Erweiterung der Android-Rechner-App **GlassCalc** um nahtlose Ergebnis-Weiterverarbeitung (Chaining), einfaches Kopieren in die System-Zwischenablage per Display-Interaktion sowie eine fehlerfreie, verzögerungsfreie Haptik-Implementierung für das Samsung Galaxy S25 (Android 17).

## User Review & Critical Decisions
> [!IMPORTANT] In Phase 1 bestätigte Interaktionsentscheidungen
- **Kopier-Interaktion**: Das aktuelle Ergebnis oder der Ausdruck im Display wird durch einfaches Tippen oder Gedrückthalten direkt auf das `GlassDisplay` in die Android-Zwischenablage kopiert.
- **Visuelles Feedback**: Beim Kopieren blinkt das Glass-Display kurz sanft auf und zeigt eine prägnante Toast-Meldung ("In Zwischenablage kopiert").
- **Haptik-Architektur**: Die Auslösung von haptischer Vibration wird direkt in die `onClick`-Callbacks der Buttons integriert und greift für Samsung OneUI / Android 17 verlässlich auf den `Vibrator`-Systemdienst als Fallback zurück (gekoppelt an den Schalter in den Entwickleroptionen).

---

## 1. Overview & Core Concept

1. **Ergebnis weiterverwenden (Calculation Chaining)**:
   - Sobald eine Berechnung mit `=` abgeschlossen wurde und ein fixes Ergebnis im Display steht, bewirkt das Drücken eines Operators (`+`, `-`, `×`, `÷`, `%`, `^`, `²`, `³`) oder einer wissenschaftlichen Funktion (`sin`, `cos`, `tan`, `log`, `ln`, `√`, `³√`), dass das bisherige Ergebnis automatisch als Startwert übernommen wird.
   - Tippt der Nutzer hingegen direkt eine neue Ziffer (`0`–`9`), eine Konstante (`π`, `e`) oder `(` ein, wird der alte Ausdruck zurückgesetzt und eine frische Rechnung begonnen.
2. **Kopieren in die Zwischenablage**:
   - `GlassDisplay` reagiert auf Klick- und Long-Press-Eingaben.
   - Nutzt den Android `ClipboardManager` über `context.getSystemService(Context.CLIPBOARD_SERVICE)`.
   - Bei Auslösung pulsiert eine sanfte Glanz-Animation im Display und ein Toast informiert den Nutzer.
3. **Robustes Haptisches Feedback (Samsung Galaxy S25 Fix)**:
   - Die Haptik-Auslösung wird aus untergeordneten `pointerInput`/`detectTapGestures`-Blocks in die primären `onClick`-Ereignisse verlagert.
   - Direkter Aufruf über `android.os.Vibrator` / `VibratorManager` mit `VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)` als hardwarenaher Fallback für Samsung S25.
   - Behält die vollständige Steuerung über den persistenten Schalter in den Entwickleroptionen bei.

---

## 2. User Experience & Visual Design

- **Ergebnis-Chaining Flow**:
  - `5 + 5 = 10` (Rechnung abgeschlossen, `justCalculated = true`)
  - Nutzer drückt `+` → Display zeigt sofort `10+` (nahtloser Arbeitsfluss).
  - Nutzer drückt `3` → Display zeigt `10+3`.
  - Drückt der Nutzer stattdessen nach `= 10` direkt `7`, startet die Eingabe neu bei `7`.

- **Display Copy Flow**:
  - Nutzer tippt oder hält das Display gedrückt.
  - Das Glass-Display leuchtet für 150 ms in sanftem Neon-Cyan auf.
  - Das System zeigt den Toast: *"In Zwischenablage kopiert: 10"*.

- **Visual Feedback & Haptik**:
  - Die Auslösung von `EFFECT_CLICK` / `KEYBOARD_TAP` erfolgt spürbar zeitgleich mit dem optischen Skalierungs-Feedback (90% Button-Scale).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Trennung von Operatoren und Ziffern im `justCalculated`-Zustand**:
  - *Chosen Approach*: `CalculatorViewModel` prüft beim Tasten-Input den Typ der Taste. Operatoren und Funktionen verknüpfen das vorherige Ergebnis, Ziffern und Klammern löschen das bisherige Ergebnis.
  - *Why*: Entspricht dem gewohnten Verhalten professioneller wissenschaftlicher Taschenrechner (z. B. Casio, Texas Instruments, iOS/Android System-Rechner).

- **Decision 2: Haptik-Auslösung über `Vibrator` Systemdienst**:
  - *Chosen Approach*: Neben Compose `LocalHapticFeedback` wird ein kompakter, sicherer Haptic-Manager integriert, der auf Samsung OneUI Geräte abgestimmt ist.
  - *Why*: Samsung Android 17 / OneUI unterdrückt teilweise generisches Compose `performHapticFeedback` in Custom Touch-Pointern. Der direkte System-Vibrator garantiert 100% Zuverlässigkeit.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CalculatorScreen (UI Layer)                      │
│                                                                         │
│  ┌───────────────────────┐             ┌─────────────────────────────┐  │
│  │     GlassDisplay      │             │    GlassCalculatorButton    │  │
│  │  - Tap/LongClick      │             │  - Direct onClick() Haptics │  │
│  │  - ClipboardManager   │             │  - HapticFeedback / Vibrator│  │
│  │  - Flash Animation    │             │  - Ripple + Scale Motion    │  │
│  └───────────┬───────────┘             └──────────────┬──────────────┘  │
└──────────────│────────────────────────────────────────│─────────────────┘
               │                                        │
               ▼                                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   CalculatorViewModel (Domain / State)                  │
│                                                                         │
│  - _expression: StateFlow<String>                                       │
│  - _result: StateFlow<String>                                           │
│  - justCalculated: Boolean                                              │
│  - onOperatorInput() -> Chaining Logic                                  │
│  - onCharacterInput() -> New Input vs. Append                           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Komponenten- und Code-Änderungen:
1. `CalculatorViewModel.kt`:
   - Erweiterung von `onCharacterInput`, `onScientificFunction` und `onPower`: Wenn `justCalculated == true` und eine Operatortaste oder Funktion gedrückt wird, wird `_expression.value` mit dem bisherigen `_result.value` initialisiert, bevor das neue Zeichen angehängt wird.
2. `GlassDisplay.kt`:
   - Hinzufügen von `.clickable` / `.pointerInput` mit `ClipboardManager` Aufruf (`ClipData.newPlainText("GlassCalc", textToCopy)`).
   - Auslösen eines visuellen Flash-Zustands (`isCopyFlashed`) und Anzeigen eines Toasts.
3. `GlassCalculatorButton.kt`:
   - Aufruf der Haptik direkt in `onClick()` und `onLongClick()`.
   - Einbindung des `Vibrator` Systemdienstes über `context.getSystemService(Context.VIBRATOR_SERVICE)` als Fallback.
4. `AndroidManifest.xml`:
   - Bestätigung von `<uses-permission android:name="android.permission.VIBRATE" />`.
