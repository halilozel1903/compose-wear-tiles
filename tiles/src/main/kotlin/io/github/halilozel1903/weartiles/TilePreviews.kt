package io.github.halilozel1903.weartiles

import android.content.Context
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import io.github.halilozel1903.weartiles.core.Countdown
import io.github.halilozel1903.weartiles.core.ProgressGoal
import io.github.halilozel1903.weartiles.core.StatItem
import io.github.halilozel1903.weartiles.core.StatList
import io.github.halilozel1903.weartiles.core.Trend

/**
 * Preview data for Android Studio's tile previews (`androidx.wear.tiles:tiles-tooling`):
 *
 * ```kotlin
 * @Preview(device = WearDevices.LARGE_ROUND)
 * @Preview(device = WearDevices.SMALL_ROUND)
 * fun stepsTilePreview(context: Context) = StepsTiles.progress(sampleSteps).previewData(context)
 * ```
 *
 * Add `debugImplementation("androidx.wear.tiles:tiles-tooling:1.5.0")` to the module with the previews.
 */
public fun TileLayout.previewData(
    context: Context,
    theme: TileTheme = TileTheme.Default,
    nowMillis: Long = System.currentTimeMillis(),
): TilePreviewData {
    val layout = this
    return TilePreviewData(
        onTileResourceRequest = { request ->
            val rendered = TileRendering.render(context, request.deviceConfiguration, layout, theme, nowMillis)
            TileRendering.resources(request.version, rendered.images)
        },
        platformDataValues = null,
        onTileRequest = { request ->
            TileRendering.render(context, request.deviceConfiguration, layout, theme, nowMillis).toTile()
        },
    )
}

private const val PREVIEW_NOW = 1_791_297_000_000L // 2026-10-06 14:30 UTC

@Preview(device = WearDevices.LARGE_ROUND, name = "Progress, large round")
@Preview(device = WearDevices.SMALL_ROUND, name = "Progress, small round")
private fun progressTilePreview(context: Context): TilePreviewData =
    ProgressTile(
        goal = ProgressGoal("Steps", current = 8_421.0, target = 10_000.0, unit = "steps"),
        button = TileButton("Walk", TileAction.Refresh),
    ).previewData(context, nowMillis = PREVIEW_NOW)

@Preview(device = WearDevices.LARGE_ROUND, name = "Stats, large round")
@Preview(device = WearDevices.SMALL_ROUND, name = "Stats, small round")
private fun statsTilePreview(context: Context): TilePreviewData =
    StatsTile(
        StatList(
            title = "Today",
            items = listOf(
                StatItem.number("Steps", 12_400.0, trend = Trend.Up),
                StatItem("Heart", "72", unit = "bpm"),
                StatItem.number("Energy", 1_840.0, unit = "kcal"),
            ),
            updatedAtMillis = PREVIEW_NOW - 5 * 60_000,
        ),
    ).previewData(context, nowMillis = PREVIEW_NOW)

@Preview(device = WearDevices.LARGE_ROUND, name = "Countdown, large round")
@Preview(device = WearDevices.SMALL_ROUND, name = "Countdown, small round")
private fun countdownTilePreview(context: Context): TilePreviewData =
    CountdownTile(
        Countdown(
            title = "Race starts in",
            targetMillis = PREVIEW_NOW + (2 * 60 + 15) * 60_000L,
            startMillis = PREVIEW_NOW - 6 * 3_600_000L,
            subtitle = "City 10K at 16:45",
        ),
        live = false,
    ).previewData(context, nowMillis = PREVIEW_NOW)
