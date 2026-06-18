package dev.yahaveliyahu.common.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SportType { RUNNING, CYCLING }

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sportType: SportType,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val calories: Int,
    val avgPaceSecondsPerKm: Double,
    val maxPaceSecondsPerKm: Double,
    val avgSpeedKmh: Double,
    val steps: Int = 0,
    val isPersonalBestDistance: Boolean = false,
    val isPersonalBestPace: Boolean = false
) {
    val distanceKm: Double get() = distanceMeters / 1000.0
    val formattedDuration: String get() {
        val h = durationSeconds / 3600
        val m = (durationSeconds % 3600) / 60
        val s = durationSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }
    val formattedPace: String get() {
        if (avgPaceSecondsPerKm <= 0) return "--:--"
        val m = (avgPaceSecondsPerKm / 60).toInt()
        val s = (avgPaceSecondsPerKm % 60).toInt()
        return "%d:%02d /km".format(m, s)
    }
    val formattedDistance: String get() = "%.2f km".format(distanceKm)
}
