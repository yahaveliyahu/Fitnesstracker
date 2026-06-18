package dev.yahaveliyahu.common.engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class WorkoutEngine(
    private val context: Context,
    private val config: AppConfig,
    private val userWeightKg: Double = 70.0
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _state = MutableStateFlow(WorkoutState())
    val state: StateFlow<WorkoutState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var lastLocation: Location? = null
    private var startTime: Long = 0L
    private var pausedSeconds: Long = 0L
    private var pauseStart: Long = 0L

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private var stepCount = 0

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!_state.value.isActive || _state.value.isPaused) return
            stepCount++
            _state.value = _state.value.copy(steps = stepCount)
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 2000L
    ).setMinUpdateDistanceMeters(5f).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { onNewLocation(it) }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        startTime = System.currentTimeMillis()
        _state.value = WorkoutState(isActive = true)
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        stepDetector?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        startTimer()
    }

    fun pause() {
        pauseStart = System.currentTimeMillis()
        timerJob?.cancel()
        fusedClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(stepListener)
        _state.value = _state.value.copy(isPaused = true)
    }

    @SuppressLint("MissingPermission")
    fun resume() {
        pausedSeconds += (System.currentTimeMillis() - pauseStart) / 1000L
        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        stepDetector?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        startTimer()
        _state.value = _state.value.copy(isPaused = false)
    }

    fun stop(): WorkoutSessionData {
        timerJob?.cancel()
        fusedClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(stepListener)
        scope.cancel()
        val s = _state.value
        return WorkoutSessionData(
            startTime = startTime,
            endTime = System.currentTimeMillis(),
            durationSeconds = s.elapsedSeconds,
            distanceMeters = s.distanceMeters,
            calories = s.calories,
            avgPaceSecPerKm = s.avgPaceSecPerKm,
            maxPaceSecPerKm = s.currentPaceSecPerKm,
            avgSpeedKmh = if (s.elapsedSeconds > 0)
                (s.distanceKm / (s.elapsedSeconds / 3600.0)) else 0.0,
            steps = s.steps
        )
    }

    private fun startTimer() {
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                val activeSeconds = (System.currentTimeMillis() - startTime) / 1000L - pausedSeconds
                val current = _state.value
                val calories = calculateCalories(current.distanceKm, config.metValue, userWeightKg, activeSeconds)
                val (coaching, milestone) = checkMilestone(current)
                _state.value = current.copy(
                    elapsedSeconds = activeSeconds,
                    calories = calories,
                    coachingMessage = coaching,
                    lastMilestoneKm = milestone
                )
            }
        }
    }

    private fun onNewLocation(location: Location) {
        val current = _state.value
        if (!current.isActive || current.isPaused) return
        val prev = lastLocation
        var addedDistance = 0.0
        if (prev != null) {
            val dist = prev.distanceTo(location).toDouble()
            if (dist > 2.0) addedDistance = dist
        }
        lastLocation = location
        val newDistance = current.distanceMeters + addedDistance
        val speedKmh = location.speed * 3.6
        val paceSecPerKm = if (speedKmh > 0.5) 3600.0 / speedKmh else 0.0
        val avgPace = if (newDistance > 50)
            current.elapsedSeconds / (newDistance / 1000.0) else 0.0

        _state.value = current.copy(
            distanceMeters = newDistance,
            currentSpeedKmh = speedKmh,
            currentPaceSecPerKm = paceSecPerKm,
            avgPaceSecPerKm = avgPace
        )
    }

    private fun calculateCalories(distKm: Double, met: Double, weightKg: Double, seconds: Long): Int {
        // Zero distance = zero calories. No movement, no energy spent.
        if (distKm < 0.01) return 0
        val speedKmh = if (seconds > 0) distKm / (seconds / 3600.0) else 0.0
        // Below 0.5 km/h means the user is essentially stationary
        if (speedKmh < 0.5) return 0
        val hours = seconds / 3600.0
        return (met * weightKg * hours).roundToInt()
    }

    private fun checkMilestone(state: WorkoutState): Pair<String, Int> {
        val kmCompleted = (state.distanceMeters / config.coachingMilestoneMeters).toInt()
        if (kmCompleted > state.lastMilestoneKm && kmCompleted > 0) {
            val paceStr = state.formattedPace
            val msg = when (config.sportType.name) {
                "RUNNING" -> "🏃 $kmCompleted km completed! Avg pace: $paceStr"
                else -> "🚴 $kmCompleted km completed! Avg pace: $paceStr"
            }
            return Pair(msg, kmCompleted)
        }
        return Pair(state.coachingMessage, state.lastMilestoneKm)
    }
}

data class WorkoutSessionData(
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val calories: Int,
    val avgPaceSecPerKm: Double,
    val maxPaceSecPerKm: Double,
    val avgSpeedKmh: Double,
    val steps: Int = 0
)
