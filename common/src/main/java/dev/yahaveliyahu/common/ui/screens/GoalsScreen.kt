package dev.yahaveliyahu.common.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.yahaveliyahu.common.data.WeeklyGoal
import dev.yahaveliyahu.common.engine.FitnessViewModel

@Composable
fun GoalsScreen(vm: FitnessViewModel) {
    val config = vm.config
    val accent = config.accentColor
    val bg = config.screenBackground
    val card = config.cardBackground
    val textPrimary = config.textPrimary
    val textSecondary = config.textSecondary

    val goal by vm.goal.collectAsStateWithLifecycle()
    val weekDist by vm.weeklyDistanceKm.collectAsStateWithLifecycle(0.0)
    val weekSessions by vm.weekSessions.collectAsStateWithLifecycle()
    val totalStats by vm.totalStats.collectAsStateWithLifecycle()

    var distanceInput by remember(goal) {
        mutableStateOf(goal?.targetDistanceKm?.toInt()?.toString() ?: "20")
    }
    var sessionsInput by remember(goal) {
        mutableStateOf(goal?.targetSessions?.toString() ?: "3")
    }
    var saved by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(bg)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Goals", fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = textPrimary, modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 16.dp))

        val targetDist = distanceInput.toDoubleOrNull() ?: 20.0
        val targetSess = sessionsInput.toIntOrNull() ?: 3
        val distProgress = (weekDist / targetDist).toFloat().coerceIn(0f, 1f)
        val sessProgress = (weekSessions.size.toFloat() / targetSess).coerceIn(0f, 1f)

        GoalProgressCard("Weekly Distance Goal",
            "%.1f / %.0f km".format(weekDist, targetDist),
            distProgress, Icons.Outlined.Route, accent, card, textPrimary, textSecondary)

        Spacer(Modifier.height(10.dp))

        GoalProgressCard("Weekly Sessions Goal",
            "${weekSessions.size} / $targetSess sessions",
            sessProgress, Icons.Outlined.FitnessCenter, accent, card, textPrimary, textSecondary)

        Spacer(Modifier.height(16.dp))

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)).background(card).padding(16.dp)
        ) {
            Text("Set Weekly Goals", fontSize = 16.sp,
                fontWeight = FontWeight.Bold, color = textPrimary)
            Spacer(Modifier.height(14.dp))

            Text("Distance target (km)", fontSize = 13.sp, color = textSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = distanceInput,
                onValueChange = { distanceInput = it; saved = false },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text("Sessions target", fontSize = 13.sp, color = textSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = sessionsInput,
                onValueChange = { sessionsInput = it; saved = false },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val d = distanceInput.toDoubleOrNull() ?: 20.0
                    val s = sessionsInput.toIntOrNull() ?: 3
                    vm.saveGoal(d, s)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent, contentColor = config.buttonTextColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (saved) "✓ Saved!" else "Save Goals", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)).background(card).padding(16.dp)
        ) {
            Text("All-time Records", fontSize = 16.sp,
                fontWeight = FontWeight.Bold, color = textPrimary)
            Spacer(Modifier.height(12.dp))
            RecordRow("Longest session", "%.2f km".format(totalStats.bestDistanceKm),
                Icons.Outlined.EmojiEvents, accent, textPrimary, textSecondary)
            Divider(color = textSecondary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            RecordRow("Fastest pace", totalStats.formattedBestPace,
                Icons.Outlined.Speed, accent, textPrimary, textSecondary)
            Divider(color = textSecondary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            RecordRow("Total sessions", "${totalStats.sessions}",
                Icons.Outlined.CheckCircle, accent, textPrimary, textSecondary)
            Divider(color = textSecondary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            RecordRow("Total distance", "%.1f km".format(totalStats.distanceKm),
                Icons.Outlined.Route, accent, textPrimary, textSecondary)
            Divider(color = textSecondary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            RecordRow("Total calories", "${totalStats.calories} kcal",
                Icons.Outlined.LocalFireDepartment, accent, textPrimary, textSecondary)
        }
    }
}

@Composable
fun GoalProgressCard(
    title: String, subtitle: String, progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color, card: Color, textPrimary: Color, textSecondary: Color
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)).background(card).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textPrimary)
            Spacer(Modifier.weight(1f))
            Text("${(progress * 100).toInt()}%", fontSize = 14.sp,
                fontWeight = FontWeight.Bold, color = accent)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = accent,
            trackColor = accent.copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 12.sp, color = textSecondary)
    }
}

@Composable
fun RecordRow(
    label: String, value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color, textPrimary: Color, textSecondary: Color
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = textSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
    }
}
