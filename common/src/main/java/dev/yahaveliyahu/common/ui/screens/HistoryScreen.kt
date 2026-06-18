package dev.yahaveliyahu.common.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.yahaveliyahu.common.data.WorkoutSession
import dev.yahaveliyahu.common.engine.FitnessViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(vm: FitnessViewModel) {
    val config = vm.config
    val accent = config.accentColor
    val bg = config.screenBackground
    val card = config.cardBackground
    val textPrimary = config.textPrimary
    val textSecondary = config.textSecondary

    val sessions by vm.allSessions.collectAsStateWithLifecycle()
    val totalStats by vm.totalStats.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(bg)) {
        Text("History", fontSize = 26.sp, fontWeight = FontWeight.Bold,
            color = textPrimary, modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 8.dp))

        if (totalStats.sessions > 0) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp, 0.dp, 16.dp, 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PBCard("Best distance",
                    "%.2f km".format(totalStats.bestDistanceKm),
                    Icons.Outlined.EmojiEvents, accent, card, textPrimary, textSecondary,
                    Modifier.weight(1f))
                PBCard("Best pace", totalStats.formattedBestPace,
                    Icons.Outlined.Speed, accent, card, textPrimary, textSecondary,
                    Modifier.weight(1f))
                PBCard("Total runs", "${totalStats.sessions}",
                    Icons.Outlined.CheckCircle, accent, card, textPrimary, textSecondary,
                    Modifier.weight(1f))
            }
        }

        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.History, null, tint = textSecondary,
                        modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No workouts yet", fontSize = 18.sp, color = textSecondary)
                    Text("Start your first session!", fontSize = 14.sp,
                        color = textSecondary.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    HistorySessionCard(session, accent, card, textPrimary, textSecondary)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun PBCard(
    label: String, value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color, card: Color, textPrimary: Color, textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(card)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
        Text(label, fontSize = 10.sp, color = textSecondary)
    }
}

@Composable
fun HistorySessionCard(
    session: WorkoutSession,
    accent: Color, card: Color, textPrimary: Color, textSecondary: Color
) {
    val sdf = SimpleDateFormat("EEE dd MMM, HH:mm", Locale.getDefault())
    val isPB = session.isPersonalBestDistance || session.isPersonalBestPace
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPB) accent.copy(alpha = 0.1f) else card)
            .then(if (isPB) Modifier.border(1.dp, accent.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)) else Modifier)
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(session.formattedDistance, fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = if (isPB) accent else textPrimary)
                if (isPB) {
                    Spacer(Modifier.width(8.dp))
                    Surface(color = accent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)) {
                        Text("🏆 PB", fontSize = 11.sp, color = accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(session.formattedDuration, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, color = textPrimary)
            }
            Spacer(Modifier.height(6.dp))
            Text(sdf.format(Date(session.startTime)), fontSize = 12.sp, color = textSecondary)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HistoryStat("Avg pace", session.formattedPace, accent, textSecondary)
                HistoryStat("Avg speed", "%.1f km/h".format(session.avgSpeedKmh),
                    accent, textSecondary)
                HistoryStat("Calories", "${session.calories} kcal", accent, textSecondary)
            }
        }
    }
}

@Composable
fun HistoryStat(label: String, value: String, accent: Color, textSecondary: Color) {
    Column {
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = accent)
        Text(label, fontSize = 10.sp, color = textSecondary)
    }
}
