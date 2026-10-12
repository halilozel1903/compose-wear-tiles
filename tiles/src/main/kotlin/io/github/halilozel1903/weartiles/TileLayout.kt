package io.github.halilozel1903.weartiles

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material3.ColorScheme
import androidx.wear.protolayout.material3.MaterialScope
import io.github.halilozel1903.weartiles.core.TileLayoutSpec
import io.github.halilozel1903.weartiles.core.TileScreen

/**
 * A tile layout: turns data into a ProtoLayout element. The ready-made layouts are [ProgressTile],
 * [StatsTile], [ShortcutsTile], [CountdownTile] and [MessageTile]; write your own with the
 * protolayout-material3 functions on [TileScope.material]:
 *
 * ```kotlin
 * val hello = TileLayout { scope ->
 *     with(scope.material) {
 *         primaryLayout(mainSlot = { text("Hello".layoutString) })
 *     }
 * }
 * ```
 */
public fun interface TileLayout {
    /** Builds the root element of the tile. Called on every tile request. */
    public fun render(scope: TileScope): LayoutElementBuilders.LayoutElement
}

/**
 * Colors of the tiles. With [allowDynamicColors] the watch's dynamic theme (Wear OS 6 and newer)
 * wins when the user turned it on; [colorScheme] is used otherwise.
 */
public class TileTheme(
    public val colorScheme: ColorScheme = ColorScheme(),
    public val allowDynamicColors: Boolean = true,
) {
    public companion object {
        /** The Material 3 baseline colors, following the watch's dynamic colors when available. */
        public val Default: TileTheme = TileTheme()
    }
}

/**
 * Everything a [TileLayout] needs while it builds: the Material 3 scope, the screen, the time and
 * helpers for images and clicks. Images registered with [image] are put into the tile resources
 * automatically.
 */
public class TileScope internal constructor(
    /** A context of the app that owns the tile. */
    public val context: Context,
    /** The device the tile is built for. */
    public val deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
    /** The protolayout-material3 scope with the theme's colors, shapes and typography. */
    public val material: MaterialScope,
    /** The time the tile is built at, in epoch milliseconds. */
    public val nowMillis: Long,
    private val images: ImageRegistry,
) {
    /** The screen the tile is built for. */
    public val screen: TileScreen = deviceConfiguration.toTileScreen()

    /** What fits on [screen]: number of stats, shortcut columns, compact numbers and more. */
    public val spec: TileLayoutSpec get() = screen.layout

    /**
     * The ProtoLayout resource id for the drawable [resId] (a vector or bitmap drawable of your app).
     * Use it with `icon(id)` or `avatarImage(id)`; the image is added to the tile resources for you.
     */
    public fun image(@DrawableRes resId: Int): String = images.register(resId)

    /** [action] as a clickable with the element id [id], reported back as `lastClickId`. */
    public fun clickable(action: TileAction, id: String): ModifiersBuilders.Clickable = action.toClickable(context, id)

    /** True when the renderer on the watch supports ProtoLayout schema [major].[minor] (e.g. 1.200). */
    public fun supportsSchema(major: Int, minor: Int): Boolean {
        val version = deviceConfiguration.rendererSchemaVersion
        return version.major > major || (version.major == major && version.minor >= minor)
    }
}

/** The screen described by these device parameters; an unknown size is treated as a small round watch. */
public fun DeviceParametersBuilders.DeviceParameters.toTileScreen(): TileScreen {
    val width = screenWidthDp
    val height = screenHeightDp
    if (width <= 0 || height <= 0) return TileScreen.SmallRound
    return TileScreen(width, height, isRound = screenShape != DeviceParametersBuilders.SCREEN_SHAPE_RECT)
}

/** Collects the drawables a layout uses, keyed by stable ids derived from the resource names. */
internal class ImageRegistry(private val context: Context) {
    private val images = LinkedHashMap<String, Int>()

    val mapping: Map<String, Int> get() = images.toMap()

    fun register(@DrawableRes resId: Int): String {
        val name = runCatching { context.resources.getResourceEntryName(resId) }.getOrNull() ?: resId.toString()
        val id = "image_$name"
        images[id] = resId
        return id
    }
}
