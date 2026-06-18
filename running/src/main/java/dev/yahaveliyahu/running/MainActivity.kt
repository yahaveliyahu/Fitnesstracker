package dev.yahaveliyahu.running

import androidx.compose.ui.graphics.Color
import dev.yahaveliyahu.common.BaseActivity
import dev.yahaveliyahu.common.data.SportType
import dev.yahaveliyahu.common.engine.AppConfig

class MainActivity : BaseActivity() {

    override fun buildConfig() = AppConfig(
        sportType            = SportType.RUNNING,
        appName              = "RunTracker",
        primaryMetricLabel   = "km",
        speedLabel           = "km/h",
        paceLabel            = "min/km",
        metValue             = 9.8,
        accentColor          = Color(0xFF5BBCE4),
        accentDimColor       = Color(0x1F5BBCE4),
        screenBackground     = Color(0xFF101820),
        cardBackground       = Color(0xFF162435),
        textPrimary          = Color(0xFFDFF0F8),
        textSecondary        = Color(0xFF6A9AB0),
        buttonTextColor      = Color(0xFF101820),
        coachingMilestoneMeters = 1000.0
    )
}
