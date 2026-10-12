package io.github.halilozel1903.weartiles.sample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Today's activity of the fictional Trailmate user. */
data class DailyActivity(val steps: Int, val stepGoal: Int, val heartRate: Int, val energyKcal: Int)

/** The weather at the user's location. */
data class Weather(
    val temperatureC: Int,
    val windKmh: Int,
    val windRising: Boolean,
    val uvIndex: Int,
    val updatedAtMillis: Long,
    val rainStartsAtMillis: Long,
    val forecastAtMillis: Long,
    val rainLabel: String,
)

/**
 * A pretend data source. A real app would read Health Services and a weather API; here the values
 * only move with the clock so the tiles have something to refresh.
 */
object TrailmateRepository {

    /** Extra steps logged in the app with "Log walk" (see [WorkoutActivity]). */
    @Volatile
    var loggedSteps: Int = 0

    suspend fun activity(nowMillis: Long): DailyActivity = withContext(Dispatchers.Default) {
        delay(50) // pretend to read sensors
        val minuteOfDay = (nowMillis / 60_000 % (24 * 60)).toInt()
        DailyActivity(
            steps = 4_000 + minuteOfDay * 6 + loggedSteps,
            stepGoal = 10_000,
            heartRate = 68 + minuteOfDay % 9,
            energyKcal = 900 + minuteOfDay,
        )
    }

    suspend fun weather(nowMillis: Long): Weather = withContext(Dispatchers.Default) {
        delay(50) // pretend to call a weather API
        val hour = 3_600_000L
        val forecastAt = nowMillis - nowMillis % hour
        Weather(
            temperatureC = 18,
            windKmh = 12,
            windRising = true,
            uvIndex = 4,
            updatedAtMillis = forecastAt,
            rainStartsAtMillis = forecastAt + 2 * hour + 25 * 60_000L,
            forecastAtMillis = forecastAt,
            rainLabel = "Light rain",
        )
    }
}

/**
 * Fixed data for the tile gallery and the previews: the same picture on every run.
 * The clock reads 2026-10-06 14:30 UTC.
 */
object SampleData {
    const val NOW: Long = 1_791_297_000_000L

    val activity = DailyActivity(steps = 8_421, stepGoal = 10_000, heartRate = 72, energyKcal = 1_840)

    val weather = Weather(
        temperatureC = 18,
        windKmh = 12,
        windRising = true,
        uvIndex = 4,
        updatedAtMillis = NOW - 5 * 60_000L,
        rainStartsAtMillis = NOW + 85 * 60_000L + 30_000L,
        forecastAtMillis = NOW - 35 * 60_000L,
        rainLabel = "Light rain",
    )
}
