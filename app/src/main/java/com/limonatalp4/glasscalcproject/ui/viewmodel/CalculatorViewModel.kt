package com.limonatalp4.glasscalcproject.ui.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.CalculationHistoryItem
import com.limonatalp4.glasscalcproject.data.preferences.GlassCalcPreferences
import com.limonatalp4.glasscalcproject.domain.evaluator.EvaluationResult
import com.limonatalp4.glasscalcproject.domain.evaluator.ExpressionEvaluator
import com.limonatalp4.glasscalcproject.lifecycle.AppLifecycleObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = GlassCalcPreferences(application)
    private val evaluator = ExpressionEvaluator()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("0")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    val angleMode: StateFlow<AngleMode> = preferences.angleModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AngleMode.DEG)

    val currentTheme: StateFlow<AppTheme> = preferences.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.AUTOMATIC)

    val history: StateFlow<List<CalculationHistoryItem>> = preferences.historyFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val simulatedApiEnabled: StateFlow<Boolean> = preferences.simulatedApiEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val simulatedApiLevel: StateFlow<Int> = preferences.simulatedApiLevelFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 30)

    val hapticFeedbackEnabled: StateFlow<Boolean> = preferences.hapticFeedbackEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val lifecycleStatus: StateFlow<String> = AppLifecycleObserver.lastLifecycleEvent

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setHapticFeedbackEnabled(enabled)
        }
    }

    private var justCalculated = false

    init {
        // Load saved state from DataStore crash-safe
        viewModelScope.launch {
            preferences.savedStateFlow.collect { (savedExpr, savedRes) ->
                if (_expression.value.isEmpty() && savedExpr.isNotEmpty()) {
                    _expression.value = savedExpr
                    _result.value = savedRes
                }
            }
        }

        // Register with ProcessLifecycleObserver
        AppLifecycleObserver.initialize(
            onStop = { onAppStop() },
            onStart = { onAppStart() }
        )
    }

    fun onAppStop() {
        // Save current calculation state crash-safely on ON_STOP
        viewModelScope.launch {
            preferences.saveCalculationState(_expression.value, _result.value)
        }
    }

    fun onAppStart() {
        // State is already maintained in ViewModel / DataStore
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
        if (res.isNotEmpty() && _errorMessage.value == null && res != "Error" && !res.contains("nicht möglich")) {
            return res
        }
        return null
    }

    fun onCharacterInput(char: String) {
        _errorMessage.value = null

        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null && (isOperator(char) || char == "²" || char == "³" || char == "^" || char == "!" || char == "%")) {
                _expression.value = chainingRes
            } else {
                _expression.value = ""
            }
        }
        justCalculated = false

        _expression.value += char
        tryLivePreview()
    }

    fun onScientificFunction(fnName: String) {
        _errorMessage.value = null
        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null) {
                _expression.value = "$fnName($chainingRes)"
                justCalculated = false
                tryLivePreview()
                return
            } else {
                _expression.value = ""
            }
        }
        justCalculated = false

        _expression.value += "$fnName("
        tryLivePreview()
    }

    fun onConstant(constant: String) {
        _errorMessage.value = null
        if (justCalculated) {
            _expression.value = ""
        }
        justCalculated = false

        _expression.value += constant
        tryLivePreview()
    }

    fun onPower(exponent: String) {
        _errorMessage.value = null
        if (justCalculated) {
            val chainingRes = getValidChainingResult()
            if (chainingRes != null) {
                _expression.value = chainingRes
            } else {
                _expression.value = ""
            }
        }
        justCalculated = false

        _expression.value += when (exponent) {
            "2" -> "^2"
            "3" -> "^3"
            else -> "^"
        }
        tryLivePreview()
    }

    fun onToggleSign() {
        _errorMessage.value = null
        justCalculated = false
        val curr = _expression.value
        if (curr.isEmpty()) {
            _expression.value = "−"
        } else if (curr.startsWith("−")) {
            _expression.value = curr.substring(1)
        } else if (curr.startsWith("-")) {
            _expression.value = curr.substring(1)
        } else {
            _expression.value = "−$curr"
        }
        tryLivePreview()
    }

    fun onDelete() {
        _errorMessage.value = null
        justCalculated = false
        val curr = _expression.value
        if (curr.isNotEmpty()) {
            // If ends with function like "sin(", "cos(", "log(", remove the entire token
            val funcs = listOf("sin(", "cos(", "tan(", "log(", "ln(", "³√(", "√(")
            var handled = false
            for (f in funcs) {
                if (curr.endsWith(f)) {
                    _expression.value = curr.dropLast(f.length)
                    handled = true
                    break
                }
            }
            if (!handled) {
                _expression.value = curr.dropLast(1)
            }
        }
        tryLivePreview()
    }

    fun onClearAll() {
        _expression.value = ""
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

                val historyItem = CalculationHistoryItem(
                    expression = expr,
                    result = formatted
                )
                viewModelScope.launch {
                    preferences.addHistoryItem(historyItem)
                    preferences.saveCalculationState(expr, formatted)
                }
            }
            is EvaluationResult.Error -> {
                _errorMessage.value = eval.message
            }
        }
    }

    private fun tryLivePreview() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) {
            _result.value = "0"
            return
        }
        // Live preview only if expression has operators/functions
        val hasOp = expr.any { it in "+-*÷×/^%!scl" }
        if (hasOp) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                when (val eval = evaluator.evaluate(expr, angleMode.value)) {
                    is EvaluationResult.Success -> {
                        _result.value = eval.formatted
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
        _result.value = item.result
        _errorMessage.value = null
        justCalculated = false
    }

    fun clearHistory() {
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
        viewModelScope.launch {
            preferences.resetAll()
            _expression.value = ""
            _result.value = "0"
            _errorMessage.value = null
            _isScientificExpanded.value = false
        }
    }

    fun getEffectiveApiLevel(): Int {
        return if (simulatedApiEnabled.value) simulatedApiLevel.value else Build.VERSION.SDK_INT
    }
}
