package com.limonatalp4.glasscalcproject

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.limonatalp4.glasscalcproject.data.model.AccentColorOption
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppIconOption
import com.limonatalp4.glasscalcproject.data.preferences.GlassCalcPreferences
import com.limonatalp4.glasscalcproject.data.remote.FrankfurterApiService
import com.limonatalp4.glasscalcproject.data.remote.FrankfurterRatesResponse
import com.limonatalp4.glasscalcproject.data.repository.CurrencyRepository
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.ui.util.AppIconManager
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    @Test
    fun testCustomAccentColorPersistsInDataStoreAndResets() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = GlassCalcPreferences(app)
        val vm = CalculatorViewModel(app)

        assertNull(vm.customAccentColor.value)
        assertNull(prefs.customAccentColorFlow.first())

        val teal = AccentColorOption.TEAL.colorArgb!!
        vm.setCustomAccentColor(teal)
        assertEquals(teal, vm.customAccentColor.value)
        waitUntil { runBlocking { prefs.customAccentColorFlow.first() == teal } }

        // Fresh ViewModel loads persisted custom accent color
        val restoredVm = CalculatorViewModel(app)
        waitUntil { restoredVm.customAccentColor.value == teal }
        assertEquals(teal, restoredVm.customAccentColor.value)

        // Resetting to null removes custom accent override
        restoredVm.setCustomAccentColor(null)
        assertNull(restoredVm.customAccentColor.value)
        waitUntil { runBlocking { prefs.customAccentColorFlow.first() == null } }

        // Selecting Coral then calling resetApp() clears it cleanly
        val coral = AccentColorOption.CORAL.colorArgb!!
        restoredVm.setCustomAccentColor(coral)
        assertEquals(coral, restoredVm.customAccentColor.value)
        waitUntil { runBlocking { prefs.customAccentColorFlow.first() == coral } }

        restoredVm.resetApp()
        assertNull(restoredVm.customAccentColor.value)
        assertEquals("", restoredVm.accentHexInput.value)
        assertFalse(restoredVm.isAccentHexInvalid.value)
        waitUntil { runBlocking { prefs.customAccentColorFlow.first() == null } }
    }

    @Test
    fun testCustomAccentHexInputSanitizationValidationAndPersistence() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = GlassCalcPreferences(app)
        val vm = CalculatorViewModel(app)

        // Filters non-hex characters, uppercases hex, and clamps to 6 hex digits
        assertEquals("", CalculatorViewModel.sanitizeAccentHexInput("xyz"))
        assertEquals("#", CalculatorViewModel.sanitizeAccentHexInput("#"))
        assertEquals("#FF5722", CalculatorViewModel.sanitizeAccentHexInput("ff57z22g99"))

        // Selecting a preset circle updates hex input cleanly
        val purple = AccentColorOption.PURPLE.colorArgb!!
        vm.setCustomAccentColor(purple)
        assertEquals("#9333EA", vm.accentHexInput.value)
        assertFalse(vm.isAccentHexInvalid.value)

        // Partial/incomplete hex triggers invalid flag without overwriting last valid persisted color
        vm.onAccentHexInput("#FF5")
        assertEquals("#FF5", vm.accentHexInput.value)
        assertTrue(vm.isAccentHexInvalid.value)
        assertEquals(purple, vm.customAccentColor.value)

        // Valid 6-digit hex immediately clears error, updates color, and persists via DataStore
        vm.onAccentHexInput("ff5722")
        assertEquals("#FF5722", vm.accentHexInput.value)
        assertFalse(vm.isAccentHexInvalid.value)
        val orangeArgb = 0xFFFF5722.toInt()
        assertEquals(orangeArgb, vm.customAccentColor.value)
        assertNull(AccentColorOption.findPresetOrNull(orangeArgb))
        waitUntil { runBlocking { prefs.customAccentColorFlow.first() == orangeArgb } }

        // Fresh ViewModel restores custom hex color and fills accentHexInput
        val restoredVm = CalculatorViewModel(app)
        waitUntil { restoredVm.customAccentColor.value == orangeArgb }
        assertEquals("#FF5722", restoredVm.accentHexInput.value)
        assertFalse(restoredVm.isAccentHexInvalid.value)

        // Typing preset hex automatically matches preset enum
        restoredVm.onAccentHexInput("#9333EA")
        assertEquals(AccentColorOption.PURPLE, AccentColorOption.findPresetOrNull(restoredVm.customAccentColor.value))
    }

    @Test
    fun testRecentAccentColorsTrackCirclesAndHexCappedAtFiveAndReset() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = GlassCalcPreferences(app)
        val vm = CalculatorViewModel(app)

        assertTrue(vm.recentAccentColors.value.isEmpty())

        val teal = AccentColorOption.TEAL.colorArgb!!
        val coral = AccentColorOption.CORAL.colorArgb!!
        val purple = AccentColorOption.PURPLE.colorArgb!!
        val green = AccentColorOption.GREEN.colorArgb!!
        val amber = AccentColorOption.AMBER.colorArgb!!
        val customHex = 0xFFFF5722.toInt()

        // Selecting Standard/Default (null) does not record null into recent colors
        vm.setCustomAccentColor(null)
        assertTrue(vm.recentAccentColors.value.isEmpty())

        // Pick 5 preset colors + 1 valid hex -> MRU order, capped at 5
        vm.setCustomAccentColor(teal)
        vm.setCustomAccentColor(coral)
        vm.setCustomAccentColor(purple)
        vm.setCustomAccentColor(green)
        vm.setCustomAccentColor(amber)
        // Partial hex must not be added
        vm.onAccentHexInput("#FF5")
        assertEquals(listOf(amber, green, purple, coral, teal), vm.recentAccentColors.value)

        // Complete valid hex becomes most recent, evicting oldest (teal)
        vm.onAccentHexInput("#FF5722")
        assertEquals(listOf(customHex, amber, green, purple, coral), vm.recentAccentColors.value)

        // Selecting an existing recent color moves it to index 0 without duplication
        vm.setCustomAccentColor(purple)
        assertEquals(listOf(purple, customHex, amber, green, coral), vm.recentAccentColors.value)

        waitUntil {
            runBlocking {
                prefs.recentAccentColorsFlow.first() == listOf(purple, customHex, amber, green, coral)
            }
        }

        // Fresh ViewModel restores persisted recent colors
        val restoredVm = CalculatorViewModel(app)
        waitUntil {
            restoredVm.recentAccentColors.value == listOf(purple, customHex, amber, green, coral)
        }

        // resetApp clears recent accent colors
        restoredVm.resetApp()
        assertTrue(restoredVm.recentAccentColors.value.isEmpty())
        waitUntil { runBlocking { prefs.recentAccentColorsFlow.first().isEmpty() } }
    }

    @Test
    fun testCurrencyRepositoryCachesRatesAndFallsBackOffline() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = GlassCalcPreferences(app)

        val onlineApi = object : FrankfurterApiService {
            var callCount = 0
            override suspend fun getLatestRates(base: String, symbols: String?): FrankfurterRatesResponse {
                callCount++
                return FrankfurterRatesResponse(
                    amount = 1.0,
                    base = "EUR",
                    date = "2026-11-26",
                    rates = mapOf("USD" to 1.10, "GBP" to 0.85, "JPY" to 165.0)
                )
            }
        }

        val repo = CurrencyRepository(app, prefs, apiServiceOverride = onlineApi)
        repo.loadRatesIfNeeded(forceRefresh = false)

        val state = repo.ratesState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasError)
        assertFalse(state.isOfflineFallback)
        assertEquals("2026-11-26", state.date)
        assertEquals(1.0, state.rates["EUR"] ?: 0.0, 1e-6)
        assertEquals(1.10, state.rates["USD"] ?: 0.0, 1e-6)

        // Calling again within TTL uses cached state without hitting network
        repo.loadRatesIfNeeded(forceRefresh = false)
        assertEquals(1, onlineApi.callCount)

        // Bidirectional currency conversion
        val eurUsdPair = UnitConverter.CONVERSIONS.first { it.unitFrom == "EUR" && it.unitTo == "USD" }
        assertEquals("110,00", UnitConverter.convertCurrency(100.0, eurUsdPair, forward = true, rates = state.rates))
        assertEquals("100,00", UnitConverter.convertCurrency(110.0, eurUsdPair, forward = false, rates = state.rates))

        // Cross-currency conversion (USD -> JPY)
        val usdJpyPair = UnitConverter.CONVERSIONS.first { it.unitFrom == "USD" && it.unitTo == "JPY" }
        assertEquals("1500,00", UnitConverter.convertCurrency(10.0, usdJpyPair, forward = true, rates = state.rates))

        // Offline force-refresh falls back cleanly to persisted DataStore cache
        val failingApi = object : FrankfurterApiService {
            override suspend fun getLatestRates(base: String, symbols: String?): FrankfurterRatesResponse {
                throw IOException("Network unavailable")
            }
        }
        val offlineWithCacheRepo = CurrencyRepository(app, prefs, apiServiceOverride = failingApi)
        offlineWithCacheRepo.loadRatesIfNeeded(forceRefresh = true)
        val fallbackState = offlineWithCacheRepo.ratesState.value
        assertTrue(fallbackState.hasRates)
        assertTrue(fallbackState.isOfflineFallback)
        assertFalse(fallbackState.hasError)
        assertEquals("2026-11-26", fallbackState.date)

        // Offline cold start without any cache reports error cleanly
        prefs.resetAll()
        val offlineNoCacheRepo = CurrencyRepository(app, prefs, apiServiceOverride = failingApi)
        offlineNoCacheRepo.loadRatesIfNeeded(forceRefresh = true)
        assertFalse(offlineNoCacheRepo.ratesState.value.hasRates)
        assertTrue(offlineNoCacheRepo.ratesState.value.hasError)
    }
}
