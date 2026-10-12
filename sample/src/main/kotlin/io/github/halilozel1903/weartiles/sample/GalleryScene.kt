package io.github.halilozel1903.weartiles.sample

import io.github.halilozel1903.weartiles.TileLayout

/**
 * The tiles of the gallery, each with fixed data from [SampleData]. `scripts/screenshots.sh` opens
 * one with `--es scene <id>`; [title] is the tile's title, checked on screen before a capture.
 */
enum class GalleryScene(val id: String, val title: String) {
    Progress("progress", "Steps"),
    Stats("stats", "Weather"),
    Shortcuts("shortcuts", "Quick start"),
    Countdown("countdown", "Rain in"),
    ;

    /** The same layout the tile service builds, on fixed data and a fixed clock. */
    fun layout(): TileLayout = when (this) {
        Progress -> TrailmateTiles.steps(SampleData.activity)
        Stats -> TrailmateTiles.weather(SampleData.weather)
        Shortcuts -> TrailmateTiles.quickStart()
        // Static text: the live countdown would tick during the screenshot.
        Countdown -> TrailmateTiles.rain(SampleData.weather, live = false, zone = TrailmateTiles.fixedZone)
    }

    companion object {
        fun from(id: String?): GalleryScene? = entries.firstOrNull { it.id == id }
    }
}
