package dev.yahaveliyahu.common.engine

data class WorkoutState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentPaceSecPerKm: Double = 0.0,
    val avgPaceSecPerKm: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val calories: Int = 0,
    val steps: Int = 0,
    val coachingMessage: String = "",
    val lastMilestoneKm: Int = 0
) {
    val distanceKm: Double get() = distanceMeters / 1000.0

    val formattedTime: String get() {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    val formattedDistance: String get() = "%.2f".format(distanceKm)

    val formattedPace: String get() {
        if (avgPaceSecPerKm <= 0) return "--:--"
        val m = (avgPaceSecPerKm / 60).toInt()
        val s = (avgPaceSecPerKm % 60).toInt()
        return "%d:%02d".format(m, s)
    }

    val formattedSpeed: String get() = "%.1f".format(currentSpeedKmh)
}
