package io.github.halilozel1903.weartiles.core

import kotlin.math.max

/**
 * Progress towards a goal: 8,421 of 10,000 steps, 5 of 8 glasses of water.
 *
 * @property label what is counted, shown as the tile title (`"Steps"`).
 * @property current the value so far.
 * @property target the goal; must be positive.
 * @property unit the unit after the target (`"steps"`), may be empty.
 */
public data class ProgressGoal(
    public val label: String,
    public val current: Double,
    public val target: Double,
    public val unit: String = "",
) {
    init {
        require(target > 0) { "target must be positive, was $target" }
        require(!current.isNaN()) { "current must be a number" }
    }

    /** Progress from 0 to 1 (1 once the goal is reached). */
    public val fraction: Float get() = (current / target).coerceIn(0.0, 1.0).toFloat()

    /** True when [current] reached [target]. */
    public val isReached: Boolean get() = current >= target

    /** What is left to reach the goal, 0 once it is reached. */
    public val remaining: Double get() = max(0.0, target - current)

    /** Progress as a percentage, which may exceed 100%: `"84%"`. */
    public val percentText: String get() = TileFormat.percent(max(0.0, current / target))

    /** [current] as text: `"8,421"`, or `"8.4k"` when [compact]. */
    public fun currentText(compact: Boolean = false): String = TileFormat.number(current, compact)

    /** The target with its unit: `"of 10k steps"`. */
    public fun targetText(): String = listOf("of", TileFormat.compact(target), unit).filter { it.isNotEmpty() }.joinToString(" ")
}

/** The direction a stat moved in. */
public enum class Trend(public val symbol: String) {
    Up("↑"),
    Down("↓"),
    Flat("→"),
    None(""),
}

/**
 * One value of a stats tile: `"12.4k"` `"Steps"` `↑`.
 *
 * @property label what the value is (`"Steps"`, `"Wind"`).
 * @property value the formatted value (`"12.4k"`, `"18°"`); see [number] to format numbers.
 * @property unit an optional unit shown after the value (`"km/h"`).
 * @property trend an optional direction shown next to the label.
 */
public data class StatItem(
    public val label: String,
    public val value: String,
    public val unit: String = "",
    public val trend: Trend = Trend.None,
) {
    /** The label with the trend symbol: `"Steps ↑"`. */
    public val labelWithTrend: String get() = if (trend == Trend.None) label else "$label ${trend.symbol}"

    public companion object {
        /** A stat whose number is formatted with [TileFormat.compact]: `12_400` -> `"12.4k"`. */
        public fun number(label: String, value: Double, unit: String = "", trend: Trend = Trend.None): StatItem =
            StatItem(label, TileFormat.compact(value), unit, trend)
    }
}

/**
 * A titled list of stats. A tile shows the first [TileLayoutSpec.maxStats] of them.
 */
public data class StatList(
    public val title: String,
    public val items: List<StatItem>,
    public val updatedAtMillis: Long? = null,
) {
    /** The stats that fit on a screen with [spec]. */
    public fun visibleItems(spec: TileLayoutSpec): List<StatItem> = items.take(spec.maxStats)

    /** `"Updated 5 min ago"`, or null when [updatedAtMillis] is unknown. */
    public fun updatedText(nowMillis: Long): String? =
        updatedAtMillis?.let { "Updated " + TileFormat.age(it, nowMillis).replaceFirstChar { c -> c.lowercaseChar() } }
}

/**
 * Items of a shortcuts tile, laid out in balanced rows. Generic so the Android module can put icons
 * and click actions in it while this module stays pure Kotlin.
 */
public data class ShortcutGrid<T>(
    public val title: String,
    public val items: List<T>,
) {
    /** The items that fit on a screen with [spec], in rows. */
    public fun rows(spec: TileLayoutSpec): List<List<T>> = spec.shortcutRows(items)

    /** How many items are left out on a screen with [spec]. */
    public fun hiddenCount(spec: TileLayoutSpec): Int = max(0, items.size - spec.maxShortcuts)
}

/**
 * A countdown to a moment: a race start, rain, sunset.
 *
 * @property title what is counted down to (`"Rain in"`).
 * @property targetMillis the moment, in epoch milliseconds.
 * @property startMillis when the countdown started, used for the progress ring; null for no progress.
 * @property doneLabel the text once the moment passed.
 * @property subtitle an optional line under the time (`"Light rain at 14:30"`).
 */
public data class Countdown(
    public val title: String,
    public val targetMillis: Long,
    public val startMillis: Long? = null,
    public val doneLabel: String = "Now",
    public val subtitle: String = "",
) {
    /** Milliseconds left at [nowMillis], never negative. */
    public fun remainingMillis(nowMillis: Long): Long = max(0L, targetMillis - nowMillis)

    /** True once [targetMillis] is reached. */
    public fun isOver(nowMillis: Long): Boolean = nowMillis >= targetMillis

    /** Elapsed share of the countdown from [startMillis] to [targetMillis], 0 to 1. */
    public fun progress(nowMillis: Long): Float {
        val start = startMillis ?: return if (isOver(nowMillis)) 1f else 0f
        if (start >= targetMillis) return if (isOver(nowMillis)) 1f else 0f
        return ((nowMillis - start).toDouble() / (targetMillis - start)).coerceIn(0.0, 1.0).toFloat()
    }

    /** The remaining time as text: `"1h 25m"`, `"<1m"`, [doneLabel]. See [TileFormat.countdown]. */
    public fun remainingText(nowMillis: Long): String = TileFormat.countdown(targetMillis - nowMillis, doneLabel)

    /**
     * When [remainingText] shows something else next, or null once the countdown is over. The text
     * counts down hours above a day and minutes below, and switches to `"<1m"` and then [doneLabel].
     */
    public fun nextChangeMillis(nowMillis: Long): Long? {
        val remaining = targetMillis - nowMillis
        if (remaining <= 0) return null
        if (remaining < MINUTE) return targetMillis
        val step = if (remaining >= DAY) HOUR else MINUTE
        return nowMillis + remaining % step + 1
    }

    /**
     * How a tile showing this countdown should refresh. A [live] tile ticks on the watch by itself
     * and only needs new data when the moment passed; a static one refreshes whenever its text changes.
     */
    public fun refreshPolicy(nowMillis: Long, live: Boolean): RefreshPolicy {
        if (isOver(nowMillis)) return RefreshPolicy.Never
        if (live) return RefreshPolicy.At(targetMillis)
        return nextChangeMillis(nowMillis)?.let { RefreshPolicy.At(it) } ?: RefreshPolicy.Never
    }

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 3_600_000L
        const val DAY = 86_400_000L
    }
}
