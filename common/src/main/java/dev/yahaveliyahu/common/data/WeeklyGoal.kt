package dev.yahaveliyahu.common.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_goals")
data class WeeklyGoal(
    @PrimaryKey val sportType: String,
    val targetDistanceKm: Double,
    val targetSessions: Int
)
