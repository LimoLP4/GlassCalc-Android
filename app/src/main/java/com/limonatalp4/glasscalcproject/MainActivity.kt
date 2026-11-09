package com.limonatalp4.glasscalcproject

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limonatalp4.glasscalcproject.ui.screens.CalculatorScreen
import com.limonatalp4.glasscalcproject.ui.theme.GlassCalcTheme
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

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
