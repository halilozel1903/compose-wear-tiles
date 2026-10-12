package io.github.halilozel1903.weartiles.sample

import android.content.Context
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import io.github.halilozel1903.weartiles.previewData

// Android Studio tile previews of the four Trailmate tiles on small and large round watches.

@Preview(device = WearDevices.LARGE_ROUND, name = "Steps")
@Preview(device = WearDevices.SMALL_ROUND, name = "Steps, small")
fun stepsTilePreview(context: Context): TilePreviewData =
    TrailmateTiles.steps(SampleData.activity).previewData(context, TrailmateTiles.theme, SampleData.NOW)

@Preview(device = WearDevices.LARGE_ROUND, name = "Weather")
@Preview(device = WearDevices.SMALL_ROUND, name = "Weather, small")
fun weatherTilePreview(context: Context): TilePreviewData =
    TrailmateTiles.weather(SampleData.weather).previewData(context, TrailmateTiles.theme, SampleData.NOW)

@Preview(device = WearDevices.LARGE_ROUND, name = "Quick start")
@Preview(device = WearDevices.SMALL_ROUND, name = "Quick start, small")
fun quickStartTilePreview(context: Context): TilePreviewData =
    TrailmateTiles.quickStart().previewData(context, TrailmateTiles.theme, SampleData.NOW)

@Preview(device = WearDevices.LARGE_ROUND, name = "Rain")
@Preview(device = WearDevices.SMALL_ROUND, name = "Rain, small")
fun rainTilePreview(context: Context): TilePreviewData =
    TrailmateTiles.rain(SampleData.weather, live = false, zone = TrailmateTiles.fixedZone)
        .previewData(context, TrailmateTiles.theme, SampleData.NOW)
