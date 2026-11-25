package com.limonatalp4.glasscalcproject.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppIconOption
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.CalculationHistoryItem
import com.limonatalp4.glasscalcproject.data.preferences.GlassCalcPreferences
import com.limonatalp4.glasscalcproject.ui.util.AppIconManager
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.round

@Immutable
data class CalculatorDisplayState(
    val expression: String = "",
    val cursorPosition: Int = 0,
    val result: String = "0",
    val errorMessage: String? = null
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext ?: application
    private val preferences = GlassCalcPreferences(application)
    private val evaluator = ExpressionEvaluator()

    private val _isPowerSaveMode = MutableStateFlow(readPowerSaveMode(appContext))
    val isPowerSaveMode: StateFlow<Boolean> = _isPowerSaveMode.asStateFlow()

    private var powerSaveReceiver: BroadcastReceiver? = null
    private var powerSaveObserver: ContentObserver? = null

    private val displayStateLock = Any()
    private val inputRevision = AtomicLong(0L)

    private val _displayState = MutableStateFlow(CalculatorDisplayState())
    val displayState: StateFlow<CalculatorDisplayState> = _displayState.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _cursorPosition = MutableStateFlow(0)
    val cursorPosition: StateFlow<Int> = _cursorPosition.asStateFlow()

    private val _result = MutableStateFlow("0")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private fun updateDisplayState(
        bumpRevision: Boolean = true,
        transform: (CalculatorDisplayState) -> CalculatorDisplayState
    ): Pair<CalculatorDisplayState, Long> {
        return synchronized(displayStateLock) {
            val next = transform(_displayState.value)
            val rev = if (bumpRevision) inputRevision.incrementAndGet() else inputRevision.get()
            publishDisplayStateLocked(next)
            next to rev
        }
    }

    private fun publishDisplayStateLocked(next: CalculatorDisplayState) {
        _displayState.value = next
        if (_expression.value != next.expression) {
            _expression.value = next.expression
        }
        if (_cursorPosition.value != next.cursorPosition) {
            _cursorPosition.value = next.cursorPosition
        }
        if (_result.value != next.result) {
            _result.value = next.result
        }
        if (_errorMessage.value != next.errorMessage) {
            _errorMessage.value = next.errorMessage
        }
    }

    fun setCursorPosition(position: Int) {
        updateDisplayState(bumpRevision = false) { current ->
            val clamped = if (current.expression.isEmpty()) 0 else position.coerceIn(0, current.expression.length)
            current.copy(cursorPosition = clamped)
        }
    }

    fun onDirectExpressionChange(newExpression: String, newCursorPosition: Int) {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            justCalculated = false
            val clampedCursor = if (newExpression.isEmpty()) 0 else newCursorPosition.coerceIn(0, newExpression.length)
            current.copy(
                expression = newExpression,
                cursorPosition = clampedCursor,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

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
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            justCalculated = false
            current.copy(
                expression = cleaned,
                cursorPosition = cleaned.length,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
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
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            justCalculated = false
            current.copy(
                expression = newExpr,
                cursorPosition = newExpr.length,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    private fun formatGraphCoordinate(value: Double): String {
        if (abs(value) < 1e-9) return "0"
        if (abs(value - round(value)) < 1e-7 && abs(value) < 1e12) {
            return round(value).toLong().toString().replace("-", "−")
        }
        val formatter = GRAPH_COORD_FORMAT.get()
            ?: DecimalFormat("#,##0.####", DecimalFormatSymbols(Locale.GERMANY)).apply {
                isGroupingUsed = false
            }
        return formatter.format(value).replace("-", "−")
    }

    fun insertFormula(formulaExpression: String) {
        if (formulaExpression.isBlank()) return
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            val curr = current.expression
            val pos = current.cursorPosition.coerceIn(0, curr.length)
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
            current.copy(
                expression = updated,
                cursorPosition = nextCursor,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    val formulas: List<MathFormula> = FormulaCatalog.FORMULAS

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

    private val _selectedAppIconOverride = MutableStateFlow<AppIconOption?>(null)
    @Volatile
    private var pendingAppIconToApply: AppIconOption? = null

    val currentAppIcon: StateFlow<AppIconOption> = combine(
        preferences.appIconFlow,
        _selectedAppIconOverride,
        simulatedApiEnabled,
        simulatedApiLevel
    ) { savedIcon, overrideIcon, simEnabled, simLevel ->
        val activeSelection = overrideIcon ?: savedIcon
        val effective = minOf(Build.VERSION.SDK_INT, if (simEnabled) simLevel else Build.VERSION.SDK_INT)
        if (activeSelection.requiresMinApi > 0 && effective < activeSelection.requiresMinApi) {
            AppIconOption.CLASSIC
        } else {
            activeSelection
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppIconOption.CLASSIC)

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
    private var equalsJob: Job? = null

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
                if (savedExpr.isNotEmpty()) {
                    synchronized(displayStateLock) {
                        if (_displayState.value.expression.isEmpty()) {
                            inputRevision.incrementAndGet()
                            publishDisplayStateLocked(
                                CalculatorDisplayState(
                                    expression = savedExpr,
                                    cursorPosition = savedExpr.length,
                                    result = savedRes,
                                    errorMessage = null
                                )
                            )
                        }
                    }
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

        // Register reactive Power Save Mode listeners (BroadcastReceiver + low_power ContentObserver)
        refreshPowerSaveMode()
        registerPowerSaveListeners()

        // Keep HapticUtil intensity synced with persisted preference
        viewModelScope.launch {
            preferences.hapticIntensityFlow.collect { intensity ->
                HapticUtil.currentIntensityLevel = intensity
            }
        }

        // Automatically queue launcher alias fallback when Material You icon becomes unsupported (API < 33)
        viewModelScope.launch {
            var lastSyncedAppIcon: AppIconOption? = null
            combine(
                preferences.appIconFlow,
                _selectedAppIconOverride,
                simulatedApiEnabled,
                simulatedApiLevel
            ) { savedIcon, overrideIcon, simEnabled, simLevel ->
                val activeSelection = overrideIcon ?: savedIcon
                val effective = minOf(Build.VERSION.SDK_INT, if (simEnabled) simLevel else Build.VERSION.SDK_INT)
                val unsupported = activeSelection.requiresMinApi > 0 && effective < activeSelection.requiresMinApi
                val resolved = if (unsupported) AppIconOption.CLASSIC else activeSelection
                resolved to unsupported
            }.collect { (resolved, unsupportedFallback) ->
                if (unsupportedFallback || (lastSyncedAppIcon != null && lastSyncedAppIcon != resolved)) {
                    pendingAppIconToApply = resolved
                }
                lastSyncedAppIcon = resolved
            }
        }
    }

    fun refreshPowerSaveMode() {
        val latest = readPowerSaveMode(appContext)
        if (_isPowerSaveMode.value != latest) {
            _isPowerSaveMode.value = latest
        }
    }

    private fun registerPowerSaveListeners() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                    refreshPowerSaveMode()
                }
            }
        }
        val receiverRegistered = runCatching {
            ContextCompat.registerReceiver(
                appContext,
                receiver,
                IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED
            )
        }.isSuccess
        if (receiverRegistered) {
            powerSaveReceiver = receiver
        }

        val lowPowerUri = runCatching { Settings.Global.getUriFor("low_power") }.getOrNull()
        if (lowPowerUri != null) {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    refreshPowerSaveMode()
                }
            }
            val observerRegistered = runCatching {
                appContext.contentResolver.registerContentObserver(lowPowerUri, false, observer)
            }.isSuccess
            if (observerRegistered) {
                powerSaveObserver = observer
            }
        }
    }

    private fun readPowerSaveMode(context: Context): Boolean {
        val powerManager = runCatching {
            context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        }.getOrNull()
        if (powerManager != null) {
            return runCatching { powerManager.isPowerSaveMode }.getOrDefault(false)
        }
        return runCatching {
            Settings.Global.getInt(context.contentResolver, "low_power", 0) == 1
        }.getOrDefault(false)
    }

    override fun onCleared() {
        flushPendingAppIconChange()
        super.onCleared()
        powerSaveReceiver?.let { receiver ->
            runCatching { appContext.unregisterReceiver(receiver) }
            powerSaveReceiver = null
        }
        powerSaveObserver?.let { observer ->
            runCatching { appContext.contentResolver.unregisterContentObserver(observer) }
            powerSaveObserver = null
        }
    }

    fun onAppStop() {
        // Save current calculation state crash-safely on ON_STOP
        val snapshot = synchronized(displayStateLock) { _displayState.value }
        viewModelScope.launch(Dispatchers.IO) {
            preferences.saveCalculationState(snapshot.expression, snapshot.result)
        }
        // Launcher-<activity-alias> erst im Hintergrund umschalten, damit die laufende Activity nicht zerstört wird
        flushPendingAppIconChange()
    }

    private fun flushPendingAppIconChange() {
        val iconToApply = pendingAppIconToApply ?: return
        pendingAppIconToApply = null
        AppIconManager.applyAppIconAsync(appContext, iconToApply)
    }

    fun onAppStart() {
        // Re-check device locale and power save mode in case system settings changed while in background
        _systemLocaleRefreshTrigger.value += 1
        refreshPowerSaveMode()
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
        val snapshot = synchronized(displayStateLock) { _displayState.value }
        viewModelScope.launch {
            preferences.saveCalculationState(snapshot.expression, snapshot.result)
            throw RuntimeException("Künstlicher Test-Absturz zur Überprüfung des Zustandserhalts!")
        }
    }

    private fun getValidChainingResultLocked(current: CalculatorDisplayState): String? {
        val res = current.result
        if (res.isNotEmpty() && current.errorMessage == null && res != "Error") {
            return res
        }
        return null
    }

    fun onCharacterInput(char: String) {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            var curr = current.expression
            var pos = current.cursorPosition.coerceIn(0, curr.length)

            if (justCalculated) {
                val chainingRes = getValidChainingResultLocked(current)
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
            current.copy(
                expression = updated,
                cursorPosition = pos + char.length,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onScientificFunction(fnName: String) {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            var curr = current.expression
            var pos = current.cursorPosition.coerceIn(0, curr.length)

            if (justCalculated) {
                val chainingRes = getValidChainingResultLocked(current)
                if (chainingRes != null) {
                    val expr = "$fnName($chainingRes)"
                    justCalculated = false
                    return@updateDisplayState current.copy(
                        expression = expr,
                        cursorPosition = expr.length,
                        errorMessage = null
                    )
                } else {
                    curr = ""
                    pos = 0
                }
            }
            justCalculated = false

            val insertStr = "$fnName("
            val updated = curr.substring(0, pos) + insertStr + curr.substring(pos)
            current.copy(
                expression = updated,
                cursorPosition = pos + insertStr.length,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onConstant(constant: String) {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            var curr = current.expression
            var pos = current.cursorPosition.coerceIn(0, curr.length)

            if (justCalculated) {
                curr = ""
                pos = 0
            }
            justCalculated = false

            val updated = curr.substring(0, pos) + constant + curr.substring(pos)
            current.copy(
                expression = updated,
                cursorPosition = pos + constant.length,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onPower(exponent: String) {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            var curr = current.expression
            var pos = current.cursorPosition.coerceIn(0, curr.length)

            if (justCalculated) {
                val chainingRes = getValidChainingResultLocked(current)
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
            current.copy(
                expression = updated,
                cursorPosition = pos + insertStr.length,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onFractionSlash() {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            var curr = current.expression
            var pos = current.cursorPosition.coerceIn(0, curr.length)

            if (justCalculated) {
                val chainingRes = getValidChainingResultLocked(current)
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
            current.copy(
                expression = updated,
                cursorPosition = pos + 1,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onNthRoot() {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            var curr = current.expression
            var pos = current.cursorPosition.coerceIn(0, curr.length)

            if (justCalculated) {
                val chainingRes = getValidChainingResultLocked(current)
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
            current.copy(
                expression = updated,
                cursorPosition = pos + insertStr.length,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onToggleSign() {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            justCalculated = false
            val curr = current.expression
            val pos = current.cursorPosition.coerceIn(0, curr.length)

            val (nextExpr, nextPos) = if (curr.isEmpty()) {
                "−" to 1
            } else if (curr.startsWith("−") || curr.startsWith("-")) {
                curr.substring(1) to (pos - 1).coerceAtLeast(0)
            } else {
                "−$curr" to (pos + 1)
            }
            current.copy(
                expression = nextExpr,
                cursorPosition = nextPos,
                errorMessage = null
            )
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onDelete() {
        equalsJob?.cancel()
        val (snapshot, rev) = updateDisplayState { current ->
            justCalculated = false
            val curr = current.expression
            val pos = current.cursorPosition.coerceIn(0, curr.length)

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
                current.copy(
                    expression = newTextBefore + textAfterCursor,
                    cursorPosition = pos - deletedLen,
                    errorMessage = null
                )
            } else {
                current.copy(errorMessage = null)
            }
        }
        tryLivePreview(expr = snapshot.expression, targetRevision = rev)
    }

    fun onClearAll() {
        previewJob?.cancel()
        equalsJob?.cancel()
        updateDisplayState {
            justCalculated = false
            CalculatorDisplayState(
                expression = "",
                cursorPosition = 0,
                result = "0",
                errorMessage = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            preferences.saveCalculationState("", "0")
        }
    }

    fun onEquals() {
        val (expr, targetRev) = synchronized(displayStateLock) {
            _displayState.value.expression.trim() to inputRevision.get()
        }
        if (expr.isEmpty()) return
        val mode = angleMode.value

        previewJob?.cancel()
        equalsJob?.cancel()
        equalsJob = viewModelScope.launch(Dispatchers.Default) {
            val eval = evaluator.evaluateCancellable(expr, mode)
            if (!isActive) return@launch

            when (eval) {
                is EvaluationResult.Success -> {
                    val formatted = eval.formatted
                    var committed = false
                    synchronized(displayStateLock) {
                        if (inputRevision.get() == targetRev && _displayState.value.expression.trim() == expr) {
                            justCalculated = true
                            val currentExpr = _displayState.value.expression
                            publishDisplayStateLocked(
                                _displayState.value.copy(
                                    result = formatted,
                                    errorMessage = null,
                                    cursorPosition = currentExpr.length
                                )
                            )
                            committed = true
                        }
                    }
                    if (!committed) return@launch

                    val historyItem = CalculationHistoryItem(
                        expression = expr,
                        result = formatted
                    )
                    isHistoryCleared = false
                    _history.value = (listOf(historyItem) + _history.value).take(20)
                    withContext(Dispatchers.IO) {
                        preferences.addHistoryItem(historyItem)
                        preferences.saveCalculationState(expr, formatted)
                    }
                }
                is EvaluationResult.Error -> {
                    val msg = appContext.getString(eval.messageResId)
                    synchronized(displayStateLock) {
                        if (inputRevision.get() == targetRev && _displayState.value.expression.trim() == expr) {
                            publishDisplayStateLocked(
                                _displayState.value.copy(
                                    result = "Error",
                                    errorMessage = msg
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun tryLivePreview(
        expr: String = _displayState.value.expression,
        targetRevision: Long = inputRevision.get(),
        overrideAngleMode: AngleMode = angleMode.value
    ) {
        previewJob?.cancel()
        val trimmedExpr = expr.trim()
        if (trimmedExpr.isEmpty()) {
            synchronized(displayStateLock) {
                if (inputRevision.get() == targetRevision) {
                    publishDisplayStateLocked(_displayState.value.copy(result = "0"))
                }
            }
            return
        }
        // Live preview only if expression has operators/functions/constants (evaluated asynchronously on Dispatchers.Default)
        val hasOp = trimmedExpr.any { it in "+-−*÷×/⁄^%!scl∫√ⁿeπdat;²³" }
        if (hasOp && _isAutoResultEnabled.value) {
            previewJob = viewModelScope.launch(Dispatchers.Default) {
                val eval = evaluator.evaluateCancellable(trimmedExpr, overrideAngleMode)
                if (!isActive) return@launch
                if (eval is EvaluationResult.Success) {
                    synchronized(displayStateLock) {
                        if (inputRevision.get() == targetRevision && _displayState.value.expression.trim() == trimmedExpr) {
                            publishDisplayStateLocked(_displayState.value.copy(result = eval.formatted))
                        }
                    }
                }
            }
        }
    }

    private fun isOperator(s: String): Boolean {
        return s in listOf("+", "-", "−", "*", "/", "⁄", "×", "÷", "%", "^")
    }

    fun toggleScientificPanel() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    fun toggleAngleMode() {
        val next = if (angleMode.value == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        viewModelScope.launch {
            preferences.setAngleMode(next)
        }
        val (expr, rev) = synchronized(displayStateLock) {
            _displayState.value.expression to inputRevision.get()
        }
        tryLivePreview(expr = expr, targetRevision = rev, overrideAngleMode = next)
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferences.setTheme(theme)
        }
    }

    fun setAppIcon(icon: AppIconOption) {
        val effective = minOf(Build.VERSION.SDK_INT, getEffectiveApiLevel())
        val safeIcon = if (icon.requiresMinApi > 0 && effective < icon.requiresMinApi) {
            AppIconOption.CLASSIC
        } else {
            icon
        }
        // UI-Vorschau & DataStore sofort aktualisieren; System-Icon wird beim Wechsel in den Hintergrund umgeschaltet
        _selectedAppIconOverride.value = safeIcon
        pendingAppIconToApply = safeIcon
        viewModelScope.launch(Dispatchers.IO) {
            preferences.setAppIcon(safeIcon)
        }
    }

    fun restoreHistoryItem(item: CalculationHistoryItem) {
        previewJob?.cancel()
        equalsJob?.cancel()
        updateDisplayState {
            justCalculated = false
            CalculatorDisplayState(
                expression = item.expression,
                cursorPosition = item.expression.length,
                result = item.result,
                errorMessage = null
            )
        }
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
        previewJob?.cancel()
        equalsJob?.cancel()
        isHistoryCleared = true
        _history.value = emptyList()
        _isGraphLocked.value = false
        HapticUtil.currentIntensityLevel = 5
        _selectedAppIconOverride.value = AppIconOption.CLASSIC
        pendingAppIconToApply = AppIconOption.CLASSIC
        updateDisplayState {
            justCalculated = false
            CalculatorDisplayState(
                expression = "",
                cursorPosition = 0,
                result = "0",
                errorMessage = null
            )
        }
        _isScientificExpanded.value = false
        viewModelScope.launch {
            preferences.resetAll()
        }
    }

    fun getEffectiveApiLevel(): Int {
        return if (simulatedApiEnabled.value) simulatedApiLevel.value else Build.VERSION.SDK_INT
    }

    companion object {
        private val GRAPH_COORD_FORMAT = ThreadLocal.withInitial {
            DecimalFormat("#,##0.####", DecimalFormatSymbols(Locale.GERMANY)).apply {
                isGroupingUsed = false
            }
        }
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
