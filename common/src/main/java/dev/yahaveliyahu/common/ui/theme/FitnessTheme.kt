package dev.yahaveliyahu.common.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.yahaveliyahu.common.engine.AppConfig

@Composable
fun FitnessTheme(config: AppConfig, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = config.accentColor,
            background = config.screenBackground,
            surface = config.cardBackground,
            onPrimary = config.buttonTextColor,
            onBackground = config.textPrimary,
            onSurface = config.textPrimary
        ),
        content = content
    )
}
