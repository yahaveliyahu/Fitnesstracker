package dev.yahaveliyahu.common.engine

import androidx.compose.ui.graphics.Color
import dev.yahaveliyahu.common.data.SportType

data class AppConfig(
    val sportType: SportType,
    val appName: String,
    val primaryMetricLabel: String,
    val speedLabel: String,
    val paceLabel: String,
    val metValue: Double,
    val accentColor: Color,
    val accentDimColor: Color,
    val screenBackground: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonTextColor: Color,
    val coachingMilestoneMeters: Double = 1000.0
)
