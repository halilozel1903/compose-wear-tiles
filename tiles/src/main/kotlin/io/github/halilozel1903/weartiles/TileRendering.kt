package io.github.halilozel1903.weartiles

import android.content.Context
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.expression.VersionBuilders
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.tiles.TileBuilders
import io.github.halilozel1903.weartiles.core.Freshness
import io.github.halilozel1903.weartiles.core.ResourceVersion
import kotlin.math.roundToInt

/**
 * A [TileLayout] built for one device: the root element, its resources and their version.
 * [SimpleTileService] sends it to the watch; you can also inflate [layout] and [resources] yourself
 * with `TileRenderer` (as the sample's tile gallery does) or use them in tests.
 */
public class RenderedTile internal constructor(
    /** The root element of the tile. */
    public val root: LayoutElementBuilders.LayoutElement,
    /** The version of [resources]; changes only when the images change. */
    public val resourcesVersion: String,
    /** The images the layout registered with [TileScope.image]: ProtoLayout id to drawable resource id. */
    public val images: Map<String, Int>,
) {
    /** [root] as a complete layout. */
    public val layout: LayoutElementBuilders.Layout get() = LayoutElementBuilders.Layout.fromLayoutElement(root)

    /** The resources of the layout. */
    public val resources: ResourceBuilders.Resources get() = TileRendering.resources(resourcesVersion, images)

    /** A tile with a single timeline entry that refreshes after [freshnessIntervalMillis] (0: never). */
    public fun toTile(freshnessIntervalMillis: Long = Freshness.NO_REFRESH): TileBuilders.Tile =
        TileBuilders.Tile.Builder()
            .setResourcesVersion(resourcesVersion)
            .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(root))
            .setFreshnessIntervalMillis(freshnessIntervalMillis)
            .build()
}

/** Builds [TileLayout]s outside of a tile service: for previews, galleries and tests. */
public object TileRendering {

    /** Builds [layout] for [deviceConfiguration] with [theme] at [nowMillis]. */
    public fun render(
        context: Context,
        deviceConfiguration: DeviceParametersBuilders.DeviceParameters,
        layout: TileLayout,
        theme: TileTheme = TileTheme.Default,
        nowMillis: Long = System.currentTimeMillis(),
    ): RenderedTile {
        val images = ImageRegistry(context)
        val root = materialScope(
            context = context,
            deviceConfiguration = deviceConfiguration,
            allowDynamicTheme = theme.allowDynamicColors,
            defaultColorScheme = theme.colorScheme,
        ) {
            layout.render(TileScope(context, deviceConfiguration, this, nowMillis, images))
        }
        val mapping = images.mapping
        return RenderedTile(root, ResourceVersion.of(mapping), mapping)
    }

    /** Resources with [version] that map each id of [images] to its drawable resource. */
    public fun resources(version: String, images: Map<String, Int>): ResourceBuilders.Resources {
        val builder = ResourceBuilders.Resources.Builder().setVersion(version)
        images.forEach { (id, resId) ->
            val image = ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(
                    ResourceBuilders.AndroidImageResourceByResId.Builder().setResourceId(resId).build(),
                )
                .build()
            builder.addIdToImageMapping(id, image)
        }
        return builder.build()
    }

    /**
     * Device parameters for the screen of [context], like the ones the watch sends with a tile request.
     * [rendererSchemaVersion] is the ProtoLayout schema the renderer supports (1.400 for the
     * tiles-renderer 1.5 that ships with this library).
     */
    public fun deviceParameters(
        context: Context,
        rendererSchemaVersion: VersionBuilders.VersionInfo = VersionBuilders.VersionInfo.Builder().setMajor(1).setMinor(400).build(),
    ): DeviceParametersBuilders.DeviceParameters {
        val metrics = context.resources.displayMetrics
        val configuration = context.resources.configuration
        return DeviceParametersBuilders.DeviceParameters.Builder()
            .setScreenWidthDp((metrics.widthPixels / metrics.density).roundToInt())
            .setScreenHeightDp((metrics.heightPixels / metrics.density).roundToInt())
            .setScreenDensity(metrics.density)
            .setScreenShape(
                if (configuration.isScreenRound) DeviceParametersBuilders.SCREEN_SHAPE_ROUND else DeviceParametersBuilders.SCREEN_SHAPE_RECT,
            )
            .setDevicePlatform(DeviceParametersBuilders.DEVICE_PLATFORM_WEAR_OS)
            .setFontScale(configuration.fontScale.takeIf { it > 0f } ?: 1f)
            .setRendererSchemaVersion(rendererSchemaVersion)
            .build()
    }
}
