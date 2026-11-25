package com.limonatalp4.glasscalcproject

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.graphics.ColorUtils
import androidx.test.core.app.ApplicationProvider
import com.limonatalp4.glasscalcproject.data.model.AccentColorOption
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.ui.theme.DarkPrimaryBlue
import com.limonatalp4.glasscalcproject.ui.theme.GlassCalcTheme
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.theme.PrimaryBlue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemePowerSaveTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun automaticThemeReactsDynamicallyToPowerSaveModeBroadcast() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(false)

        var latestIsTiefschwarz = false
        var latestIsDark = false
        var latestBgColor = Color.Unspecified
        var latestCardBgColor = Color.Unspecified

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.AUTOMATIC) {
                val glassProps = LocalGlassProperties.current
                val colorScheme = MaterialTheme.colorScheme
                latestIsTiefschwarz = glassProps.isTiefschwarz
                latestIsDark = glassProps.isDark
                latestBgColor = colorScheme.background
                latestCardBgColor = glassProps.cardBackground
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(latestIsTiefschwarz)

        // Enable Power Saver Mode and broadcast system change
        shadowPowerManager.setIsPowerSaveMode(true)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertTrue(latestIsTiefschwarz)
        assertTrue(latestIsDark)
        assertEquals(Color.Black, latestBgColor)
        assertEquals(Color.Black, latestCardBgColor)

        // Disable Power Saver Mode and verify fallback away from Tiefschwarz
        shadowPowerManager.setIsPowerSaveMode(false)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertFalse(latestIsTiefschwarz)
    }

    @Test
    @Config(sdk = [35], qualifiers = "night")
    fun automaticThemeReturnsToStandardDarkWhenPowerSaveModeIsDisabledInDarkSystemMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(false)

        var latestIsTiefschwarz = true
        var latestIsDark = false
        var latestBgColor = Color.Unspecified

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.AUTOMATIC) {
                val glassProps = LocalGlassProperties.current
                val colorScheme = MaterialTheme.colorScheme
                latestIsTiefschwarz = glassProps.isTiefschwarz
                latestIsDark = glassProps.isDark
                latestBgColor = colorScheme.background
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(latestIsTiefschwarz)
        assertTrue(latestIsDark)
        assertEquals(Color(0xFF1A1C1E), latestBgColor)

        // Enable Power Save -> AMOLED (Tiefschwarz)
        shadowPowerManager.setIsPowerSaveMode(true)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertTrue(latestIsTiefschwarz)
        assertEquals(Color.Black, latestBgColor)

        // Disable Power Save -> immediately back to Standard Dark
        shadowPowerManager.setIsPowerSaveMode(false)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertFalse(latestIsTiefschwarz)
        assertTrue(latestIsDark)
        assertEquals(Color(0xFF1A1C1E), latestBgColor)
    }

    @Test
    fun calculatorViewModelReactsBidirectionallyToPowerSaveBroadcast() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val powerManager = app.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(false)

        val viewModel = com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel(app)
        assertFalse(viewModel.isPowerSaveMode.value)

        shadowPowerManager.setIsPowerSaveMode(true)
        app.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertTrue(viewModel.isPowerSaveMode.value)

        shadowPowerManager.setIsPowerSaveMode(false)
        app.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertFalse(viewModel.isPowerSaveMode.value)
    }

    @Test
    fun materialYouDoesNotSwitchToTiefschwarzWhenPowerSaverTogglesOnAndOff() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(false)

        var latestIsTiefschwarz = false
        var latestIsMaterialYou = false

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.MATERIAL_YOU, simulatedApiLevel = 35) {
                val glassProps = LocalGlassProperties.current
                latestIsTiefschwarz = glassProps.isTiefschwarz
                latestIsMaterialYou = glassProps.isMaterialYou
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(latestIsTiefschwarz)
        assertTrue(latestIsMaterialYou)

        shadowPowerManager.setIsPowerSaveMode(true)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertFalse("Material You must never switch to Tiefschwarz on Power Saver", latestIsTiefschwarz)
        assertTrue(latestIsMaterialYou)

        shadowPowerManager.setIsPowerSaveMode(false)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertFalse(latestIsTiefschwarz)
        assertTrue(latestIsMaterialYou)
    }

    @Test
    fun manualWhiteThemeDoesNotSwitchToTiefschwarzWhenPowerSaverIsActive() {
        var isWhiteModeTiefschwarz = true

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.WHITE_MODE, powerSaveModeOverride = true) {
                isWhiteModeTiefschwarz = LocalGlassProperties.current.isTiefschwarz
            }
        }
        composeTestRule.waitForIdle()
        assertFalse(isWhiteModeTiefschwarz)
    }

    @Test
    fun manualStandardDarkThemeDoesNotSwitchToTiefschwarzWhenPowerSaverIsActive() {
        var isStandardDarkTiefschwarz = true

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.STANDARD_DARK, powerSaveModeOverride = true) {
                isStandardDarkTiefschwarz = LocalGlassProperties.current.isTiefschwarz
            }
        }
        composeTestRule.waitForIdle()
        assertFalse(isStandardDarkTiefschwarz)
    }

    @Test
    fun manualAmoledThemeRemainsUnchangedWhenPowerSaverTogglesOnAndOff() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(true)

        var latestIsTiefschwarz = false
        var latestBgColor = Color.Unspecified

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.DARK_MODE) {
                latestIsTiefschwarz = LocalGlassProperties.current.isTiefschwarz
                latestBgColor = MaterialTheme.colorScheme.background
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(latestIsTiefschwarz)
        assertEquals(Color.Black, latestBgColor)

        // Power Save OFF must keep manual AMOLED intact
        shadowPowerManager.setIsPowerSaveMode(false)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertTrue(latestIsTiefschwarz)
        assertEquals(Color.Black, latestBgColor)

        // Power Save ON again must still keep manual AMOLED intact
        shadowPowerManager.setIsPowerSaveMode(true)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertTrue(latestIsTiefschwarz)
        assertEquals(Color.Black, latestBgColor)
    }

    @Test
    fun manualStandardDarkThemeRemainsUnchangedWhenPowerSaverTogglesOnAndOff() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(false)

        var latestIsTiefschwarz = true
        var latestBgColor = Color.Unspecified

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.STANDARD_DARK) {
                latestIsTiefschwarz = LocalGlassProperties.current.isTiefschwarz
                latestBgColor = MaterialTheme.colorScheme.background
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(latestIsTiefschwarz)
        assertEquals(Color(0xFF1A1C1E), latestBgColor)

        shadowPowerManager.setIsPowerSaveMode(true)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertFalse(latestIsTiefschwarz)
        assertEquals(Color(0xFF1A1C1E), latestBgColor)

        shadowPowerManager.setIsPowerSaveMode(false)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertFalse(latestIsTiefschwarz)
        assertEquals(Color(0xFF1A1C1E), latestBgColor)
    }

    @Test
    fun manualWhiteThemeRemainsUnchangedWhenPowerSaverTogglesOnAndOff() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(false)

        var latestIsTiefschwarz = true
        var latestIsDark = true

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.WHITE_MODE) {
                val props = LocalGlassProperties.current
                latestIsTiefschwarz = props.isTiefschwarz
                latestIsDark = props.isDark
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(latestIsTiefschwarz)
        assertFalse(latestIsDark)

        shadowPowerManager.setIsPowerSaveMode(true)
        context.sendBroadcast(Intent(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        composeTestRule.waitForIdle()

        assertFalse(latestIsTiefschwarz)
        assertFalse(latestIsDark)
    }

    @Test
    fun customAccentColorOverridesNonMaterialYouThemesWithAccessibleContrast() {
        val customPurple = AccentColorOption.PURPLE.colorArgb!!

        var defaultLightPrimary = Color.Unspecified
        var customLightPrimary = Color.Unspecified
        var customLightOnPrimary = Color.Unspecified
        var customLightContainer = Color.Unspecified
        var customLightOnContainer = Color.Unspecified

        var defaultDarkPrimary = Color.Unspecified
        var customDarkPrimary = Color.Unspecified
        var customDarkOnPrimary = Color.Unspecified
        var customAmoledPrimary = Color.Unspecified

        composeTestRule.setContent {
            GlassCalcTheme(selectedTheme = AppTheme.WHITE_MODE, customAccentColorArgb = null) {
                defaultLightPrimary = MaterialTheme.colorScheme.primary
            }
            GlassCalcTheme(selectedTheme = AppTheme.WHITE_MODE, customAccentColorArgb = customPurple) {
                customLightPrimary = MaterialTheme.colorScheme.primary
                customLightOnPrimary = MaterialTheme.colorScheme.onPrimary
                customLightContainer = MaterialTheme.colorScheme.primaryContainer
                customLightOnContainer = MaterialTheme.colorScheme.onPrimaryContainer
            }
            GlassCalcTheme(selectedTheme = AppTheme.STANDARD_DARK, customAccentColorArgb = null) {
                defaultDarkPrimary = MaterialTheme.colorScheme.primary
            }
            GlassCalcTheme(selectedTheme = AppTheme.STANDARD_DARK, customAccentColorArgb = customPurple) {
                customDarkPrimary = MaterialTheme.colorScheme.primary
                customDarkOnPrimary = MaterialTheme.colorScheme.onPrimary
            }
            GlassCalcTheme(selectedTheme = AppTheme.DARK_MODE, customAccentColorArgb = customPurple) {
                customAmoledPrimary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(PrimaryBlue, defaultLightPrimary)
        assertEquals(DarkPrimaryBlue, defaultDarkPrimary)
        assertNotEquals(defaultLightPrimary, customLightPrimary)
        assertNotEquals(defaultDarkPrimary, customDarkPrimary)
        assertEquals(customDarkPrimary, customAmoledPrimary)

        assertTrue(
            ColorUtils.calculateContrast(customLightOnPrimary.toArgb(), customLightPrimary.toArgb()) >= 4.5
        )
        assertTrue(
            ColorUtils.calculateContrast(customLightOnContainer.toArgb(), customLightContainer.toArgb()) >= 4.5
        )
        assertTrue(
            ColorUtils.calculateContrast(customDarkOnPrimary.toArgb(), customDarkPrimary.toArgb()) >= 4.5
        )
    }

    @Test
    fun materialYouThemeIgnoresCustomAccentColor() {
        val customCoral = AccentColorOption.CORAL.colorArgb!!
        var materialYouDefaultPrimary = Color.Unspecified
        var materialYouWithCustomPrimary = Color.Unspecified

        composeTestRule.setContent {
            GlassCalcTheme(
                selectedTheme = AppTheme.MATERIAL_YOU,
                simulatedApiLevel = 35,
                customAccentColorArgb = null
            ) {
                materialYouDefaultPrimary = MaterialTheme.colorScheme.primary
            }
            GlassCalcTheme(
                selectedTheme = AppTheme.MATERIAL_YOU,
                simulatedApiLevel = 35,
                customAccentColorArgb = customCoral
            ) {
                materialYouWithCustomPrimary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(materialYouDefaultPrimary, materialYouWithCustomPrimary)
    }
}

