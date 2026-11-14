package com.limonatalp4.glasscalcproject.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.CalculationHistoryItem
import com.limonatalp4.glasscalcproject.data.preferences.GlassCalcPreferences
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.domain.evaluator.EvaluationResult
import com.limonatalp4.glasscalcproject.domain.evaluator.ExpressionEvaluator
import com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog
import com.limonatalp4.glasscalcproject.domain.formulas.MathFormula
import com.limonatalp4.glasscalcproject.lifecycle.AppLifecycleObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GlassCalcPreferences(application)
    private val evaluator = ExpressionEvaluator()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _cursorPosition = MutableStateFlow(1)
    val cursorPosition: StateFlow<Int> = _cursorPosition.asStateFlow()

    private val _result = MutableStateFlow("0")
    val result: StateFlow<String> = _result.asStateFlow()

    fun setCursorPosition(position: Int) {
        val maxLen = if (_expression.value.isEmpty()) 1 else _expression.value.length
        _cursorPosition.value = position.coerceIn(0, maxLen)
    }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    private val _isGraphLocked = MutableStateFlow(false)
    val isGraphLocked: StateFlow<Boolean> = _isGraphLocked.asStateFlow()

    fun setGraphLocked(locked: Boolean) {
        _isGraphLocked.value = locked
    }

    fun importConvertedValue(value: String) {
        val cleaned = value.trim()
        if (cleaned.isEmpty()) return
        _errorMessage.value = null
        justCalculated = false
        _expression.value = cleaned
        _cursorPosition.value = cleaned.length
        tryLivePreview()
    }

    fun applyGraphPointToCalculator(x: Double, y: Double) {
        if (!x.isFinite() || !y.isFinite()) return
        val xFormatted = formatGraphCoordinate(x)
        val yFormatted = formatGraphCoordinate(y)
        val newExpr = if (yFormatted.startsWith("−")) {
            "$xFormatted + ($yFormatted)"
        } else {
            "$xFormatted + $yFormatted"
        }
        _errorMessage.value = null
        justCalculated = false
        _expression.value = newExpr
        _cursorPosition.value = newExpr.length
        tryLivePreview()
    }

    private fun formatGraphCoordinate(value: Double): String {
        if (abs(value) < 1e-9) return "0"
        if (abs(value - round(value)) < 1e-7 && abs(value) < 1e12) {
            return round(value).toLong().toString().replace("-", "−")
        }
        val symbols = DecimalFormatSymbols(Locale.GERMANY)
        val formatter = DecimalFormat("#,##0.####", symbols).apply {
            isGroupingUsed = false
        }
        return formatter.format(value).replace("-", "−")
    }

    fun insertFormula(formulaExpression: String) {
        if (formulaExpression.isBlank()) return
        _errorMessage.value = null
        val curr = _expression.value
        val pos = _cursorPosition.value.coerceIn(0, curr.length)
        val shouldInsertAtCursor = !justCalculated &&
            curr.isNotEmpty() &&
            pos > 0 &&
            curr[pos - 1] in "+-−*/×÷(;"

        val (updated, nextCursor) = if (shouldInsertAtCursor) {
            val inserted = curr.substring(0, pos) + formulaExpression + curr.substring(pos)
            inserted to (pos + formulaExpression.length)
        } else {
            formulaExpression to formulaExpression.length
        }

        justCalculated = false
        _expression.value = updated
        _cursorPosition.value = nextCursor
        tryLivePreview()
    }

    val formulas: List<MathFormula> = FormulaCatalog.FORMULAS

    fun searchFormulas(query: String, context: Context): List<MathFormula> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return formulas.filter { item ->
            val localizedName = context.getString(item.nameResId)
            val localizedKeywords = item.keywordsResIds.flatMap { resId ->
                context.getString(resId)
                    .split(',', ';')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
            localizedName.contains(trimmed, ignoreCase = true) ||
                item.formula.contains(trimmed, ignoreCase = true) ||
                localizedKeywords.any { it.contains(trimmed, ignoreCase = true) }
        }
    }

    val angleMode: StateFlow<AngleMode> = preferences.angleModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AngleMode.DEG)

    val simulatedApiEnabled: StateFlow<Boolean> = preferences.simulatedApiEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val simulatedApiLevel: StateFlow<Int> = preferences.simulatedApiLevelFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 30)

    private val _systemLocaleRefreshTrigger = MutableStateFlow(0)
    private val _isLanguageLoaded = MutableStateFlow(false)
    val isLanguageLoaded: StateFlow<Boolean> = _isLanguageLoaded.asStateFlow()

    private val _language = MutableStateFlow("SYSTEM")
    val language: StateFlow<String> = _language.asStateFlow()

    val effectiveLanguage: StateFlow<String> = combine(
        language,
        _systemLocaleRefreshTrigger
    ) { selectedLanguageTag, _ ->
        resolveEffectiveLanguageTag(selectedLanguageTag)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        resolveEffectiveLanguageTag("SYSTEM")
    )

    fun setLanguage(languageTag: String) {
        val isSystem = languageTag.isBlank() || languageTag.equals("SYSTEM", ignoreCase = true)
        val normalizedTag = if (isSystem) "SYSTEM" else languageTag.trim().lowercase(Locale.ROOT)

        _language.value = normalizedTag
        _systemLocaleRefreshTrigger.value += 1

        viewModelScope.launch {
            try {
                preferences.setLanguage(normalizedTag)
            } catch (_: Exception) {
            }

            try {
                if (isSystem) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                } else {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalizedTag))
                }
            } catch (e: Throwable) {
                Log.e("GlassCalc", "Fehler beim Setzen von setApplicationLocales", e)
            }
        }
    }

    val currentTheme: StateFlow<AppTheme> = combine(
        preferences.themeFlow,
        simulatedApiEnabled,
        simulatedApiLevel
    ) { savedTheme, simEnabled, simLevel ->
        val effective = if (simEnabled) simLevel else Build.VERSION.SDK_INT
        
        // 1. Initial Preference (Saved or Intelligent Default)
        val pref = savedTheme ?: if (effective >= 31) AppTheme.MATERIAL_YOU else AppTheme.AUTOMATIC
        
        // 2. Real-time Fallback for UI sync
        if (pref == AppTheme.MATERIAL_YOU && effective < 31) {
            AppTheme.AUTOMATIC
        } else {
            pref
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.AUTOMATIC)

    private val _history = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
    val history: StateFlow<List<CalculationHistoryItem>> = _history.asStateFlow()

    val hapticFeedbackEnabled: StateFlow<Boolean> = preferences.hapticFeedbackEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val hapticIntensity: StateFlow<Int> = preferences.hapticIntensityFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 5)

    val lifecycleStatus: StateFlow<String> = AppLifecycleObserver.lastLifecycleEvent

    private val _isAutoResultEnabled = preferences.autoResultEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val isAutoResultEnabled: StateFlow<Boolean> = _isAutoResultEnabled

    fun setAutoResultEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoResultEnabled(enabled)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setHapticIntensity(intensity: Int) {
        val clamped = intensity.coerceIn(1, 10)
        HapticUtil.currentIntensityLevel = clamped
        viewModelScope.launch {
            preferences.setHapticIntensity(clamped)
        }
    }

    private var justCalculated = false
    private var isHistoryCleared = false
    private var previewJob: Job? = null

    init {
        // Load and observe saved language from DataStore
        viewModelScope.launch {
            preferences.languageFlow.collect { savedLang ->
                val normalized = if (savedLang.isBlank() || savedLang.equals("SYSTEM", ignoreCase = true)) {
                    "SYSTEM"
                } else {
                    savedLang
                }
                _language.value = normalized
                _isLanguageLoaded.value = true
            }
        }

        // Load saved state once from DataStore crash-safe
        viewModelScope.launch {
            runCatching {
                val (savedExpr, savedRes) = preferences.savedStateFlow.first()
                if (_expression.value.isEmpty() && savedExpr.isNotEmpty()) {
                    _expression.value = savedExpr
                    _cursorPosition.value = savedExpr.length
                    _result.value = savedRes
                }
            }
        }

        // Load initial history once from DataStore without overwriting optimistic updates
        viewModelScope.launch {
            runCatching {
                val stored = preferences.historyFlow.first()
                if (!isHistoryCleared && stored.isNotEmpty()) {
                    val currentLocal = _history.value
                    val merged = (currentLocal + stored.filter { s -> currentLocal.none { it.id == s.id } }).take(20)
                    _history.value = merged
                }
            }
        }

        // Register with ProcessLifecycleObserver
        AppLifecycleObserver.initialize(
            onStop = { onAppStop() },
            onStart = { onAppStart() }
        )

        // Keep HapticUtil intensity synced with persisted preference
        viewModelScope.launch {
            preferences.hapticIntensityFlow.collect { intensity ->
                HapticUtil.currentIntensityLevel = intensity
            }
        }
    }

    fun onAppStop() {
        // Save current calculation state crash-safely on ON_STOP
        viewModelScope.launch {
            preferences.saveCalculationState(_expression.value, _result.value)
        }
    }

    fun onAppStart() {
        // Re-check device locale in case system language changed while in background
        _systemLocaleRefreshTrigger.value += 1
    }

    fun simulateBackgroundSave() {
        AppLifecycleObserver.simulateBackground()
        onAppStop()
    }

    fun simulateResume() {
        AppLifecycleObserver.simulateForeground()
    }

    fun testCrashProtection() {
        // Save state immediately first so the test succeeds on restart
        viewModelScope.launch {
            preferences.saveCalculationState(_expression.value, _result.value)
            throw RuntimeException("Künstlicher Test-Absturz zur Überprüfung des Zustandserhalts!")
        }
    }

    private fun getValidChainingResult(): String? {
        val res = _result.value
        if (res.isNotEmpty() && _errorMessage.value == null && res != "Error") {
            return res
        }
        return null
    }

    fun onCharacterInput(char: String) {
        _errorMessage.value = null

        var curr = _expression.value
        var pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null && (isOperator(char) || char == "²" || char == "³" || char == "^" || char == "!" || char == "%")) {
                curr = chainingRes
                pos = curr.length
            } else {
                curr = ""
                pos = 0
            }
        }
        justCalculated = false

        val updated = curr.substring(0, pos) + char + curr.substring(pos)
        _expression.value = updated
        _cursorPosition.value = pos + char.length
        tryLivePreview()
    }

    fun onScientificFunction(fnName: String) {
        _errorMessage.value = null

        var curr = _expression.value
        var pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null) {
                val expr = "$fnName($chainingRes)"
                _expression.value = expr
                _cursorPosition.value = expr.length
                justCalculated = false
                tryLivePreview()
                return
            } else {
                curr = ""
                pos = 0
            }
        }
        justCalculated = false

        val insertStr = "$fnName("
        val updated = curr.substring(0, pos) + insertStr + curr.substring(pos)
        _expression.value = updated
        _cursorPosition.value = pos + insertStr.length
        tryLivePreview()
    }

    fun onConstant(constant: String) {
        _errorMessage.value = null

        var curr = _expression.value
        var pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (justCalculated) {
            curr = ""
            pos = 0
        }
        justCalculated = false

        val updated = curr.substring(0, pos) + constant + curr.substring(pos)
        _expression.value = updated
        _cursorPosition.value = pos + constant.length
        tryLivePreview()
    }

    fun onPower(exponent: String) {
        _errorMessage.value = null

        var curr = _expression.value
        var pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null) {
                curr = chainingRes
                pos = curr.length
            } else {
                curr = ""
                pos = 0
            }
        }
        justCalculated = false

        val insertStr = when (exponent) {
            "2" -> "^2"
            "3" -> "^3"
            else -> "^"
        }
        val updated = curr.substring(0, pos) + insertStr + curr.substring(pos)
        _expression.value = updated
        _cursorPosition.value = pos + insertStr.length
        tryLivePreview()
    }

    fun onFractionSlash() {
        _errorMessage.value = null

        var curr = _expression.value
        var pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null) {
                curr = chainingRes
                pos = curr.length
            } else {
                curr = ""
                pos = 0
            }
        }
        justCalculated = false

        val updated = curr.substring(0, pos) + "/" + curr.substring(pos)
        _expression.value = updated
        _cursorPosition.value = pos + 1
        tryLivePreview()
    }

    fun onNthRoot() {
        _errorMessage.value = null

        var curr = _expression.value
        var pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null) {
                curr = chainingRes
                pos = curr.length
            } else {
                curr = ""
                pos = 0
            }
        }
        justCalculated = false

        val insertStr = "ⁿ√("
        val updated = curr.substring(0, pos) + insertStr + curr.substring(pos)
        _expression.value = updated
        _cursorPosition.value = pos + insertStr.length
        tryLivePreview()
    }

    fun onToggleSign() {
        _errorMessage.value = null
        justCalculated = false
        val curr = _expression.value
        val pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (curr.isEmpty()) {
            _expression.value = "−"
            _cursorPosition.value = 1
        } else if (curr.startsWith("−") || curr.startsWith("-")) {
            _expression.value = curr.substring(1)
            _cursorPosition.value = (pos - 1).coerceAtLeast(0)
        } else {
            _expression.value = "−$curr"
            _cursorPosition.value = pos + 1
        }
        tryLivePreview()
    }

    fun onDelete() {
        _errorMessage.value = null
        justCalculated = false
        val curr = _expression.value
        val pos = _cursorPosition.value.coerceIn(0, curr.length)

        if (curr.isNotEmpty() && pos > 0) {
            val textBeforeCursor = curr.substring(0, pos)
            val textAfterCursor = curr.substring(pos)

            val funcs = listOf(
                "arcsin(", "arccos(", "arctan(",
                "sinh(", "cosh(", "tanh(",
                "d/dx(", "der(", "int(", "∫(", "abs(", "mod(", "log(", "ln(",
                "sin(", "cos(", "tan(", "³√(", "ⁿ√(", "√("
            )
            var deletedLen = 1
            for (f in funcs) {
                if (textBeforeCursor.endsWith(f)) {
                    deletedLen = f.length
                    break
                }
            }

            val newTextBefore = textBeforeCursor.dropLast(deletedLen)
            _expression.value = newTextBefore + textAfterCursor
            _cursorPosition.value = pos - deletedLen
        }
        tryLivePreview()
    }

    fun onClearAll() {
        _expression.value = ""
        _cursorPosition.value = 1
        _result.value = "0"
        _errorMessage.value = null
        justCalculated = false
        viewModelScope.launch {
            preferences.saveCalculationState("", "0")
        }
    }

    fun onEquals() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        when (val eval = evaluator.evaluate(expr, angleMode.value)) {
            is EvaluationResult.Success -> {
                val formatted = eval.formatted
                _result.value = formatted
                _errorMessage.value = null
                justCalculated = true
                _cursorPosition.value = _expression.value.length

                val historyItem = CalculationHistoryItem(
                    expression = expr,
                    result = formatted
                )
                isHistoryCleared = false
                _history.value = (listOf(historyItem) + _history.value).take(20)
                viewModelScope.launch {
                    preferences.addHistoryItem(historyItem)
                    preferences.saveCalculationState(expr, formatted)
                }
            }
            is EvaluationResult.Error -> {
                _result.value = "Error"
                _errorMessage.value = getApplication<Application>().getString(eval.messageResId)
            }
        }
    }

    private fun tryLivePreview() {
        previewJob?.cancel()
        val expr = _expression.value.trim()
        if (expr.isEmpty()) {
            _result.value = "0"
            return
        }
        // Live preview only if expression has operators/functions/constants
        val hasOp = expr.any { it in "+-*÷×/^%!scl∫√ⁿeπdat;" }
        if (hasOp && _isAutoResultEnabled.value) {
            previewJob = viewModelScope.launch(Dispatchers.Default) {
                when (val eval = evaluator.evaluate(expr, angleMode.value)) {
                    is EvaluationResult.Success -> {
                        if (_expression.value.trim() == expr) {
                            _result.value = eval.formatted
                        }
                    }
                    is EvaluationResult.Error -> {
                        // Do not show errors during typing preview
                    }
                }
            }
        }
    }

    private fun isOperator(s: String): Boolean {
        return s in listOf("+", "-", "*", "/", "×", "÷", "%", "^")
    }

    fun toggleScientificPanel() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    fun toggleAngleMode() {
        val next = if (angleMode.value == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        viewModelScope.launch {
            preferences.setAngleMode(next)
        }
        tryLivePreview()
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferences.setTheme(theme)
        }
    }

    fun restoreHistoryItem(item: CalculationHistoryItem) {
        _expression.value = item.expression
        _cursorPosition.value = item.expression.length
        _result.value = item.result
        _errorMessage.value = null
        justCalculated = false
    }

    fun clearHistory() {
        isHistoryCleared = true
        _history.value = emptyList()
        viewModelScope.launch {
            preferences.clearHistory()
        }
    }

    fun setSimulatedApi(enabled: Boolean, level: Int) {
        viewModelScope.launch {
            preferences.setSimulatedApi(enabled, level)
        }
    }

    fun resetApp() {
        isHistoryCleared = true
        _history.value = emptyList()
        viewModelScope.launch {
            preferences.resetAll()
            _expression.value = ""
            _cursorPosition.value = 1
            _result.value = "0"
            _errorMessage.value = null
            _isScientificExpanded.value = false
        }
    }

    fun getEffectiveApiLevel(): Int {
        return if (simulatedApiEnabled.value) simulatedApiLevel.value else Build.VERSION.SDK_INT
    }

    companion object {
        val SUPPORTED_LANGUAGE_TAGS: Set<String> = setOf("de", "en", "es", "fr", "ja", "zh", "tlh", "ru", "pt", "hi")
        const val FALLBACK_LANGUAGE_TAG: String = "en"

        fun isSystemLocaleSupported(systemLocale: Locale = resolveDeviceSystemLocale()): Boolean {
            val systemTag = systemLocale.language.lowercase(Locale.ROOT)
                .substringBefore('-')
                .substringBefore('_')
            return systemTag in SUPPORTED_LANGUAGE_TAGS
        }

        fun resolveEffectiveLanguageTag(
            selectedLanguageTag: String,
            systemLocale: Locale = resolveDeviceSystemLocale()
        ): String {
            val trimmed = selectedLanguageTag.trim()
            if (trimmed.isNotEmpty() && !trimmed.equals("SYSTEM", ignoreCase = true)) {
                val explicitTag = trimmed.lowercase(Locale.ROOT)
                    .substringBefore('-')
                    .substringBefore('_')
                return if (explicitTag in SUPPORTED_LANGUAGE_TAGS) {
                    explicitTag
                } else {
                    FALLBACK_LANGUAGE_TAG
                }
            }

            val systemTag = systemLocale.language.lowercase(Locale.ROOT)
                .substringBefore('-')
                .substringBefore('_')
            return if (systemTag in SUPPORTED_LANGUAGE_TAGS) {
                systemTag
            } else {
                FALLBACK_LANGUAGE_TAG
            }
        }

        fun resolveDeviceSystemLocale(): Locale {
            return try {
                val sysLocales = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
                if (!sysLocales.isEmpty && sysLocales[0] != null) {
                    sysLocales[0]!!
                } else {
                    Locale.getDefault()
                }
            } catch (_: Throwable) {
                Locale.getDefault()
            }
        }
    }
}
