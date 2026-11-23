package com.limonatalp4.glasscalcproject

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
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
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("display_result_text")
                .fetchSemanticsNodes()
                .firstOrNull()
                ?.config
                ?.getOrNull(SemanticsProperties.Text)
                ?.joinToString("") == "10"
        }
        composeTestRule.onNodeWithTag("display_result_text").assertTextEquals("10")

        // Toggle keyboard via pill button (open -> close -> open), type with blocked chars, evaluate
        composeTestRule.onNodeWithTag("btn_open_keyboard").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_open_keyboard").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_open_keyboard").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_clear").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("display_expression_text").performTextInput("2+3@äф")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_equals").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("display_result_text")
                .fetchSemanticsNodes()
                .firstOrNull()
                ?.config
                ?.getOrNull(SemanticsProperties.Text)
                ?.joinToString("") == "5"
        }
        composeTestRule.onNodeWithTag("display_result_text").assertTextEquals("5")

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
        // Open and close Supported Conversion Systems info dialog
        composeTestRule.onNodeWithTag("btn_converter_info").assertIsDisplayed().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("dialog_converter_systems_info").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("text_converter_systems_detail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_close_converter_info").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("dialog_converter_systems_info").fetchSemanticsNodes().isEmpty()
        }
        // Pairs accordion initially collapsed, expands on tap, and preserves state on recreate
        composeTestRule.onNodeWithTag("card_converter_pairs_accordion").performScrollTo().assertIsDisplayed()
        assert(composeTestRule.onAllNodesWithTag("pair_m_ft").fetchSemanticsNodes().isEmpty())
        composeTestRule.onNodeWithTag("card_converter_pairs_accordion").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("pair_m_ft").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("pair_m_ft").performScrollTo().assertIsDisplayed()
        composeTestRule.runOnUiThread {
            composeTestRule.activity.recreate()
        }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("pair_m_ft").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("pair_m_ft").performScrollTo().assertIsDisplayed()
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
        // Select sin(x) for clean finite points across the viewport
        composeTestRule.onNodeWithText("sin(x)").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Tap 1 -> sets P1
        composeTestRule.onNodeWithTag("graph_canvas_container").performTouchInput {
            click(Offset(80f, 100f))
        }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("graph_point_1_info").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("graph_point_1_info").performScrollTo().assertIsDisplayed()

        // Tap 2 -> sets P2 and displays slope & distance analysis
        composeTestRule.onNodeWithTag("graph_canvas_container").performTouchInput {
            click(Offset(200f, 100f))
        }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("graph_point_2_info").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("graph_point_2_info").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("graph_points_analysis").performScrollTo().assertIsDisplayed()

        // Tap 3 -> moves P1 while keeping P2
        val p1BeforeText = composeTestRule.onAllNodesWithText("P1:", substring = true)
            .fetchSemanticsNodes()
            .firstOrNull()
            ?.config
            ?.getOrNull(SemanticsProperties.Text)
            ?.joinToString("")
        composeTestRule.onNodeWithTag("graph_canvas_container").performTouchInput {
            click(Offset(40f, 100f))
        }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            val currentText = composeTestRule.onAllNodesWithText("P1:", substring = true)
                .fetchSemanticsNodes()
                .firstOrNull()
                ?.config
                ?.getOrNull(SemanticsProperties.Text)
                ?.joinToString("")
            currentText != null && currentText != p1BeforeText
        }
        composeTestRule.onNodeWithTag("graph_point_1_info").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("graph_point_2_info").performScrollTo().assertIsDisplayed()

        // Clear points and verify lock blocks point placement
        composeTestRule.onNodeWithTag("btn_clear_graph_points").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assert(composeTestRule.onAllNodesWithTag("graph_trace_hud").fetchSemanticsNodes().isEmpty())

        composeTestRule.onNodeWithTag("btn_lock_graph").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("graph_canvas_container").performTouchInput {
            click(Offset(120f, 100f))
        }
        composeTestRule.waitForIdle()
        assert(composeTestRule.onAllNodesWithTag("graph_trace_hud").fetchSemanticsNodes().isEmpty())
        composeTestRule.onNodeWithTag("btn_lock_graph").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        val zoomInDesc = composeTestRule.activity.getString(R.string.graph_zoom_in)
        val panLeftDesc = composeTestRule.activity.getString(R.string.graph_pan_left)
        composeTestRule.onNodeWithContentDescription(zoomInDesc).performScrollTo().performClick()
        composeTestRule.onNodeWithContentDescription(panLeftDesc).performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_close_graph_dialog").performScrollTo().performClick()
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

    @Test
    fun testDesignTabAccordionsExpandAndSurviveRecreate() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("btn_hamburger_menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_settings").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("dialog_settings").fetchSemanticsNodes().isNotEmpty()
        }

        // Initially collapsed: accordion headers visible, inner radio pills hidden
        composeTestRule.onNodeWithTag("card_theme_accordion").assertIsDisplayed()
        composeTestRule.onNodeWithTag("card_app_icon_accordion").assertIsDisplayed()
        assert(composeTestRule.onAllNodesWithTag("radio_theme_DARK_MODE").fetchSemanticsNodes().isEmpty())
        assert(composeTestRule.onAllNodesWithTag("radio_app_icon_BLUE").fetchSemanticsNodes().isEmpty())

        // Expand Theme accordion
        composeTestRule.onNodeWithTag("card_theme_accordion").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("radio_theme_DARK_MODE").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("radio_theme_DARK_MODE").performScrollTo().assertIsDisplayed()

        // Expand App-Icon accordion
        composeTestRule.onNodeWithTag("card_app_icon_accordion").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("radio_app_icon_BLUE").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("radio_app_icon_BLUE").performScrollTo().assertIsDisplayed()

        // Recreate activity (simulates rotation / configuration change) -> both stay expanded
        composeTestRule.runOnUiThread {
            composeTestRule.activity.recreate()
        }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("dialog_settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("radio_theme_DARK_MODE").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_app_icon_BLUE").performScrollTo().assertIsDisplayed()

        // Switch to Units tab: angle mode accordion initially collapsed
        composeTestRule.onNodeWithTag("tab_settings_1").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("card_angle_mode_accordion").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("card_angle_mode_accordion").assertIsDisplayed()
        assert(composeTestRule.onAllNodesWithTag("radio_angle_deg").fetchSemanticsNodes().isEmpty())
        assert(composeTestRule.onAllNodesWithTag("radio_angle_rad").fetchSemanticsNodes().isEmpty())
        val supportedSystemsTitle = composeTestRule.activity.getString(R.string.units_supported_systems)
        assert(composeTestRule.onAllNodesWithText(supportedSystemsTitle).fetchSemanticsNodes().isEmpty())

        // Expand Angle mode accordion & select RAD
        composeTestRule.onNodeWithTag("card_angle_mode_accordion").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("radio_angle_deg").fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithTag("radio_angle_rad").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("radio_angle_deg").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_angle_rad").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_angle_rad").performClick()
        composeTestRule.waitForIdle()

        // Recreate activity -> Units tab and expanded Angle mode accordion stay preserved
        composeTestRule.runOnUiThread {
            composeTestRule.activity.recreate()
        }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("radio_angle_rad").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("radio_angle_deg").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_angle_rad").performScrollTo().assertIsDisplayed()

        // Switch to Language tab: accordion initially collapsed
        composeTestRule.onNodeWithTag("tab_settings_4").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("card_language_accordion").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("card_language_accordion").assertIsDisplayed()
        assert(composeTestRule.onAllNodesWithTag("radio_lang_GERMAN").fetchSemanticsNodes().isEmpty())

        // Expand Language accordion
        composeTestRule.onNodeWithTag("card_language_accordion").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("radio_lang_GERMAN").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("radio_lang_GERMAN").performScrollTo().assertIsDisplayed()

        // Recreate activity -> Language tab and expanded Language accordion stay preserved
        composeTestRule.runOnUiThread {
            composeTestRule.activity.recreate()
        }
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithTag("radio_lang_GERMAN").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("radio_lang_GERMAN").performScrollTo().assertIsDisplayed()
    }
}


