package io.github.halilozel1903.weartiles.sample.tiles

import io.github.halilozel1903.weartiles.SimpleTileService
import io.github.halilozel1903.weartiles.TileState
import io.github.halilozel1903.weartiles.TileStateRequest
import io.github.halilozel1903.weartiles.TileTheme
import io.github.halilozel1903.weartiles.sample.TrailmateTiles

/** Run, ride, walk and yoga shortcuts. Nothing changes, so the tile never refreshes on its own. */
class QuickStartTileService : SimpleTileService() {

    override val theme: TileTheme get() = TrailmateTiles.theme

    override suspend fun tileState(request: TileStateRequest): TileState =
        TileState(layout = TrailmateTiles.quickStart())
}
