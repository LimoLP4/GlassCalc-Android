package com.limonatalp4.glasscalcproject

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.ui.theme.GlassCalcTheme
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun materialYouDoesNotSwitchToTiefschwarzWhenPowerSaverIsActive() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val shadowPowerManager = Shadows.shadowOf(powerManager)
        shadowPowerManager.setIsPowerSaveMode(true)

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
        assertFalse("Material You must never switch to Tiefschwarz on Power Saver", latestIsTiefschwarz)
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
}

