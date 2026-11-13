package com.limonatalp4.glasscalcproject.domain.formulas

import androidx.compose.runtime.Immutable
import com.limonatalp4.glasscalcproject.R

@Immutable
data class MathFormula(
    val id: String,
    val titleResId: Int,
    val previewFormula: String,
    val insertExpression: String,
    val keywords: List<String>
)

object FormulaCatalog {
    val FORMULAS: List<MathFormula> = listOf(
        MathFormula(
            id = "circle_area",
            titleResId = R.string.formula_circle_area,
            previewFormula = "A = π × r²",
            insertExpression = "π×5^2",
            keywords = listOf("kreis", "fläche", "circle", "area", "pi", "radius", "r^2")
        ),
        MathFormula(
            id = "circle_circumference",
            titleResId = R.string.formula_circle_circumference,
            previewFormula = "U = 2 × π × r",
            insertExpression = "2×π×5",
            keywords = listOf("kreis", "umfang", "circle", "circumference", "pi", "radius")
        ),
        MathFormula(
            id = "pythagoras",
            titleResId = R.string.formula_pythagoras,
            previewFormula = "c = √(a² + b²)",
            insertExpression = "√(3^2+4^2)",
            keywords = listOf("pythagoras", "pythagorean", "dreieck", "hypotenuse", "triangle", "a^2", "b^2")
        ),
        MathFormula(
            id = "compound_interest",
            titleResId = R.string.formula_compound_interest,
            previewFormula = "Kₙ = K₀ × (1 + p/100)ⁿ",
            insertExpression = "1000×(1+5/100)^10",
            keywords = listOf("zinseszins", "zins", "kapital", "compound", "interest", "prozent", "percent")
        ),
        MathFormula(
            id = "sphere_volume",
            titleResId = R.string.formula_sphere_volume,
            previewFormula = "V = (4/3) × π × r³",
            insertExpression = "(4/3)×π×3^3",
            keywords = listOf("kugel", "volumen", "sphere", "volume", "pi", "r^3")
        ),
        MathFormula(
            id = "sphere_surface",
            titleResId = R.string.formula_sphere_surface,
            previewFormula = "O = 4 × π × r²",
            insertExpression = "4×π×3^2",
            keywords = listOf("kugel", "oberfläche", "sphere", "surface", "pi")
        ),
        MathFormula(
            id = "cylinder_volume",
            titleResId = R.string.formula_cylinder_volume,
            previewFormula = "V = π × r² × h",
            insertExpression = "π×3^2×10",
            keywords = listOf("zylinder", "volumen", "cylinder", "volume", "höhe", "height")
        ),
        MathFormula(
            id = "triangle_area",
            titleResId = R.string.formula_triangle_area,
            previewFormula = "A = 0,5 × g × h",
            insertExpression = "0,5×6×4",
            keywords = listOf("dreieck", "fläche", "triangle", "area", "grundseite", "höhe")
        ),
        MathFormula(
            id = "quadratic_formula",
            titleResId = R.string.formula_quadratic,
            previewFormula = "x = (−b + √(b² − 4ac)) / (2a)",
            insertExpression = "(−(−5)+√((−5)^2−4×1×6))÷(2×1)",
            keywords = listOf("quadratisch", "mitternacht", "abc", "pq", "gleichung", "quadratic", "equation")
        ),
        MathFormula(
            id = "percentage",
            titleResId = R.string.formula_percentage,
            previewFormula = "W = G × p%",
            insertExpression = "250×19%",
            keywords = listOf("prozent", "rabatt", "mwst", "percentage", "percent", "%")
        ),
        MathFormula(
            id = "definite_integral",
            titleResId = R.string.formula_integral_example,
            previewFormula = "∫(x²; 0; 3)",
            insertExpression = "∫(x^2; 0; 3)",
            keywords = listOf("integral", "fläche", "simpson", "calculus", "analysis")
        ),
        MathFormula(
            id = "derivative_point",
            titleResId = R.string.formula_derivative_example,
            previewFormula = "d/dx(x³; 2)",
            insertExpression = "d/dx(x^3; 2)",
            keywords = listOf("ableitung", "steigung", "derivative", "differential", "d/dx")
        )
    )
}
