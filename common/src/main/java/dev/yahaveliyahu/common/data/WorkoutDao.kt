package dev.yahaveliyahu.common.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions WHERE sportType = :sport ORDER BY startTime DESC")
    fun getSessionsBySport(sport: SportType): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE sportType = :sport ORDER BY startTime DESC LIMIT 5")
    fun getRecentSessions(sport: SportType): Flow<List<WorkoutSession>>

    @Query("""
        SELECT * FROM workout_sessions 
        WHERE sportType = :sport AND startTime >= :weekStart
        ORDER BY startTime DESC
    """)
    fun getWeekSessions(sport: SportType, weekStart: Long): Flow<List<WorkoutSession>>

    @Query("SELECT MAX(distanceMeters) FROM workout_sessions WHERE sportType = :sport")
    suspend fun getPersonalBestDistance(sport: SportType): Double?

    @Query("SELECT MIN(avgPaceSecondsPerKm) FROM workout_sessions WHERE sportType = :sport AND avgPaceSecondsPerKm > 0")
    suspend fun getPersonalBestPace(sport: SportType): Double?

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE sportType = :sport")
    suspend fun getTotalSessionCount(sport: SportType): Int

    @Query("SELECT SUM(distanceMeters) FROM workout_sessions WHERE sportType = :sport")
    suspend fun getTotalDistanceMeters(sport: SportType): Double?

    @Query("SELECT SUM(calories) FROM workout_sessions WHERE sportType = :sport")
    suspend fun getTotalCalories(sport: SportType): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: WeeklyGoal)

    @Query("SELECT * FROM weekly_goals WHERE sportType = :sport")
    fun getGoal(sport: String): Flow<WeeklyGoal?>
}
