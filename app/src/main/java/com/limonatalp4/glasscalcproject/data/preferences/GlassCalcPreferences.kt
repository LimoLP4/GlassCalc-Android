package com.limonatalp4.glasscalcproject.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.CalculationHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "glass_calc_preferences")

class GlassCalcPreferences(private val context: Context) {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val ANGLE_MODE = stringPreferencesKey("angle_mode")
        val SAVED_EXPRESSION = stringPreferencesKey("saved_expression")
        val SAVED_RESULT = stringPreferencesKey("saved_result")
        val CALCULATION_HISTORY = stringPreferencesKey("calculation_history")
        val SIMULATED_API_ENABLED = booleanPreferencesKey("simulated_api_enabled")
        val SIMULATED_API_LEVEL = intPreferencesKey("simulated_api_level")
        val DEFAULT_LENGTH_UNIT = stringPreferencesKey("default_length_unit")
        val DEFAULT_MASS_UNIT = stringPreferencesKey("default_mass_unit")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val AUTO_RESULT_ENABLED = booleanPreferencesKey("auto_result_enabled")
    }

    private val safeDataFlow: Flow<Preferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val themeFlow: Flow<AppTheme?> = safeDataFlow.map { prefs ->
        val name = prefs[PreferencesKeys.APP_THEME] ?: return@map null
        try {
            AppTheme.valueOf(name)
        } catch (_: Exception) {
            null
        }
    }

    val angleModeFlow: Flow<AngleMode> = safeDataFlow.map { prefs ->
        val name = prefs[PreferencesKeys.ANGLE_MODE] ?: AngleMode.DEG.name
        try {
            AngleMode.valueOf(name)
        } catch (_: Exception) {
            AngleMode.DEG
        }
    }

    val savedStateFlow: Flow<Pair<String, String>> = safeDataFlow.map { prefs ->
        val expr = prefs[PreferencesKeys.SAVED_EXPRESSION] ?: ""
        val res = prefs[PreferencesKeys.SAVED_RESULT] ?: "0"
        Pair(expr, res)
    }

    val historyFlow: Flow<List<CalculationHistoryItem>> = safeDataFlow.map { prefs ->
        val raw = prefs[PreferencesKeys.CALCULATION_HISTORY] ?: "[]"
        parseHistoryJson(raw)
    }

    val simulatedApiEnabledFlow: Flow<Boolean> = safeDataFlow.map { prefs ->
        prefs[PreferencesKeys.SIMULATED_API_ENABLED] ?: false
    }

    val simulatedApiLevelFlow: Flow<Int> = safeDataFlow.map { prefs ->
        prefs[PreferencesKeys.SIMULATED_API_LEVEL] ?: 30
    }

    val defaultLengthUnitFlow: Flow<String> = safeDataFlow.map { prefs ->
        prefs[PreferencesKeys.DEFAULT_LENGTH_UNIT] ?: "m"
    }

    val defaultMassUnitFlow: Flow<String> = safeDataFlow.map { prefs ->
        prefs[PreferencesKeys.DEFAULT_MASS_UNIT] ?: "kg"
    }

    val hapticFeedbackEnabledFlow: Flow<Boolean> = safeDataFlow.map { prefs ->
        prefs[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true
    }

    val autoResultEnabledFlow: Flow<Boolean> = safeDataFlow.map { prefs ->
        prefs[PreferencesKeys.AUTO_RESULT_ENABLED] ?: true
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun setAutoResultEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.AUTO_RESULT_ENABLED] = enabled
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.APP_THEME] = theme.name
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun setAngleMode(mode: AngleMode) {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.ANGLE_MODE] = mode.name
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun saveCalculationState(expression: String, result: String) {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.SAVED_EXPRESSION] = expression
                prefs[PreferencesKeys.SAVED_RESULT] = result
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun addHistoryItem(item: CalculationHistoryItem) {
        try {
            context.dataStore.edit { prefs ->
                val raw = prefs[PreferencesKeys.CALCULATION_HISTORY] ?: "[]"
                val currentList = parseHistoryJson(raw).toMutableList()
                currentList.add(0, item)
                val trimmed = currentList.take(20)
                prefs[PreferencesKeys.CALCULATION_HISTORY] = serializeHistoryJson(trimmed)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun clearHistory() {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.CALCULATION_HISTORY] = "[]"
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun setSimulatedApi(enabled: Boolean, level: Int) {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.SIMULATED_API_ENABLED] = enabled
                prefs[PreferencesKeys.SIMULATED_API_LEVEL] = level
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun setDefaultUnits(lengthUnit: String, massUnit: String) {
        try {
            context.dataStore.edit { prefs ->
                prefs[PreferencesKeys.DEFAULT_LENGTH_UNIT] = lengthUnit
                prefs[PreferencesKeys.DEFAULT_MASS_UNIT] = massUnit
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    suspend fun resetAll() {
        try {
            context.dataStore.edit { prefs ->
                prefs.clear()
                prefs[PreferencesKeys.SAVED_EXPRESSION] = ""
                prefs[PreferencesKeys.SAVED_RESULT] = "0"
                prefs[PreferencesKeys.CALCULATION_HISTORY] = "[]"
                prefs[PreferencesKeys.DEFAULT_LENGTH_UNIT] = "m"
                prefs[PreferencesKeys.DEFAULT_MASS_UNIT] = "kg"
                prefs[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = true
                prefs[PreferencesKeys.AUTO_RESULT_ENABLED] = true
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    private fun parseHistoryJson(jsonStr: String): List<CalculationHistoryItem> {
        val list = mutableListOf<CalculationHistoryItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalculationHistoryItem(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        expression = obj.optString("expression", ""),
                        result = obj.optString("result", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return list
    }

    private fun serializeHistoryJson(list: List<CalculationHistoryItem>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("expression", item.expression)
            obj.put("result", item.result)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        return array.toString()
    }
}
