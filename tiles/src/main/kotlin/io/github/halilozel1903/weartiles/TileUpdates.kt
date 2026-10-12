package io.github.halilozel1903.weartiles

import android.content.Context
import androidx.wear.tiles.TileService

/**
 * Asks Wear OS for fresh tiles when your data changed, instead of waiting for the next automatic
 * refresh. The system may batch and throttle requests; tiles that are not added do nothing.
 *
 * ```kotlin
 * repository.logWorkout(run)
 * context.requestTileUpdate<StepsTileService>()
 * ```
 */
public object TileUpdates {

    /** Requests an update of every tile of [service]. */
    public fun request(context: Context, service: Class<out TileService>) {
        TileService.getUpdater(context).requestUpdate(service)
    }

    /** Requests an update of every tile of each of [services]. */
    public fun requestAll(context: Context, vararg services: Class<out TileService>) {
        services.forEach { request(context, it) }
    }
}

/** Requests an update of every tile of the service [T]. */
public inline fun <reified T : TileService> Context.requestTileUpdate() {
    TileUpdates.request(this, T::class.java)
}
