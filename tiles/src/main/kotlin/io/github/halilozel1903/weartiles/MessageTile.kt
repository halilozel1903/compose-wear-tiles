package io.github.halilozel1903.weartiles

import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString

/**
 * A short message with an optional action, for empty, signed out and error states.
 *
 * ```kotlin
 * MessageTile("Steps", "Allow activity access", TileButton("Open", TileAction.launch<MainActivity>()))
 * ```
 */
public class MessageTile(
    public val title: String,
    public val message: String,
    public val button: TileButton? = null,
) : TileLayout {

    override fun render(scope: TileScope): LayoutElement = with(scope.material) {
        primaryLayout(
            titleSlot = titleSlot(scope, title),
            mainSlot = { text(message.layoutString, typography = Typography.BODY_LARGE, maxLines = 3) },
            bottomSlot = bottomSlot(scope, button, caption = null, idPrefix = "message"),
        )
    }
}
