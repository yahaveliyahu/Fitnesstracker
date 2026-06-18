package dev.yahaveliyahu.common.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.yahaveliyahu.common.engine.FitnessViewModel
import dev.yahaveliyahu.common.engine.Screen

@Composable
fun MainScaffold(vm: FitnessViewModel) {
    val currentScreen by vm.currentScreen.collectAsStateWithLifecycle()
    val config = vm.config
    val showNav = currentScreen != Screen.ACTIVE_WORKOUT

    Scaffold(
        containerColor = config.screenBackground,
        bottomBar = {
            if (showNav) {
                BottomNav(
                    current = currentScreen,
                    onNavigate = { vm.navigate(it) },
                    accent = config.accentColor,
                    bg = config.cardBackground,
                    inactive = config.textSecondary
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(config.screenBackground)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(vm)
                Screen.ACTIVE_WORKOUT -> ActiveWorkoutScreen(vm)
                Screen.HISTORY -> HistoryScreen(vm)
                Screen.GOALS -> GoalsScreen(vm)
            }
        }
    }
}

@Composable
fun BottomNav(
    current: Screen,
    onNavigate: (Screen) -> Unit,
    accent: Color,
    bg: Color,
    inactive: Color
) {
    NavigationBar(
        containerColor = bg,
        contentColor = accent
    ) {
        NavigationBarItem(
            selected = current == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            icon = { Icon(Icons.Outlined.Home, "Home") },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accent,
                selectedTextColor = accent,
                unselectedIconColor = inactive,
                unselectedTextColor = inactive,
                indicatorColor = accent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = current == Screen.HISTORY,
            onClick = { onNavigate(Screen.HISTORY) },
            icon = { Icon(Icons.Outlined.History, "History") },
            label = { Text("History", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accent,
                selectedTextColor = accent,
                unselectedIconColor = inactive,
                unselectedTextColor = inactive,
                indicatorColor = accent.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = current == Screen.GOALS,
            onClick = { onNavigate(Screen.GOALS) },
            icon = { Icon(Icons.Outlined.TrackChanges, "Goals") },
            label = { Text("Goals", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = accent,
                selectedTextColor = accent,
                unselectedIconColor = inactive,
                unselectedTextColor = inactive,
                indicatorColor = accent.copy(alpha = 0.15f)
            )
        )
    }
}
