package io.github.halilozel1903.weartiles

import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.types.layoutString
import io.github.halilozel1903.weartiles.core.StatItem
import io.github.halilozel1903.weartiles.core.StatList

/**
 * A list of stats: the list title, then up to 2 (small screens) or 3 (large screens) full-width
 * rows, one per stat, with the label and its trend on the left and the value on the right, followed
 * by its unit on large screens. The bottom shows the edge [button], or how old the data is when
 * [StatList.updatedAtMillis] is set.
 *
 * Each stat gets the full width of the screen, so labels like `Wind ↑` and values like `12 km/h`
 * fit without being shortened.
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
 * @param onClick what tapping a row does; refreshes the tile when null. The row ids are
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
                val column = LayoutElementBuilders.Column.Builder()
                    .setWidth(expand())
                    .setHeight(expand())
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                items.forEachIndexed { index, item ->
                    if (index > 0) column.addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
                    val unit = item.unit.takeIf { spec.showSecondaryText && it.isNotBlank() }
                    column.addContent(statRow(scope, index, item, unit))
                }
                column.build()
            },
            bottomSlot = bottomSlot(scope, button, caption = stats.updatedText(scope.nowMillis), idPrefix = "stats"),
        )
    }

    /** One stat as a full-width card: `Wind ↑ .......... 12 km/h`. The first one is highlighted. */
    private fun MaterialScope.statRow(scope: TileScope, index: Int, item: StatItem, unit: String?): LayoutElement {
        val highlighted = index == 0
        val background: LayoutColor = if (highlighted) colorScheme.primaryContainer else colorScheme.surfaceContainer
        val valueColor: LayoutColor = if (highlighted) colorScheme.onPrimaryContainer else colorScheme.onSurface
        val labelColor: LayoutColor = if (highlighted) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
        val description = listOf(item.label, item.value, item.unit).filter { it.isNotBlank() }.joinToString(" ")

        val label = LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_START)
            .addContent(
                text(
                    item.labelWithTrend.layoutString,
                    typography = Typography.LABEL_MEDIUM,
                    color = labelColor,
                    maxLines = 1,
                ),
            )
            .build()
        val value = LayoutElementBuilders.Row.Builder()
            .setWidth(wrap())
            .setHeight(wrap())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .addContent(text(item.value.layoutString, typography = Typography.TITLE_MEDIUM, color = valueColor, maxLines = 1))
        if (unit != null) {
            value.addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(UNIT_GAP_DP)).build())
            value.addContent(text(unit.layoutString, typography = Typography.BODY_SMALL, color = labelColor, maxLines = 1))
        }
        val row = LayoutElementBuilders.Row.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .addContent(label)
            .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(UNIT_GAP_DP)).build())
            .addContent(value.build())
            .build()

        return card(
            onClick = scope.clickable(onClick ?: TileAction.Refresh, "stat_$index"),
            modifier = LayoutModifier.contentDescription(description).background(background),
            width = expand(),
            height = expand(),
            contentPadding = ModifiersBuilders.Padding.Builder()
                .setStart(dp(ROW_PADDING_DP))
                .setEnd(dp(ROW_PADDING_DP))
                .build(),
        ) {
            LayoutElementBuilders.Box.Builder()
                .setWidth(expand())
                .setHeight(expand())
                .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                .addContent(row)
                .build()
        }
    }

    private companion object {
        /** Space between the start and end of a row and its text. */
        const val ROW_PADDING_DP = 14f

        /** Space between the label and the value, and between the value and its unit. */
        const val UNIT_GAP_DP = 4f
    }
}
