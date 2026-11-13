package com.limonatalp4.glasscalcproject

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limonatalp4.glasscalcproject.ui.screens.CalculatorScreen
import com.limonatalp4.glasscalcproject.ui.theme.GlassCalcTheme
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support
        enableEdgeToEdge()

        // Set UI first so MainActivity is not blocked during launch
        setContent {
            GlassCalcAppRoot()
        }
    }
}

@Composable
fun GlassCalcAppRoot() {
    val viewModel: CalculatorViewModel = viewModel()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val simulatedApiEnabled by viewModel.simulatedApiEnabled.collectAsState()
    val simulatedApiLevel by viewModel.simulatedApiLevel.collectAsState()
    val effectiveLanguage by viewModel.effectiveLanguage.collectAsState()
    val isLanguageLoaded by viewModel.isLanguageLoaded.collectAsState()

    // Asynchrone und sichere Sprach-Initialisierung nach dem UI-Aufbau
    LaunchedEffect(effectiveLanguage, isLanguageLoaded) {
        if (!isLanguageLoaded) return@LaunchedEffect
        try {
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val currentTag = if (currentLocales.isEmpty) "" else currentLocales.toLanguageTags()
            val currentPrimaryTag = currentTag
                .split(',')
                .firstOrNull()
                ?.trim()
                ?.lowercase(java.util.Locale.ROOT)
                ?.substringBefore('-')
                .orEmpty()

            // Nur aktualisieren, wenn sich die effektive Sprache tatsächlich unterscheidet
            if (currentPrimaryTag != effectiveLanguage) {
                kotlinx.coroutines.delay(60)
                val localeList = LocaleListCompat.forLanguageTags(effectiveLanguage)
                AppCompatDelegate.setApplicationLocales(localeList)
            }
        } catch (e: Throwable) {
            // Crash-Schutz: Bei Fehlern sicher auf Englisch zurückfallen
            Log.e("GlassCalc", "Fehler bei Sprach-Initialisierung, Fallback auf Englisch", e)
            runCatching {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(CalculatorViewModel.FALLBACK_LANGUAGE_TAG)
                )
            }
        }
    }

    val effectiveApi = if (simulatedApiEnabled) simulatedApiLevel else Build.VERSION.SDK_INT

    GlassCalcTheme(
        selectedTheme = currentTheme,
        simulatedApiLevel = effectiveApi
    ) {
        CalculatorScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )
    }
}
