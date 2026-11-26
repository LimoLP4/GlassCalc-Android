package com.limonatalp4.glasscalcproject.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.limonatalp4.glasscalcproject.MainActivity
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.domain.evaluator.EvaluationResult
import com.limonatalp4.glasscalcproject.domain.evaluator.ExpressionEvaluator

class GlassCalcWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                GlassCalcQuickWidgetContent(context)
            }
        }
    }

    companion object {
        val KEY_EXPRESSION = stringPreferencesKey("widget_expression")
        val KEY_RESULT = stringPreferencesKey("widget_result")
        val KEY_JUST_CALCULATED = booleanPreferencesKey("widget_just_calculated")
        val PARAM_KEY = ActionParameters.Key<String>("key_value")
    }
}

private enum class WidgetButtonKind {
    NUMBER,
    OPERATOR,
    ACTION,
    EQUALS
}

@Composable
private fun GlassCalcQuickWidgetContent(context: Context) {
    val prefs = currentState<Preferences>()
    val expression = prefs[GlassCalcWidget.KEY_EXPRESSION] ?: ""
    val result = prefs[GlassCalcWidget.KEY_RESULT] ?: "0"

    val colors = GlanceTheme.colors

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_glass_bg))
            .padding(8.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact Liquid-Glass Header (Tap opens full GlassCalc app)
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(12.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_qs_tile_gc),
                    contentDescription = context.getString(R.string.widget_open_app_desc),
                    colorFilter = ColorFilter.tint(colors.primary),
                    modifier = GlanceModifier.size(16.dp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = context.getString(R.string.app_name),
                    style = TextStyle(
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Liquid-Glass Display Area
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(R.drawable.widget_display_bg))
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    style = TextStyle(
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End
                    ),
                    maxLines = 1
                )
                Text(
                    text = result,
                    style = TextStyle(
                        color = colors.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // 4x5 Basic Arithmetic Keypad
            val rows = listOf(
                listOf("C" to WidgetButtonKind.ACTION, "⌫" to WidgetButtonKind.ACTION, "%" to WidgetButtonKind.OPERATOR, "÷" to WidgetButtonKind.OPERATOR),
                listOf("7" to WidgetButtonKind.NUMBER, "8" to WidgetButtonKind.NUMBER, "9" to WidgetButtonKind.NUMBER, "×" to WidgetButtonKind.OPERATOR),
                listOf("4" to WidgetButtonKind.NUMBER, "5" to WidgetButtonKind.NUMBER, "6" to WidgetButtonKind.NUMBER, "−" to WidgetButtonKind.OPERATOR),
                listOf("1" to WidgetButtonKind.NUMBER, "2" to WidgetButtonKind.NUMBER, "3" to WidgetButtonKind.NUMBER, "+" to WidgetButtonKind.OPERATOR),
                listOf("±" to WidgetButtonKind.ACTION, "0" to WidgetButtonKind.NUMBER, "," to WidgetButtonKind.NUMBER, "=" to WidgetButtonKind.EQUALS)
            )

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { (label, kind) ->
                            WidgetCalcKey(
                                label = label,
                                kind = kind,
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .fillMaxHeight()
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetCalcKey(
    label: String,
    kind: WidgetButtonKind,
    modifier: GlanceModifier = GlanceModifier
) {
    val colors = GlanceTheme.colors
    val bgColor = when (kind) {
        WidgetButtonKind.NUMBER -> colors.surfaceVariant
        WidgetButtonKind.OPERATOR -> colors.secondaryContainer
        WidgetButtonKind.ACTION -> colors.tertiaryContainer
        WidgetButtonKind.EQUALS -> colors.primary
    }
    val textColor = when (kind) {
        WidgetButtonKind.NUMBER -> colors.primary
        WidgetButtonKind.OPERATOR -> colors.onSecondaryContainer
        WidgetButtonKind.ACTION -> colors.onTertiaryContainer
        WidgetButtonKind.EQUALS -> colors.onPrimary
    }

    val action = when (label) {
        "C" -> actionRunCallback<WidgetClearAction>()
        "⌫" -> actionRunCallback<WidgetBackspaceAction>()
        "=" -> actionRunCallback<WidgetEqualsAction>()
        "±" -> actionRunCallback<WidgetToggleSignAction>()
        else -> actionRunCallback<WidgetAppendKeyAction>(
            actionParametersOf(GlassCalcWidget.PARAM_KEY to label)
        )
    }

    Box(
        modifier = modifier
            .cornerRadius(14.dp)
            .background(bgColor)
            .clickable(action),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            maxLines = 1
        )
    }
}

private object WidgetExpressionHelper {
    private val evaluator = ExpressionEvaluator()
    private val operators = setOf("+", "−", "-", "×", "*", "÷", "/", "%")

    fun isOperator(token: String): Boolean = token in operators

    fun evaluatePreviewOrNull(expr: String): String? {
        if (expr.isBlank()) return "0"
        return when (val res = evaluator.evaluate(expr, AngleMode.DEG)) {
            is EvaluationResult.Success -> res.formatted
            is EvaluationResult.Error -> null
        }
    }

    fun evaluateFinal(context: Context, expr: String): Pair<String, Boolean> {
        if (expr.isBlank()) return "0" to false
        return when (val res = evaluator.evaluate(expr, AngleMode.DEG)) {
            is EvaluationResult.Success -> res.formatted to true
            is EvaluationResult.Error -> context.getString(res.messageResId) to false
        }
    }
}

class WidgetAppendKeyAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val key = parameters[GlassCalcWidget.PARAM_KEY] ?: return
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val currentExpr = prefs[GlassCalcWidget.KEY_EXPRESSION] ?: ""
            val currentResult = prefs[GlassCalcWidget.KEY_RESULT] ?: "0"
            val justCalculated = prefs[GlassCalcWidget.KEY_JUST_CALCULATED] ?: false

            val baseExpr = if (justCalculated) {
                if (WidgetExpressionHelper.isOperator(key) &&
                    currentResult.isNotEmpty() &&
                    currentResult != "Error"
                ) {
                    currentResult
                } else {
                    ""
                }
            } else {
                currentExpr
            }

            val nextExpr = if (baseExpr.isEmpty() && key == ",") {
                "0,"
            } else {
                baseExpr + key
            }

            val preview = WidgetExpressionHelper.evaluatePreviewOrNull(nextExpr)
            prefs.toMutablePreferences().apply {
                this[GlassCalcWidget.KEY_EXPRESSION] = nextExpr
                if (preview != null) {
                    this[GlassCalcWidget.KEY_RESULT] = preview
                }
                this[GlassCalcWidget.KEY_JUST_CALCULATED] = false
            }
        }
        GlassCalcWidget().update(context, glanceId)
    }
}

class WidgetBackspaceAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val currentExpr = prefs[GlassCalcWidget.KEY_EXPRESSION] ?: ""
            val nextExpr = if (currentExpr.isNotEmpty()) currentExpr.dropLast(1) else ""
            val preview = WidgetExpressionHelper.evaluatePreviewOrNull(nextExpr)
            prefs.toMutablePreferences().apply {
                this[GlassCalcWidget.KEY_EXPRESSION] = nextExpr
                if (nextExpr.isEmpty()) {
                    this[GlassCalcWidget.KEY_RESULT] = "0"
                } else if (preview != null) {
                    this[GlassCalcWidget.KEY_RESULT] = preview
                }
                this[GlassCalcWidget.KEY_JUST_CALCULATED] = false
            }
        }
        GlassCalcWidget().update(context, glanceId)
    }
}

class WidgetClearAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[GlassCalcWidget.KEY_EXPRESSION] = ""
                this[GlassCalcWidget.KEY_RESULT] = "0"
                this[GlassCalcWidget.KEY_JUST_CALCULATED] = false
            }
        }
        GlassCalcWidget().update(context, glanceId)
    }
}

class WidgetToggleSignAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val currentExpr = prefs[GlassCalcWidget.KEY_EXPRESSION] ?: ""
            val nextExpr = when {
                currentExpr.isEmpty() -> "−"
                currentExpr.startsWith("−") || currentExpr.startsWith("-") -> currentExpr.drop(1)
                else -> "−$currentExpr"
            }
            val preview = WidgetExpressionHelper.evaluatePreviewOrNull(nextExpr)
            prefs.toMutablePreferences().apply {
                this[GlassCalcWidget.KEY_EXPRESSION] = nextExpr
                if (preview != null) {
                    this[GlassCalcWidget.KEY_RESULT] = preview
                }
                this[GlassCalcWidget.KEY_JUST_CALCULATED] = false
            }
        }
        GlassCalcWidget().update(context, glanceId)
    }
}

class WidgetEqualsAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val currentExpr = prefs[GlassCalcWidget.KEY_EXPRESSION] ?: ""
            val (evaluated, success) = WidgetExpressionHelper.evaluateFinal(context, currentExpr)
            prefs.toMutablePreferences().apply {
                this[GlassCalcWidget.KEY_RESULT] = evaluated
                this[GlassCalcWidget.KEY_JUST_CALCULATED] = success
            }
        }
        GlassCalcWidget().update(context, glanceId)
    }
}
