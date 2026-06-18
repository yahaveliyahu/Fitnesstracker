package dev.yahaveliyahu.common.engine

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.yahaveliyahu.common.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FitnessViewModel(
    application: Application,
    val config: AppConfig
) : AndroidViewModel(application) {

    private val repo = SessionRepository(application)
    private var engine: WorkoutEngine? = null

    val workoutState: StateFlow<WorkoutState> get() =
        engine?.state ?: MutableStateFlow(WorkoutState())

    private val _activeWorkoutState = MutableStateFlow(WorkoutState())
    val activeState: StateFlow<WorkoutState> = _activeWorkoutState

    val recentSessions = repo.getRecentSessions(config.sportType)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allSessions = repo.getAllSessions(config.sportType)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val weekSessions = repo.getWeekSessions(config.sportType)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val goal = repo.getGoal(config.sportType)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _totalStats = MutableStateFlow(TotalStats(0, 0.0, 0, 0.0, 0.0))
    val totalStats: StateFlow<TotalStats> = _totalStats

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _personalBestEvent = MutableStateFlow<String?>(null)
    val personalBestEvent: StateFlow<String?> = _personalBestEvent

    init {
        viewModelScope.launch {
            _totalStats.value = repo.getTotalStats(config.sportType)
        }
    }

    fun navigate(screen: Screen) { _currentScreen.value = screen }

    fun startWorkout() {
        val ctx = getApplication<Application>()
        engine = WorkoutEngine(ctx, config)
        engine!!.start()
        viewModelScope.launch {
            engine!!.state.collect { state ->
                _activeWorkoutState.value = state
            }
        }
        navigate(Screen.ACTIVE_WORKOUT)
    }

    fun pauseWorkout() { engine?.pause() }
    fun resumeWorkout() { engine?.resume() }

    fun stopWorkout() {
        val data = engine?.stop() ?: return
        engine = null
        viewModelScope.launch {
            val session = WorkoutSession(
                sportType = config.sportType,
                startTime = data.startTime,
                endTime = data.endTime,
                durationSeconds = data.durationSeconds,
                distanceMeters = data.distanceMeters,
                calories = data.calories,
                avgPaceSecondsPerKm = data.avgPaceSecPerKm,
                maxPaceSecondsPerKm = data.maxPaceSecPerKm,
                avgSpeedKmh = data.avgSpeedKmh,
                steps = data.steps
            )
            val id = repo.saveSession(session)
            val saved = repo.getAllSessions(config.sportType).first().find { it.id == id }
            if (saved?.isPersonalBestDistance == true || saved?.isPersonalBestPace == true) {
                val msg = buildString {
                    append("🏆 New Personal Best! ")
                    if (saved.isPersonalBestDistance) append("Longest ${config.sportType.name.lowercase()}: ${saved.formattedDistance}")
                    if (saved.isPersonalBestPace) append(" Fastest pace: ${saved.formattedPace}")
                }
                _personalBestEvent.value = msg
                sendPBNotification(msg)
            }
            _totalStats.value = repo.getTotalStats(config.sportType)
            _activeWorkoutState.value = WorkoutState()
        }
        navigate(Screen.HOME)
    }

    fun saveGoal(distanceKm: Double, sessions: Int) {
        viewModelScope.launch {
            repo.saveGoal(WeeklyGoal(config.sportType.name, distanceKm, sessions))
        }
    }

    fun clearPersonalBestEvent() { _personalBestEvent.value = null }

    val weeklyDistanceKm: Flow<Double> = weekSessions.map { list ->
        list.sumOf { it.distanceMeters } / 1000.0
    }

    val weeklyCalories: Flow<Int> = weekSessions.map { list ->
        list.sumOf { it.calories }
    }

    val weekDayBars: Flow<List<Float>> = weekSessions.map { sessions ->
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_WEEK)
        val days = (0..6).map { offset ->
            cal.apply {
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val dayStart = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = cal.timeInMillis
            sessions.filter { it.startTime in dayStart until dayEnd }
                .sumOf { it.distanceMeters }.toFloat()
        }
        val max = days.maxOrNull()?.takeIf { it > 0 } ?: 1f
        days.map { it / max }
    }

    private fun sendPBNotification(message: String) {
        val ctx = getApplication<Application>()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "personal_bests"
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Personal Bests", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val notif = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle("Personal Best!")
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }
}

enum class Screen { HOME, ACTIVE_WORKOUT, HISTORY, GOALS }
