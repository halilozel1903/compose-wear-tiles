package io.github.halilozel1903.weartiles

import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInt32
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import androidx.wear.protolayout.material3.ProgressIndicatorColors
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.circularProgressIndicator
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.LayoutString
import androidx.wear.protolayout.types.asLayoutConstraint
import androidx.wear.protolayout.types.layoutString
import io.github.halilozel1903.weartiles.core.Countdown
import java.time.Instant

/**
 * A countdown to a moment: the title, a ring that fills up as the moment comes closer (when
 * [Countdown.startMillis] is set) around the remaining time, and the subtitle or an edge [button]
 * at the bottom.
 *
 * With [live] (the default) the remaining time counts down on the watch by itself, with ProtoLayout
 * dynamic time expressions (`"1h 25m"`, `"25m"`, `"<1m"`, then [Countdown.doneLabel]); renderers older
 * than schema 1.2 get the static text. Use [Countdown.refreshPolicy] for the matching refresh.
 *
 * ```kotlin
 * CountdownTile(Countdown("Rain in", targetMillis = rainAt, startMillis = forecastAt, subtitle = "Light rain at 14:30"))
 * ```
 *
 * @param onClick what tapping the tile does; nothing when null.
 */
public class CountdownTile(
    public val countdown: Countdown,
    public val live: Boolean = true,
    public val button: TileButton? = null,
    public val onClick: TileAction? = null,
) : TileLayout {

    override fun render(scope: TileScope): LayoutElement = with(scope.material) {
        val now = scope.nowMillis
        val staticText = countdown.remainingText(now)
        val remaining = if (live && !countdown.isOver(now) && scope.supportsSchema(1, 200)) {
            LayoutString(
                staticText,
                liveCountdownText(countdown.targetMillis, countdown.doneLabel),
                LONGEST_TEXT.asLayoutConstraint(),
            )
        } else {
            staticText.layoutString
        }
        val ringColors = ProgressIndicatorColors(
            indicatorColor = colorScheme.tertiary,
            trackColor = colorScheme.surfaceContainer,
        )
        val tileClick = onClick?.let { scope.clickable(it, "countdown_tile") }
        primaryLayout(
            titleSlot = titleSlot(scope, countdown.title),
            mainSlot = {
                var modifier = LayoutModifier.contentDescription("${countdown.title} $staticText")
                if (tileClick != null) modifier = modifier.clickable(tileClick)
                LayoutElementBuilders.Box.Builder()
                    .setWidth(expand())
                    .setHeight(expand())
                    .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                    .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                    .addContent(
                        circularProgressIndicator(
                            staticProgress = countdown.progress(now),
                            colors = ringColors,
                            size = expand(),
                        ),
                    )
                    .addContent(
                        text(
                            remaining,
                            modifier = modifier,
                            typography = Typography.NUMERAL_EXTRA_SMALL,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                        ),
                    )
                    .build()
            },
            bottomSlot = bottomSlot(scope, button, caption = countdown.subtitle, idPrefix = "countdown"),
            onClick = tileClick,
        )
    }

    private companion object {
        /** The widest text the live countdown shows, to reserve its space. */
        const val LONGEST_TEXT = "00h 00m"
    }
}

/**
 * The text of [io.github.halilozel1903.weartiles.core.TileFormat.countdown] as a dynamic string the
 * watch updates every second: `"2d 4h"`, `"1h 05m"`, `"25m"`, `"<1m"`, then [doneLabel].
 */
internal fun liveCountdownText(targetMillis: Long, doneLabel: String): DynamicString {
    val target = DynamicInstant.withSecondsPrecision(Instant.ofEpochMilli(targetMillis))
    val left = DynamicInstant.platformTimeWithSecondsPrecision().durationUntil(target)
    val seconds = left.toIntSeconds()
    val twoDigits = DynamicInt32.IntFormatter.Builder().setMinIntegerDigits(2).build()
    val days = left.toIntDays().format()
        .concat(DynamicString.constant("d "))
        .concat(left.hoursPart.format())
        .concat(DynamicString.constant("h"))
    val hours = left.toIntHours().format()
        .concat(DynamicString.constant("h "))
        .concat(left.minutesPart.format(twoDigits))
        .concat(DynamicString.constant("m"))
    val minutes = left.toIntMinutes().format().concat(DynamicString.constant("m"))
    val underADay = DynamicString.onCondition(seconds.gte(3_600)).use(hours).elseUse(minutes)
    val counting = DynamicString.onCondition(seconds.gte(86_400)).use(days).elseUse(underADay)
    val lastMinute = DynamicString.onCondition(seconds.lt(60)).use("<1m").elseUse(counting)
    return DynamicString.onCondition(seconds.lte(0)).use(doneLabel).elseUse(lastMinute)
}
