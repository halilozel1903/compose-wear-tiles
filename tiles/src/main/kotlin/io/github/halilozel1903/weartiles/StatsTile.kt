package io.github.halilozel1903.weartiles

import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.CardDefaults.filledTonalCardColors
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textDataCard
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.layoutString
import io.github.halilozel1903.weartiles.core.StatList

/**
 * A row of stats: the list title, up to 2 (small screens) or 3 (large screens) data cards with the
 * value, the label and its trend, and the units on large screens. The bottom shows the edge [button],
 * or how old the data is when [StatList.updatedAtMillis] is set.
 *
 * ```kotlin
 * StatsTile(
 *     StatList(
 *         title = "Weather",
 *         items = listOf(StatItem("Temp", "18°"), StatItem("Wind", "12", "km/h", Trend.Up), StatItem("UV", "4")),
 *         updatedAtMillis = weather.updatedAt,
 *     ),
 * )
 * ```
 *
 * @param onClick what tapping a card does; refreshes the tile when null. The card ids are
 *   `stat_0`, `stat_1`, ... and arrive as [TileStateRequest.lastClickId].
 */
public class StatsTile(
    public val stats: StatList,
    public val button: TileButton? = null,
    public val onClick: TileAction? = null,
) : TileLayout {

    override fun render(scope: TileScope): LayoutElement = with(scope.material) {
        val spec = scope.spec
        val items = stats.visibleItems(spec)
        primaryLayout(
            titleSlot = titleSlot(scope, stats.title),
            mainSlot = {
                buttonGroup {
                    items.forEachIndexed { index, item ->
                        val click = scope.clickable(onClick ?: TileAction.Refresh, "stat_$index")
                        val unit = item.unit.takeIf { spec.showSecondaryText && it.isNotBlank() }
                        val description = listOf(item.label, item.value, item.unit).filter { it.isNotBlank() }.joinToString(" ")
                        buttonGroupItem {
                            textDataCard(
                                onClick = click,
                                modifier = LayoutModifier.contentDescription(description),
                                width = expand(),
                                height = expand(),
                                colors = if (index == 0) filledVariantCardColors() else filledTonalCardColors(),
                                title = { text(item.value.layoutString, maxLines = 1) },
                                content = { text(item.labelWithTrend.layoutString, maxLines = 1) },
                                secondaryText = unit?.let { textSlot(it) },
                            )
                        }
                    }
                }
            },
            bottomSlot = bottomSlot(scope, button, caption = stats.updatedText(scope.nowMillis), idPrefix = "stats"),
        )
    }
}
