package com.limonatalp4.glasscalcproject.data.model

enum class AppTheme(val label: String, val requiresMinApi: Int = 0) {
    MATERIAL_YOU("Material You (Dynamische Farben)", requiresMinApi = 31),
    AUTOMATIC("Automatisch (System)", requiresMinApi = 0),
    WHITE_MODE("White Mode (Hell)", requiresMinApi = 0),
    DARK_MODE("Dark Mode (Tiefschwarz)", requiresMinApi = 0),
    AMOLED_DARK("AMOLED Dark (Reines Schwarz)", requiresMinApi = 0)
}

enum class AngleMode {
    DEG, RAD
}

data class CalculationHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
