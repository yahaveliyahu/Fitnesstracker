package dev.yahaveliyahu.common.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.yahaveliyahu.common.engine.FitnessViewModel
import dev.yahaveliyahu.common.engine.WorkoutState

@Composable
fun ActiveWorkoutScreen(vm: FitnessViewModel) {
    val config = vm.config
    val accent = config.accentColor
    val bg = config.screenBackground
    val card = config.cardBackground
    val textPrimary = config.textPrimary
    val textSecondary = config.textSecondary

    val state by vm.activeState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("End Workout?", color = textPrimary) },
            text = { Text("Your workout will be saved.", color = textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    vm.stopWorkout()
                }) { Text("Save & End", color = accent) }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Keep going", color = textSecondary)
                }
            },
            containerColor = card
        )
    }

    Column(
        Modifier.fillMaxSize().background(bg).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            if (state.isPaused) "PAUSED" else "ACTIVE",
            fontSize = 11.sp, color = if (state.isPaused) Color(0xFFFFAA00) else accent,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(state.formattedTime, fontSize = 64.sp,
            fontWeight = FontWeight.Bold, color = textPrimary)

        Spacer(Modifier.height(4.dp))

        Text("elapsed time", fontSize = 12.sp, color = textSecondary)

        Spacer(Modifier.height(24.dp))

        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(card).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.formattedDistance, fontSize = 72.sp,
                    fontWeight = FontWeight.Bold, color = accent)
                Text("kilometers", fontSize = 14.sp, color = textSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveStatCard("Avg Pace", state.formattedPace, "/km",
                Icons.Outlined.Speed, accent, card, textPrimary, textSecondary, Modifier.weight(1f))
            LiveStatCard("Speed", state.formattedSpeed, "km/h",
                Icons.Outlined.DirectionsRun, accent, card, textPrimary, textSecondary, Modifier.weight(1f))
            LiveStatCard("Calories", "${state.calories}", "kcal",
                Icons.Outlined.LocalFireDepartment, accent, card, textPrimary, textSecondary, Modifier.weight(1f))
            if (config.sportType.name == "RUNNING") {
                LiveStatCard("Steps", "${state.steps}", "",
                    Icons.Outlined.DirectionsWalk, accent, card, textPrimary, textSecondary, Modifier.weight(1f))
            }
        }

        if (state.coachingMessage.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.15f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.RecordVoiceOver, null,
                    tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(state.coachingMessage, fontSize = 13.sp,
                    color = textPrimary, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            FloatingActionButton(
                onClick = { showStopDialog = true },
                modifier = Modifier.size(64.dp),
                containerColor = Color(0xFFCC3333),
                contentColor = Color.White
            ) {
                Icon(Icons.Outlined.Stop, "Stop", modifier = Modifier.size(28.dp))
            }

            FloatingActionButton(
                onClick = { if (state.isPaused) vm.resumeWorkout() else vm.pauseWorkout() },
                modifier = Modifier.size(80.dp),
                containerColor = accent,
                contentColor = config.buttonTextColor
            ) {
                Icon(
                    if (state.isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                    if (state.isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LiveStatCard(
    label: String, value: String, unit: String, icon: ImageVector,
    accent: Color, card: Color, textPrimary: Color, textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(card)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Text(unit, fontSize = 10.sp, color = accent)
        Text(label, fontSize = 10.sp, color = textSecondary)
    }
}
