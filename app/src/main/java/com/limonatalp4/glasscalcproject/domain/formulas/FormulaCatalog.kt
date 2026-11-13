package com.limonatalp4.glasscalcproject.domain.formulas

import androidx.compose.runtime.Immutable
import com.limonatalp4.glasscalcproject.R

@Immutable
data class MathFormula(
    val id: String,
    val formula: String,
    val insertExpression: String,
    val nameResId: Int,
    val keywordsResIds: List<Int>
)

object FormulaCatalog {
    val FORMULAS: List<MathFormula> = listOf(
        MathFormula(
            id = "circle_area",
            formula = "A = π × r²",
            insertExpression = "π×5^2",
            nameResId = R.string.formula_circle_area,
            keywordsResIds = listOf(R.string.formula_circle_area_keywords)
        ),
        MathFormula(
            id = "circle_circumference",
            formula = "U = 2 × π × r",
            insertExpression = "2×π×5",
            nameResId = R.string.formula_circle_circumference,
            keywordsResIds = listOf(R.string.formula_circle_circumference_keywords)
        ),
        MathFormula(
            id = "pythagoras",
            formula = "c = √(a² + b²)",
            insertExpression = "√(3^2+4^2)",
            nameResId = R.string.formula_pythagoras,
            keywordsResIds = listOf(R.string.formula_pythagoras_keywords)
        ),
        MathFormula(
            id = "compound_interest",
            formula = "Kₙ = K₀ × (1 + p/100)ⁿ",
            insertExpression = "1000×(1+5/100)^10",
            nameResId = R.string.formula_compound_interest,
            keywordsResIds = listOf(R.string.formula_compound_interest_keywords)
        ),
        MathFormula(
            id = "sphere_volume",
            formula = "V = (4/3) × π × r³",
            insertExpression = "(4/3)×π×3^3",
            nameResId = R.string.formula_sphere_volume,
            keywordsResIds = listOf(R.string.formula_sphere_volume_keywords)
        ),
        MathFormula(
            id = "sphere_surface",
            formula = "O = 4 × π × r²",
            insertExpression = "4×π×3^2",
            nameResId = R.string.formula_sphere_surface,
            keywordsResIds = listOf(R.string.formula_sphere_surface_keywords)
        ),
        MathFormula(
            id = "cylinder_volume",
            formula = "V = π × r² × h",
            insertExpression = "π×3^2×10",
            nameResId = R.string.formula_cylinder_volume,
            keywordsResIds = listOf(R.string.formula_cylinder_volume_keywords)
        ),
        MathFormula(
            id = "triangle_area",
            formula = "A = 0,5 × g × h",
            insertExpression = "0,5×6×4",
            nameResId = R.string.formula_triangle_area,
            keywordsResIds = listOf(R.string.formula_triangle_area_keywords)
        ),
        MathFormula(
            id = "quadratic_formula",
            formula = "x = (−b + √(b² − 4ac)) / (2a)",
            insertExpression = "(−(−5)+√((−5)^2−4×1×6))÷(2×1)",
            nameResId = R.string.formula_quadratic,
            keywordsResIds = listOf(R.string.formula_quadratic_keywords)
        ),
        MathFormula(
            id = "percentage",
            formula = "W = G × p%",
            insertExpression = "250×19%",
            nameResId = R.string.formula_percentage,
            keywordsResIds = listOf(R.string.formula_percentage_keywords)
        ),
        MathFormula(
            id = "definite_integral",
            formula = "∫(x²; 0; 3)",
            insertExpression = "∫(x^2; 0; 3)",
            nameResId = R.string.formula_integral_example,
            keywordsResIds = listOf(R.string.formula_integral_example_keywords)
        ),
        MathFormula(
            id = "derivative_point",
            formula = "d/dx(x³; 2)",
            insertExpression = "d/dx(x^3; 2)",
            nameResId = R.string.formula_derivative_example,
            keywordsResIds = listOf(R.string.formula_derivative_example_keywords)
        )
    )
}
