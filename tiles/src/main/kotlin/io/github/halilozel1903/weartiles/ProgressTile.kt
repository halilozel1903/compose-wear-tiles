package io.github.halilozel1903.weartiles

import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.CardDefaults.filledTonalCardColors
import androidx.wear.protolayout.material3.GraphicDataCardDefaults.constructGraphic
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.circularProgressIndicator
import androidx.wear.protolayout.material3.graphicDataCard
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.layoutString
import io.github.halilozel1903.weartiles.core.ProgressGoal

/**
 * Progress towards a goal: the label as title, a card with a progress ring (with an optional [icon]
 * in it), the current value and the target, and an optional edge [button].
 *
 * ```kotlin
 * ProgressTile(
 *     goal = ProgressGoal("Steps", current = 8_421.0, target = 10_000.0, unit = "steps"),
 *     icon = R.drawable.ic_steps,
 *     button = TileButton("Walk", TileAction.launch<WorkoutActivity>("type" to "walk")),
 * )
 * ```
 *
 * Small screens show compact numbers (`8.4k`), large ones the exact value (`8,421`).
 *
 * @param onClick what tapping the card does; refreshes the tile when null.
 */
public class ProgressTile(
    public val goal: ProgressGoal,
    @param:DrawableRes public val icon: Int? = null,
    public val button: TileButton? = null,
    public val onClick: TileAction? = null,
) : TileLayout {

    override fun render(scope: TileScope): LayoutElement = with(scope.material) {
        val spec = scope.spec
        val value = goal.currentText(compact = spec.compactNumbers)
        val target = goal.targetText()
        val cardClick = scope.clickable(onClick ?: TileAction.Refresh, "progress_card")
        val iconId = icon?.let { scope.image(it) }
        val description = "${goal.label}: $value $target, ${goal.percentText}"
        primaryLayout(
            titleSlot = titleSlot(scope, goal.label),
            mainSlot = {
                graphicDataCard(
                    onClick = cardClick,
                    modifier = LayoutModifier.contentDescription(description),
                    height = expand(),
                    colors = filledTonalCardColors(),
                    title = { text(value.layoutString, typography = Typography.NUMERAL_SMALL) },
                    content = { text(target.layoutString, maxLines = 2) },
                    graphic = {
                        if (iconId != null) {
                            constructGraphic(
                                mainContent = { circularProgressIndicator(staticProgress = goal.fraction) },
                                iconContent = { icon(iconId) },
                            )
                        } else {
                            circularProgressIndicator(staticProgress = goal.fraction)
                        }
                    },
                )
            },
            bottomSlot = bottomSlot(scope, button, caption = goal.percentText, idPrefix = "progress"),
        )
    }
}
