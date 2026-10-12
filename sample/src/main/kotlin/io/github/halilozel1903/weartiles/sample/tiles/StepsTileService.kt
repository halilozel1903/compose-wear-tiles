package io.github.halilozel1903.weartiles.sample.tiles

import io.github.halilozel1903.weartiles.SimpleTileService
import io.github.halilozel1903.weartiles.TileState
import io.github.halilozel1903.weartiles.TileStateRequest
import io.github.halilozel1903.weartiles.TileTheme
import io.github.halilozel1903.weartiles.core.RefreshPolicy
import io.github.halilozel1903.weartiles.sample.TrailmateRepository
import io.github.halilozel1903.weartiles.sample.TrailmateTiles

/** Steps towards today's goal; refreshes every 15 minutes and right after a walk is logged. */
class StepsTileService : SimpleTileService() {

    override val theme: TileTheme get() = TrailmateTiles.theme

    override suspend fun tileState(request: TileStateRequest): TileState {
        val activity = TrailmateRepository.activity(request.nowMillis)
        return TileState(
            layout = TrailmateTiles.steps(activity),
            refresh = RefreshPolicy.everyMinutes(15),
        )
    }
}
