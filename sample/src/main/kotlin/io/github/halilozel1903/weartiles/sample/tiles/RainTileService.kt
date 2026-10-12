package io.github.halilozel1903.weartiles.sample.tiles

import io.github.halilozel1903.weartiles.SimpleTileService
import io.github.halilozel1903.weartiles.TileState
import io.github.halilozel1903.weartiles.TileStateRequest
import io.github.halilozel1903.weartiles.TileTheme
import io.github.halilozel1903.weartiles.core.RefreshPolicy
import io.github.halilozel1903.weartiles.sample.TrailmateRepository
import io.github.halilozel1903.weartiles.sample.TrailmateTiles

/**
 * Time until the next rain. The countdown ticks on the watch by itself; the tile only asks for new
 * data when the rain starts, and hourly for a new forecast.
 */
class RainTileService : SimpleTileService() {

    override val theme: TileTheme get() = TrailmateTiles.theme

    override suspend fun tileState(request: TileStateRequest): TileState {
        val weather = TrailmateRepository.weather(request.nowMillis)
        val countdown = TrailmateTiles.rainCountdown(weather)
        return TileState(
            layout = TrailmateTiles.rain(weather, live = true),
            refresh = RefreshPolicy.earliestOf(
                countdown.refreshPolicy(request.nowMillis, live = true),
                RefreshPolicy.hourly(),
            ),
        )
    }
}
