package com.limonatalp4.glasscalcproject

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppIconOption
import com.limonatalp4.glasscalcproject.data.preferences.GlassCalcPreferences
import com.limonatalp4.glasscalcproject.ui.util.AppIconManager
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CalculatorViewModelTest {

    @Before
    fun setUp() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        GlassCalcPreferences(app).resetAll()
        AppIconManager.applyAppIcon(app, AppIconOption.CLASSIC)
    }

    @After
    fun tearDown() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        GlassCalcPreferences(app).resetAll()
        AppIconManager.applyAppIcon(app, AppIconOption.CLASSIC)
    }

    private fun waitUntil(timeoutMs: Long = 5000L, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            Thread.sleep(15)
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue("Condition was not satisfied within ${timeoutMs}ms", condition())
    }

    @Test
    fun testUnicodeMinusLivePreviewAndChainingAfterEquals() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalculatorViewModel(app)
        vm.setAutoResultEnabled(true)
        waitUntil { vm.isAutoResultEnabled.value }

        // Live preview with Unicode minus '−'
        vm.onCharacterInput("5")
        vm.onCharacterInput("−")
        vm.onCharacterInput("2")
        waitUntil { vm.result.value == "3" }
        assertEquals("3", vm.result.value)

        // Evaluate '=' then chain with Unicode '−'
        vm.onEquals()
        waitUntil { vm.history.value.isNotEmpty() && vm.result.value == "3" }

        vm.onCharacterInput("−")
        vm.onCharacterInput("1")
        assertEquals("3−1", vm.expression.value)
        waitUntil { vm.result.value == "2" }
        assertEquals("2", vm.result.value)
    }

    @Test
    fun testToggleAngleModeUsesFreshModeAndResetClearsGraphLock() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalculatorViewModel(app)
        vm.setAutoResultEnabled(true)
        waitUntil { vm.isAutoResultEnabled.value }

        // Default is DEG: sin(90) == 1
        assertEquals(AngleMode.DEG, vm.angleMode.value)
        vm.onDirectExpressionChange("sin(90)", 7)
        waitUntil { vm.result.value == "1" }
        assertEquals("1", vm.result.value)

        // Toggle to RAD: sin(90) != 1 (~0.89399666)
        vm.toggleAngleMode()
        waitUntil { vm.angleMode.value == AngleMode.RAD && vm.result.value != "1" }
        assertEquals(AngleMode.RAD, vm.angleMode.value)
        assertTrue(vm.result.value != "1")

        // Graph lock resets on resetApp()
        vm.setGraphLocked(true)
        assertTrue(vm.isGraphLocked.value)
        vm.resetApp()
        waitUntil {
            !vm.isGraphLocked.value &&
                vm.expression.value.isEmpty() &&
                vm.result.value == "0"
        }
        assertFalse(vm.isGraphLocked.value)
        assertEquals("", vm.expression.value)
        assertEquals("0", vm.result.value)
    }

    @Test
    fun testSetAppIconSwitchesLauncherAliasInBackgroundAndPersistsUntilReset() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val pm = app.packageManager
        val vm = CalculatorViewModel(app)

        assertEquals(AppIconOption.CLASSIC, vm.currentAppIcon.value)

        // Foreground selection updates UI state immediately without killing/restarting foreground alias
        vm.setAppIcon(AppIconOption.MATERIAL_YOU)
        waitUntil { vm.currentAppIcon.value == AppIconOption.MATERIAL_YOU }
        val classicBeforeBackground = pm.getComponentEnabledSetting(
            ComponentName(app, AppIconOption.CLASSIC.aliasClassName)
        )
        assertTrue(
            classicBeforeBackground == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                classicBeforeBackground == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        )
        assertTrue(
            pm.getComponentEnabledSetting(
                ComponentName(app, AppIconOption.MATERIAL_YOU.aliasClassName)
            ) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        )

        // Moving to background (onAppStop) applies the PackageManager alias change asynchronously
        vm.onAppStop()
        waitUntil {
            vm.currentAppIcon.value == AppIconOption.MATERIAL_YOU &&
                pm.getComponentEnabledSetting(
                    ComponentName(app, AppIconOption.MATERIAL_YOU.aliasClassName)
                ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        AppIconOption.entries.forEach { option ->
            val state = pm.getComponentEnabledSetting(ComponentName(app, option.aliasClassName))
            if (option == AppIconOption.MATERIAL_YOU) {
                assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, state)
            } else {
                assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DISABLED, state)
            }
        }

        vm.setAppIcon(AppIconOption.BLUE)
        waitUntil { vm.currentAppIcon.value == AppIconOption.BLUE }
        vm.onAppStop()
        waitUntil {
            vm.currentAppIcon.value == AppIconOption.BLUE &&
                pm.getComponentEnabledSetting(
                    ComponentName(app, AppIconOption.BLUE.aliasClassName)
                ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            pm.getComponentEnabledSetting(
                ComponentName(app, AppIconOption.MATERIAL_YOU.aliasClassName)
            )
        )

        vm.resetApp()
        waitUntil { vm.currentAppIcon.value == AppIconOption.CLASSIC }
        vm.onAppStop()
        waitUntil {
            vm.currentAppIcon.value == AppIconOption.CLASSIC &&
                pm.getComponentEnabledSetting(
                    ComponentName(app, AppIconOption.CLASSIC.aliasClassName)
                ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        AppIconOption.entries.filter { it != AppIconOption.CLASSIC }.forEach { option ->
            assertEquals(
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                pm.getComponentEnabledSetting(ComponentName(app, option.aliasClassName))
            )
        }
    }

    @Test
    fun testMaterialYouAppIconFallsBackToClassicBelowApi33() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val pm = app.packageManager
        val vm = CalculatorViewModel(app)

        vm.setAppIcon(AppIconOption.MATERIAL_YOU)
        vm.onAppStop()
        waitUntil {
            vm.currentAppIcon.value == AppIconOption.MATERIAL_YOU &&
                pm.getComponentEnabledSetting(
                    ComponentName(app, AppIconOption.MATERIAL_YOU.aliasClassName)
                ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        // Drop simulated API below 33 -> UI falls back immediately, launcher alias updates on onAppStop()
        vm.setSimulatedApi(enabled = true, level = 32)
        waitUntil { vm.currentAppIcon.value == AppIconOption.CLASSIC }
        vm.onAppStop()
        waitUntil {
            pm.getComponentEnabledSetting(
                ComponentName(app, AppIconOption.CLASSIC.aliasClassName)
            ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED &&
                pm.getComponentEnabledSetting(
                    ComponentName(app, AppIconOption.MATERIAL_YOU.aliasClassName)
                ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        assertEquals(AppIconOption.CLASSIC, vm.currentAppIcon.value)

        // Calling setAppIcon(MATERIAL_YOU) while simulated API is 32 falls back to CLASSIC
        vm.setAppIcon(AppIconOption.MATERIAL_YOU)
        waitUntil { vm.currentAppIcon.value == AppIconOption.CLASSIC }

        // Restore simulated API >= 33 -> Material You can be selected again
        vm.setSimulatedApi(enabled = true, level = 33)
        waitUntil { vm.simulatedApiLevel.value == 33 }
        vm.setAppIcon(AppIconOption.MATERIAL_YOU)
        vm.onAppStop()
        waitUntil {
            vm.currentAppIcon.value == AppIconOption.MATERIAL_YOU &&
                pm.getComponentEnabledSetting(
                    ComponentName(app, AppIconOption.MATERIAL_YOU.aliasClassName)
                ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
    }

    @Test
    @Config(sdk = [32])
    fun testMaterialYouAppIconRejectedOnRealDeviceBelowApi33() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val pm = app.packageManager
        val vm = CalculatorViewModel(app)

        vm.setAppIcon(AppIconOption.MATERIAL_YOU)
        assertEquals(AppIconOption.CLASSIC, vm.currentAppIcon.value)
        vm.onAppStop()
        waitUntil {
            val classicState = pm.getComponentEnabledSetting(
                ComponentName(app, AppIconOption.CLASSIC.aliasClassName)
            )
            vm.currentAppIcon.value == AppIconOption.CLASSIC &&
                (classicState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                    classicState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) &&
                pm.getComponentEnabledSetting(
                    ComponentName(app, AppIconOption.MATERIAL_YOU.aliasClassName)
                ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        assertEquals(AppIconOption.CLASSIC, vm.currentAppIcon.value)
    }

    @Test
    fun testRapidSequentialCharacterInputDoesNotDropKeysAndEvaluatesAsynchronously() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalculatorViewModel(app)
        vm.setAutoResultEnabled(true)
        waitUntil { vm.isAutoResultEnabled.value }

        // Rapid "17" input must commit immediately and synchronously to state
        vm.onCharacterInput("1")
        vm.onCharacterInput("7")
        assertEquals("17", vm.expression.value)
        assertEquals("17", vm.displayState.value.expression)
        assertEquals(2, vm.displayState.value.cursorPosition)

        // Rapid "486+4586" input must keep all characters without blocking or dropping
        vm.onClearAll()
        assertEquals("", vm.expression.value)
        listOf("4", "8", "6", "+", "4", "5", "8", "6").forEach { key ->
            vm.onCharacterInput(key)
        }
        assertEquals("486+4586", vm.expression.value)
        assertEquals("486+4586", vm.displayState.value.expression)
        waitUntil { vm.result.value == "5072" }
        assertEquals("5072", vm.result.value)

        // Rapid "17" immediately after '=' must cleanly replace previous expression with both digits
        vm.onEquals()
        waitUntil { vm.history.value.isNotEmpty() }
        vm.onCharacterInput("1")
        vm.onCharacterInput("7")
        assertEquals("17", vm.expression.value)
    }
}
