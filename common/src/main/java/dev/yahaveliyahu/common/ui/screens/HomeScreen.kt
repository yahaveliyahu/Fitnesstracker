package dev.yahaveliyahu.common.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
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
import dev.yahaveliyahu.common.data.WorkoutSession
import dev.yahaveliyahu.common.engine.FitnessViewModel
import dev.yahaveliyahu.common.engine.Screen
import java.text.SimpleDateFormat
import java.util.*


fun currentGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else      -> "Good night"
    }
}

@Composable
fun HomeScreen(vm: FitnessViewModel) {
    val config = vm.config
    val accent = config.accentColor
    val bg = config.screenBackground
    val card = config.cardBackground
    val textPrimary = config.textPrimary
    val textSecondary = config.textSecondary

    val recent by vm.recentSessions.collectAsStateWithLifecycle()
    val totalStats by vm.totalStats.collectAsStateWithLifecycle()
    val goal by vm.goal.collectAsStateWithLifecycle()
    val weekDist by vm.weeklyDistanceKm.collectAsStateWithLifecycle(0.0)
    val weekCal by vm.weeklyCalories.collectAsStateWithLifecycle(0)
    val weekBars by vm.weekDayBars.collectAsStateWithLifecycle(List(7) { 0f })
    val pbEvent by vm.personalBestEvent.collectAsStateWithLifecycle()

    pbEvent?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.clearPersonalBestEvent() },
            title = { Text("🏆 Personal Best!", color = accent, fontWeight = FontWeight.Bold) },
            text = { Text(msg, color = textPrimary) },
            confirmButton = {
                TextButton(onClick = { vm.clearPersonalBestEvent() }) {
                    Text("Awesome!", color = accent)
                }
            },
            containerColor = card
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp, 16.dp, 20.dp, 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(currentGreeting().uppercase(), fontSize = 11.sp, color = accent,
                    fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                Text(config.appName, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = textPrimary)
            }
            Box(Modifier.size(42.dp).clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(if (config.sportType.name == "RUNNING") Icons.Outlined.DirectionsRun
                else Icons.Outlined.DirectionsBike,
                    null, tint = accent, modifier = Modifier.size(22.dp))
            }
        }

        WeeklyGoalCard(weekDist, weekCal, goal?.targetDistanceKm ?: 10.0,
            accent, card, textPrimary, textSecondary)

        WeekBarChart(weekBars, accent, card, textPrimary, textSecondary)

        AllTimeStats(totalStats.sessions, totalStats.distanceKm,
            totalStats.calories, totalStats.formattedBestPace,
            accent, card, textPrimary, textSecondary)

        Button(
            onClick = { vm.startWorkout() },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = config.buttonTextColor
            )
        ) {
            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Start ${if (config.sportType.name == "RUNNING") "Run" else "Ride"}",
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        if (recent.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Recent", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                TextButton(onClick = { vm.navigate(Screen.HISTORY) }) {
                    Text("See all", color = accent, fontSize = 13.sp)
                }
            }
            recent.forEach { session ->
                SessionCard(session, accent, card, textPrimary, textSecondary)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun WeeklyGoalCard(
    weekDist: Double, weekCal: Int, target: Double,
    accent: Color, card: Color, textPrimary: Color, textSecondary: Color
) {
    val progress = (weekDist / target).toFloat().coerceIn(0f, 1f)
    Column(
        Modifier.fillMaxWidth().padding(16.dp, 12.dp)
            .clip(RoundedCornerShape(20.dp)).background(card).padding(18.dp)
    ) {
        Text("THIS WEEK", fontSize = 11.sp, color = accent,
            fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("%.1f".format(weekDist), fontSize = 48.sp,
                fontWeight = FontWeight.Bold, color = textPrimary)
            Text(" / %.0f km".format(target), fontSize = 18.sp,
                color = textSecondary, modifier = Modifier.padding(bottom = 8.dp))
        }
        Text("$weekCal kcal burned this week", fontSize = 13.sp, color = textSecondary)
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = accent,
            trackColor = accent.copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(4.dp))
        Text("${(progress * 100).toInt()}% of weekly goal",
            fontSize = 11.sp, color = textSecondary)
    }
}

@Composable
fun WeekBarChart(
    bars: List<Float>, accent: Color, card: Color,
    textPrimary: Color, textSecondary: Color
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Column(Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 12.dp)
        .clip(RoundedCornerShape(16.dp)).background(card).padding(14.dp)) {
        Text("Weekly Activity", fontSize = 14.sp,
            fontWeight = FontWeight.Medium, color = textPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom) {
            bars.zip(days).forEach { (fraction, day) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)) {
                    val height = (44 * fraction).coerceAtLeast(4f)
                    Box(Modifier.width(20.dp).height(height.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (fraction > 0) accent else accent.copy(alpha = 0.15f)))
                    Spacer(Modifier.height(4.dp))
                    Text(day, fontSize = 10.sp, color = textSecondary)
                }
            }
        }
    }
}

@Composable
fun AllTimeStats(
    sessions: Int, distKm: Double, calories: Int, bestPace: String,
    accent: Color, card: Color, textPrimary: Color, textSecondary: Color
) {
    Row(Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatMini("Total sessions", "$sessions", Icons.Outlined.FitnessCenter,
            accent, card, textPrimary, textSecondary, Modifier.weight(1f))
        StatMini("Total distance", "%.1f km".format(distKm), Icons.Outlined.Route,
            accent, card, textPrimary, textSecondary, Modifier.weight(1f))
        StatMini("Best pace", bestPace, Icons.Outlined.Speed,
            accent, card, textPrimary, textSecondary, Modifier.weight(1f))
        StatMini("Calories", "$calories", Icons.Outlined.LocalFireDepartment,
            accent, card, textPrimary, textSecondary, Modifier.weight(1f))
    }
}

@Composable
fun StatMini(
    label: String, value: String, icon: ImageVector,
    accent: Color, card: Color, textPrimary: Color, textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(card)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary,
            maxLines = 1)
        Text(label, fontSize = 9.sp, color = textSecondary, maxLines = 1)
    }
}

@Composable
fun SessionCard(
    session: WorkoutSession, accent: Color, card: Color,
    textPrimary: Color, textSecondary: Color
) {
    val sdf = SimpleDateFormat("EEE, dd MMM · HH:mm", Locale.getDefault())
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp)).background(card).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center) {
            Icon(if (session.sportType.name == "RUNNING") Icons.Outlined.DirectionsRun
            else Icons.Outlined.DirectionsBike,
                null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(session.formattedDistance, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, color = textPrimary)
                if (session.isPersonalBestDistance || session.isPersonalBestPace) {
                    Spacer(Modifier.width(6.dp))
                    Text("🏆", fontSize = 12.sp)
                }
            }
            Text(sdf.format(Date(session.startTime)), fontSize = 11.sp, color = textSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(session.formattedDuration, fontSize = 13.sp,
                fontWeight = FontWeight.Medium, color = textPrimary)
            Text("${session.calories} kcal", fontSize = 11.sp, color = textSecondary)
            if (session.sportType.name == "RUNNING" && session.steps > 0) {
                Text("${session.steps} steps", fontSize = 11.sp, color = textSecondary)
            }
        }
    }
}
