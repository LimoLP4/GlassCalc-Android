package com.limonatalp4.glasscalcproject

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppCrashRegressionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLaunchCalculatorAndOpenAllDialogsAndSearchWithoutCrash() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calculator_display").assertIsDisplayed()

        // Basic calculation
        composeTestRule.onNodeWithTag("btn_7").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("btn_add").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("btn_3").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("btn_equals").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("display_result_text").assertTextEquals("10")

        // Scientific panel & Constants dialog
        composeTestRule.onNodeWithTag("btn_toggle_scientific").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_constants").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_constants").assertIsDisplayed()
        composeTestRule.onNodeWithTag("constant_π").performClick()
        composeTestRule.waitForIdle()

        // History Sheet
        composeTestRule.onNodeWithTag("btn_top_history").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("history_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_clear_history").performClick()
        composeTestRule.waitForIdle()

        // Dismiss History Sheet via back
        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Open Hamburger Menu & Formula Search
        composeTestRule.onNodeWithTag("btn_hamburger_menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("floating_nav_menu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_formula_search_input").performTextInput("pyth")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("formula_item_pythagoras").performClick()
        composeTestRule.waitForIdle()

        // Open Unit Converter, Search, Select Suggestion & Test Extreme Inputs
        composeTestRule.onNodeWithTag("btn_hamburger_menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_converter").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("dialog_unit_converter").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("converter_search_input").performTextInput("gig")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unit_suggestion_gigabyte").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_converter_value").performTextClearance()
        composeTestRule.onNodeWithTag("input_converter_value").performTextInput("1e500")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_converter_value").performTextClearance()
        composeTestRule.onNodeWithTag("input_converter_value").performTextInput("NaN")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_converter_value").performTextClearance()
        composeTestRule.onNodeWithTag("input_converter_value").performTextInput("2")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_import_converter").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Open Graph Dialog, click presets, zoom & pan
        composeTestRule.onNodeWithTag("btn_hamburger_menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_graph").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("dialog_graph").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("tan(x)").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        val zoomInDesc = composeTestRule.activity.getString(R.string.graph_zoom_in)
        val panLeftDesc = composeTestRule.activity.getString(R.string.graph_pan_left)
        composeTestRule.onNodeWithContentDescription(zoomInDesc).performClick()
        composeTestRule.onNodeWithContentDescription(panLeftDesc).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_close_graph_dialog").performClick()
        composeTestRule.waitForIdle()

        // Open Settings Dialog, Search, Tap Tab directly during search, Open Info -> Click GitHub without browser
        composeTestRule.onNodeWithTag("btn_hamburger_menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_settings").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("dialog_settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("settings_search_input").performTextInput("haptik")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_clear_settings_search").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tab_settings_3").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("LimoLP4/GlassCalc-Android").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tab_settings_4").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_close_settings").performClick()
        composeTestRule.waitForIdle()

        // Activity recreate stability check
        composeTestRule.runOnUiThread {
            composeTestRule.activity.recreate()
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calculator_display").assertIsDisplayed()
    }
}

