package io.github.halilozel1903.weartiles

import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.ButtonDefaults.filledButtonColors
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.ButtonGroupDefaults
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.contentDescription
import io.github.halilozel1903.weartiles.core.ShortcutGrid

/**
 * One shortcut of a [ShortcutsTile].
 *
 * @property label the content description (icon buttons have no text on a tile).
 * @property icon the drawable shown in the button.
 * @property action what tapping it does.
 * @property id the clickable id, reported as [TileStateRequest.lastClickId] for [TileAction.Refresh].
 */
public class TileShortcut(
    public val label: String,
    @param:DrawableRes public val icon: Int,
    public val action: TileAction,
    public val id: String = "shortcut_" + label.lowercase().replace(Regex("[^a-z0-9]+"), "_"),
)

/**
 * A grid of icon buttons that start things: up to 4 shortcuts in rows of 2 on small screens, up to 6
 * in rows of 3 on large ones, spread over balanced rows (4 shortcuts on a large screen are 2 + 2, not
 * 3 + 1). The first shortcut is highlighted.
 *
 * ```kotlin
 * ShortcutsTile(
 *     title = "Quick start",
 *     shortcuts = listOf(
 *         TileShortcut("Run", R.drawable.ic_run, TileAction.launch<WorkoutActivity>("type" to "run")),
 *         TileShortcut("Ride", R.drawable.ic_ride, TileAction.launch<WorkoutActivity>("type" to "ride")),
 *     ),
 * )
 * ```
 */
public class ShortcutsTile(
    public val title: String,
    public val shortcuts: List<TileShortcut>,
    public val button: TileButton? = null,
) : TileLayout {

    override fun render(scope: TileScope): LayoutElement = with(scope.material) {
        val rows = ShortcutGrid(title, shortcuts).rows(scope.spec)
        primaryLayout(
            titleSlot = titleSlot(scope, title),
            mainSlot = {
                val column = LayoutElementBuilders.Column.Builder()
                    .setWidth(expand())
                    .setHeight(expand())
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                var first = true
                rows.forEachIndexed { rowIndex, row ->
                    if (rowIndex > 0) column.addContent(ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
                    column.addContent(
                        buttonGroup(width = expand(), height = expand()) {
                            row.forEach { shortcut ->
                                val highlighted = first
                                first = false
                                val iconId = scope.image(shortcut.icon)
                                val click = scope.clickable(shortcut.action, shortcut.id)
                                buttonGroupItem {
                                    iconButton(
                                        onClick = click,
                                        modifier = LayoutModifier.contentDescription(shortcut.label),
                                        width = expand(),
                                        height = expand(),
                                        colors = if (highlighted) filledButtonColors() else filledTonalButtonColors(),
                                        iconContent = { icon(iconId) },
                                    )
                                }
                            }
                        },
                    )
                }
                column.build()
            },
            bottomSlot = bottomSlot(scope, button, caption = null, idPrefix = "shortcuts"),
        )
    }
}
