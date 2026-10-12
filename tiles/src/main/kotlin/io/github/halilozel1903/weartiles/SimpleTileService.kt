package io.github.halilozel1903.weartiles

import android.util.Log
import androidx.concurrent.futures.SuspendToFutureAdapter
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.ListenableFuture
import io.github.halilozel1903.weartiles.core.Freshness
import io.github.halilozel1903.weartiles.core.RefreshPolicy
import io.github.halilozel1903.weartiles.core.TileScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * What a tile shows now and when it wants to refresh on its own.
 *
 * @property layout the layout, for example a [ProgressTile].
 * @property refresh the automatic refresh, see [RefreshPolicy] and [Freshness].
 */
public class TileState(
    public val layout: TileLayout,
    public val refresh: RefreshPolicy = RefreshPolicy.Never,
)

/**
 * A tile request as [SimpleTileService.tileState] sees it.
 *
 * @property tileId the id of this tile instance (a user can add the same tile twice).
 * @property screen the watch screen, for layout decisions.
 * @property lastClickId the id of the element whose [TileAction.Refresh] triggered this request, if any.
 * @property nowMillis the time the tile is built at.
 * @property deviceConfiguration the raw device parameters from the watch.
 */
public class TileStateRequest(
    public val tileId: Int,
    public val screen: TileScreen,
    public val lastClickId: String?,
    public val nowMillis: Long,
    public val deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
)

/**
 * A [TileService] you implement with one suspend function:
 *
 * ```kotlin
 * class StepsTileService : SimpleTileService() {
 *     override suspend fun tileState(request: TileStateRequest): TileState {
 *         val steps = repository.todaySteps()                       // suspend, any dispatcher
 *         return TileState(
 *             layout = ProgressTile(ProgressGoal("Steps", steps, 10_000.0, "steps")),
 *             refresh = RefreshPolicy.everyMinutes(15),
 *         )
 *     }
 * }
 * ```
 *
 * The service builds the layout with protolayout-material3, collects the images the layout uses
 * into versioned resources (so you never write `onTileResourcesRequest`), turns the [RefreshPolicy]
 * into the tile's freshness interval and shows [errorState] when [tileState] throws.
 *
 * Coroutines run on the main thread and are cancelled when the service is destroyed; switch to
 * `Dispatchers.IO` for disk or network work.
 */
public abstract class SimpleTileService : TileService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Resource versions of recent tiles and the images behind them. */
    private val recentImages = object : LinkedHashMap<String, Map<String, Int>>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Map<String, Int>>?): Boolean = size > 8
    }

    /** The colors of this service's tiles. */
    protected open val theme: TileTheme get() = TileTheme.Default

    /** What the tile shows now. Called for every tile request. */
    protected abstract suspend fun tileState(request: TileStateRequest): TileState

    /** What the tile shows when [tileState] failed with [error]: a message with a retry button. */
    protected open fun errorState(error: Throwable): TileState = TileState(
        layout = MessageTile(
            title = applicationInfo.loadLabel(packageManager).toString(),
            message = "Couldn't load",
            button = TileButton("Retry", TileAction.Refresh),
        ),
        refresh = RefreshPolicy.everyMinutes(15),
    )

    /** The current time; override for tests. */
    protected open fun currentTimeMillis(): Long = System.currentTimeMillis()

    final override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> =
        SuspendToFutureAdapter.launchFuture(serviceScope.coroutineContext) {
            val now = currentTimeMillis()
            val device = requestParams.deviceConfiguration
            val lastClickId = requestParams.currentState.lastClickableId.takeIf { it.isNotEmpty() }
            val state = loadState(TileStateRequest(requestParams.tileId, device.toTileScreen(), lastClickId, now, device))
            val rendered = TileRendering.render(this@SimpleTileService, device, state.layout, theme, now)
            synchronized(recentImages) { recentImages[rendered.resourcesVersion] = rendered.images }
            rendered.toTile(Freshness.intervalMillis(state.refresh, now))
        }

    final override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> =
        SuspendToFutureAdapter.launchFuture(serviceScope.coroutineContext) {
            val version = requestParams.version
            val images = synchronized(recentImages) { recentImages[version] } ?: run {
                // The process was restarted between the tile and the resources request: build the
                // tile again to find out which images it uses.
                val now = currentTimeMillis()
                val device = requestParams.deviceConfiguration
                val state = loadState(TileStateRequest(requestParams.tileId, device.toTileScreen(), null, now, device))
                TileRendering.render(this@SimpleTileService, device, state.layout, theme, now).images
            }
            TileRendering.resources(version, images)
        }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun loadState(request: TileStateRequest): TileState = try {
        tileState(request)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        Log.w(TAG, "tileState() failed, showing the error state", error)
        errorState(error)
    }

    private companion object {
        const val TAG = "SimpleTileService"
    }
}
