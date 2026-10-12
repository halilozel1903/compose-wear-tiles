package io.github.halilozel1903.weartiles.core

import kotlin.math.min

/** Size classes of watch screens that change how much a tile shows. */
public enum class ScreenClass(public val isRound: Boolean, public val isLarge: Boolean) {
    SmallRound(isRound = true, isLarge = false),
    LargeRound(isRound = true, isLarge = true),
    SmallRect(isRound = false, isLarge = false),
    LargeRect(isRound = false, isLarge = true),
}

/**
 * The watch screen a tile is built for, in dp (from `DeviceParameters` on the watch).
 *
 * Screens whose shorter side is at least [LARGE_SCREEN_MIN_DP] (225 dp, the Wear OS breakpoint)
 * are large: a 454 px round screen at density 2 is 227 dp, a 384 px one is 192 dp.
 */
public data class TileScreen(
    public val widthDp: Int,
    public val heightDp: Int,
    public val isRound: Boolean = true,
) {
    init {
        require(widthDp > 0 && heightDp > 0) { "Screen size must be positive, was ${widthDp}x$heightDp" }
    }

    /** The size class of this screen. */
    public val screenClass: ScreenClass
        get() {
            val large = min(widthDp, heightDp) >= LARGE_SCREEN_MIN_DP
            return when {
                isRound && large -> ScreenClass.LargeRound
                isRound -> ScreenClass.SmallRound
                large -> ScreenClass.LargeRect
                else -> ScreenClass.SmallRect
            }
        }

    /** The layout decisions for this screen. */
    public val layout: TileLayoutSpec get() = TileLayoutSpec.forScreenClass(screenClass)

    public companion object {
        /** The shorter side, in dp, from which a screen counts as large. */
        public const val LARGE_SCREEN_MIN_DP: Int = 225

        /** A small round watch (192 dp, e.g. 384 px at density 2). */
        public val SmallRound: TileScreen = TileScreen(192, 192, isRound = true)

        /** A large round watch (227 dp, e.g. 454 px at density 2). */
        public val LargeRound: TileScreen = TileScreen(227, 227, isRound = true)
    }
}

/**
 * How much a tile shows on a screen class. The ready-made layouts read these values; custom layouts
 * can too.
 *
 * @property maxStats stats shown side by side in a stats tile.
 * @property shortcutColumns shortcuts per row in a shortcuts tile.
 * @property maxShortcuts shortcuts shown at most in a shortcuts tile.
 * @property compactNumbers numbers are shortened (`8.4k` instead of `8,421`).
 * @property showSecondaryText captions, trends and subtitles are shown.
 * @property maxTitleChars characters of a title before it is shortened with an ellipsis.
 */
public data class TileLayoutSpec(
    public val screenClass: ScreenClass,
    public val maxStats: Int,
    public val shortcutColumns: Int,
    public val maxShortcuts: Int,
    public val compactNumbers: Boolean,
    public val showSecondaryText: Boolean,
    public val maxTitleChars: Int,
) {
    /** [title] shortened to [maxTitleChars] characters with a trailing ellipsis. */
    public fun fitTitle(title: String): String = ellipsize(title, maxTitleChars)

    /** [items] cut to [maxShortcuts] and split into balanced rows of at most [shortcutColumns]. */
    public fun <T> shortcutRows(items: List<T>): List<List<T>> =
        chunkBalanced(items.take(maxShortcuts), shortcutColumns)

    public companion object {
        /** The decisions for [screenClass]. */
        public fun forScreenClass(screenClass: ScreenClass): TileLayoutSpec = if (screenClass.isLarge) {
            TileLayoutSpec(
                screenClass = screenClass,
                maxStats = 3,
                shortcutColumns = 3,
                maxShortcuts = 6,
                compactNumbers = false,
                showSecondaryText = true,
                maxTitleChars = 18,
            )
        } else {
            TileLayoutSpec(
                screenClass = screenClass,
                maxStats = 2,
                shortcutColumns = 2,
                maxShortcuts = 4,
                compactNumbers = true,
                showSecondaryText = false,
                maxTitleChars = 14,
            )
        }

        /**
         * Row sizes that spread [count] items over as few rows of at most [maxColumns] as possible,
         * as evenly as possible, longer rows first: 4 items in rows of 3 -> `[2, 2]`, 5 -> `[3, 2]`,
         * 7 -> `[3, 2, 2]`.
         */
        public fun balancedRows(count: Int, maxColumns: Int): List<Int> {
            require(count >= 0) { "count must not be negative, was $count" }
            require(maxColumns > 0) { "maxColumns must be positive, was $maxColumns" }
            if (count == 0) return emptyList()
            val rows = (count + maxColumns - 1) / maxColumns
            val base = count / rows
            val extra = count % rows
            return List(rows) { row -> if (row < extra) base + 1 else base }
        }

        /** [items] split into the rows of [balancedRows]. */
        public fun <T> chunkBalanced(items: List<T>, maxColumns: Int): List<List<T>> {
            var start = 0
            return balancedRows(items.size, maxColumns).map { size ->
                items.subList(start, start + size).toList().also { start += size }
            }
        }

        /** [text] cut to [maxChars] characters, the last one replaced by an ellipsis when cut. */
        public fun ellipsize(text: String, maxChars: Int): String {
            require(maxChars > 0) { "maxChars must be positive, was $maxChars" }
            return if (text.length <= maxChars) text else text.take(maxChars - 1).trimEnd() + "…"
        }
    }
}
