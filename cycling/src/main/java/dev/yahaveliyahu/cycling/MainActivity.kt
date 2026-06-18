package dev.yahaveliyahu.cycling

import androidx.compose.ui.graphics.Color
import dev.yahaveliyahu.common.BaseActivity
import dev.yahaveliyahu.common.data.SportType
import dev.yahaveliyahu.common.engine.AppConfig

class MainActivity : BaseActivity() {

    override fun buildConfig() = AppConfig(
        sportType            = SportType.CYCLING,
        appName              = "CycleTracker",
        primaryMetricLabel   = "km",
        speedLabel           = "km/h",
        paceLabel            = "min/km",
        metValue             = 7.5,
        accentColor          = Color(0xFF52C452),
        accentDimColor       = Color(0x1F52C452),
        screenBackground     = Color(0xFF0E1A10),
        cardBackground       = Color(0xFF142014),
        textPrimary          = Color(0xFFD8F0D8),
        textSecondary        = Color(0xFF5A9A5A),
        buttonTextColor      = Color(0xFF0E1A10),
        coachingMilestoneMeters = 1000.0
    )
}
