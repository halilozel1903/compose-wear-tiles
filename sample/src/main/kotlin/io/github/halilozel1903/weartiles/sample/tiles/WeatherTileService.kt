package io.github.halilozel1903.weartiles.sample.tiles

import io.github.halilozel1903.weartiles.SimpleTileService
import io.github.halilozel1903.weartiles.TileState
import io.github.halilozel1903.weartiles.TileStateRequest
import io.github.halilozel1903.weartiles.TileTheme
import io.github.halilozel1903.weartiles.core.Freshness
import io.github.halilozel1903.weartiles.core.RefreshPolicy
import io.github.halilozel1903.weartiles.sample.TrailmateRepository
import io.github.halilozel1903.weartiles.sample.TrailmateTiles

/**
 * Temperature, wind and UV. Forecasts change on the hour, so the tile refreshes at the top of every
 * hour, and every 30 minutes as a fallback. Tapping a card refreshes it (TileAction.Refresh).
 */
class WeatherTileService : SimpleTileService() {

    override val theme: TileTheme get() = TrailmateTiles.theme

    override suspend fun tileState(request: TileStateRequest): TileState {
        val weather = TrailmateRepository.weather(request.nowMillis)
        val stale = Freshness.isStale(weather.updatedAtMillis, request.nowMillis, maxAgeMillis = 2 * 3_600_000L)
        return TileState(
            layout = TrailmateTiles.weather(weather),
            refresh = if (stale) {
                RefreshPolicy.everyMinutes(5)
            } else {
                RefreshPolicy.earliestOf(RefreshPolicy.hourly(), RefreshPolicy.everyMinutes(30))
            },
        )
    }
}
