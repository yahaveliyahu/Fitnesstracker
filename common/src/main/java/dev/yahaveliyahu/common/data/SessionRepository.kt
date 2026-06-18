package dev.yahaveliyahu.common.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class SessionRepository(context: Context) {
    private val dao = FitnessDatabase.getInstance(context).workoutDao()

    suspend fun saveSession(session: WorkoutSession): Long {
        val bestDist = dao.getPersonalBestDistance(session.sportType) ?: 0.0
        val bestPace = dao.getPersonalBestPace(session.sportType) ?: Double.MAX_VALUE
        val isPBDist = session.distanceMeters > bestDist
        val isPBPace = session.avgPaceSecondsPerKm > 0 && session.avgPaceSecondsPerKm < bestPace
        return dao.insertSession(session.copy(
            isPersonalBestDistance = isPBDist,
            isPersonalBestPace = isPBPace
        ))
    }

    fun getRecentSessions(sport: SportType): Flow<List<WorkoutSession>> =
        dao.getRecentSessions(sport)

    fun getAllSessions(sport: SportType): Flow<List<WorkoutSession>> =
        dao.getSessionsBySport(sport)

    fun getWeekSessions(sport: SportType): Flow<List<WorkoutSession>> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dao.getWeekSessions(sport, cal.timeInMillis)
    }

    suspend fun getTotalStats(sport: SportType): TotalStats = TotalStats(
        sessions = dao.getTotalSessionCount(sport),
        distanceKm = (dao.getTotalDistanceMeters(sport) ?: 0.0) / 1000.0,
        calories = dao.getTotalCalories(sport) ?: 0,
        bestDistanceKm = (dao.getPersonalBestDistance(sport) ?: 0.0) / 1000.0,
        bestPaceSecPerKm = dao.getPersonalBestPace(sport) ?: 0.0
    )

    fun getGoal(sport: SportType): Flow<WeeklyGoal?> =
        dao.getGoal(sport.name)

    suspend fun saveGoal(goal: WeeklyGoal) = dao.upsertGoal(goal)
}

data class TotalStats(
    val sessions: Int,
    val distanceKm: Double,
    val calories: Int,
    val bestDistanceKm: Double,
    val bestPaceSecPerKm: Double
) {
    val formattedBestPace: String get() {
        if (bestPaceSecPerKm <= 0) return "--:--"
        val m = (bestPaceSecPerKm / 60).toInt()
        val s = (bestPaceSecPerKm % 60).toInt()
        return "%d:%02d /km".format(m, s)
    }
}
