package io.github.halilozel1903.weartiles

import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconEdgeButton
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.layoutString

/** A slot of `primaryLayout`. */
internal typealias Slot = MaterialScope.() -> LayoutElement

/** The title slot: one line of title text, shortened to fit the screen. */
internal fun titleSlot(scope: TileScope, title: String): Slot? {
    if (title.isBlank()) return null
    val fitted = scope.spec.fitTitle(title)
    return { text(fitted.layoutString, typography = Typography.TITLE_SMALL, maxLines = 1) }
}

/** The bottom slot: an edge button for [button], else a line of [caption] text, else nothing. */
internal fun bottomSlot(scope: TileScope, button: TileButton?, caption: String?, idPrefix: String): Slot? {
    if (button != null) {
        val clickable = scope.clickable(button.action, "${idPrefix}_button")
        val iconRes = button.icon
        if (iconRes != null) {
            val iconId = scope.image(iconRes)
            return {
                iconEdgeButton(
                    onClick = clickable,
                    modifier = LayoutModifier.contentDescription(button.label),
                ) {
                    icon(iconId)
                }
            }
        }
        return {
            textEdgeButton(
                onClick = clickable,
                modifier = LayoutModifier.contentDescription(button.label),
            ) {
                text(button.label.layoutString)
            }
        }
    }
    if (!caption.isNullOrBlank()) {
        return { text(caption.layoutString, typography = Typography.BODY_SMALL, maxLines = 2) }
    }
    return null
}

/** A slot with one [text] element; [typography] null keeps the style of the component it is in. */
internal fun textSlot(value: String, typography: Int? = null, maxLines: Int = 1): Slot {
    if (typography == null) return { text(value.layoutString, maxLines = maxLines) }
    return { text(value.layoutString, typography = typography, maxLines = maxLines) }
}
