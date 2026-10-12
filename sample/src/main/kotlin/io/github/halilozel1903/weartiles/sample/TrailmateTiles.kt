package io.github.halilozel1903.weartiles.sample

import androidx.wear.protolayout.material3.ColorScheme
import androidx.wear.protolayout.types.argb
import io.github.halilozel1903.weartiles.CountdownTile
import io.github.halilozel1903.weartiles.ProgressTile
import io.github.halilozel1903.weartiles.ShortcutsTile
import io.github.halilozel1903.weartiles.StatsTile
import io.github.halilozel1903.weartiles.TileAction
import io.github.halilozel1903.weartiles.TileButton
import io.github.halilozel1903.weartiles.TileShortcut
import io.github.halilozel1903.weartiles.TileTheme
import io.github.halilozel1903.weartiles.core.Countdown
import io.github.halilozel1903.weartiles.core.ProgressGoal
import io.github.halilozel1903.weartiles.core.StatItem
import io.github.halilozel1903.weartiles.core.StatList
import io.github.halilozel1903.weartiles.core.TileFormat
import io.github.halilozel1903.weartiles.core.Trend
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * The four Trailmate tiles. The tile services, the in-app tile gallery and the Android Studio
 * previews all build their layouts here, so the gallery shows exactly what the watch shows.
 */
object TrailmateTiles {

    /** Mint, sky and sunset on black: Trailmate's colors (dynamic colors are off for stable screenshots). */
    val theme = TileTheme(
        colorScheme = ColorScheme(
            primary = 0xFF8AD9B0.argb,
            onPrimary = 0xFF00382A.argb,
            primaryContainer = 0xFF1F5C47.argb,
            onPrimaryContainer = 0xFFB8F5D6.argb,
            secondary = 0xFF9CCBFF.argb,
            onSecondary = 0xFF00315A.argb,
            secondaryContainer = 0xFF1D4A72.argb,
            onSecondaryContainer = 0xFFD2E7FF.argb,
            tertiary = 0xFFFFB68A.argb,
            onTertiary = 0xFF4F2500.argb,
            tertiaryContainer = 0xFF6E3A12.argb,
            onTertiaryContainer = 0xFFFFDCC7.argb,
            surfaceContainerLow = 0xFF161D24.argb,
            surfaceContainer = 0xFF1C252E.argb,
            surfaceContainerHigh = 0xFF26313B.argb,
            onSurface = 0xFFE6EDF3.argb,
            onSurfaceVariant = 0xFFB4C0CB.argb,
            outline = 0xFF7E8B97.argb,
            background = 0xFF000000.argb,
            onBackground = 0xFFFFFFFF.argb,
        ),
        allowDynamicColors = false,
    )

    fun steps(activity: DailyActivity): ProgressTile = ProgressTile(
        goal = ProgressGoal("Steps", activity.steps.toDouble(), activity.stepGoal.toDouble(), unit = "steps"),
        icon = R.drawable.ic_steps,
        button = TileButton("Log walk", TileAction.launch<WorkoutActivity>(WorkoutActivity.EXTRA_TYPE to "walk")),
        onClick = TileAction.launch<MainActivity>(),
    )

    fun weather(weather: Weather): StatsTile = StatsTile(
        StatList(
            title = "Weather",
            items = listOf(
                StatItem("Temp", "${weather.temperatureC}°"),
                StatItem("Wind", weather.windKmh.toString(), unit = "km/h", trend = if (weather.windRising) Trend.Up else Trend.Down),
                StatItem("UV", weather.uvIndex.toString(), unit = uvLevel(weather.uvIndex)),
            ),
            updatedAtMillis = weather.updatedAtMillis,
        ),
    )

    fun quickStart(): ShortcutsTile = ShortcutsTile(
        title = "Quick start",
        shortcuts = listOf(
            workout("Run", R.drawable.ic_run, "run"),
            workout("Ride", R.drawable.ic_bike, "ride"),
            workout("Walk", R.drawable.ic_walk, "walk"),
            workout("Yoga", R.drawable.ic_yoga, "yoga"),
        ),
    )

    fun rain(weather: Weather, live: Boolean, zone: ZoneId = ZoneId.systemDefault()): CountdownTile = CountdownTile(
        countdown = rainCountdown(weather, zone),
        live = live,
        onClick = TileAction.launch<MainActivity>(MainActivity.EXTRA_SCENE to "countdown"),
    )

    fun rainCountdown(weather: Weather, zone: ZoneId = ZoneId.systemDefault()): Countdown = Countdown(
        title = "Rain in",
        targetMillis = weather.rainStartsAtMillis,
        startMillis = weather.forecastAtMillis,
        doneLabel = "Raining",
        subtitle = "${weather.rainLabel} at ${TileFormat.timeOfDay(weather.rainStartsAtMillis, zone)}",
    )

    /** The gallery and previews show UTC times so the screenshots never change. */
    val fixedZone: ZoneId = ZoneOffset.UTC

    private fun workout(label: String, icon: Int, type: String) = TileShortcut(
        label = label,
        icon = icon,
        action = TileAction.launch<WorkoutActivity>(WorkoutActivity.EXTRA_TYPE to type),
    )

    private fun uvLevel(index: Int): String = when {
        index <= 2 -> "low"
        index <= 5 -> "moderate"
        index <= 7 -> "high"
        else -> "very high"
    }
}
