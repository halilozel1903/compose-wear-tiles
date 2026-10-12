package io.github.halilozel1903.weartiles.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Short, locale independent formatting for the little space a tile has.
 *
 * Every function is pure: the same input always gives the same text, so tile layouts, previews and
 * screenshots stay stable.
 */
public object TileFormat {

    private val units = listOf("" to 1.0, "k" to 1e3, "M" to 1e6, "B" to 1e9, "T" to 1e12)

    private val timeOfDay = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Compact numbers: `950` -> `"950"`, `1_234` -> `"1.2k"`, `12_400` -> `"12.4k"`, `123_456` -> `"123k"`,
     * `999_960` -> `"1M"`, `1_250_000` -> `"1.3M"`, `72.5` -> `"72.5"`.
     *
     * Values under 100 of a unit keep one decimal (dropped when it is zero), larger ones none, and a
     * value that rounds up to 1000 moves to the next unit.
     */
    public fun compact(value: Double): String {
        require(!value.isNaN()) { "Cannot format NaN" }
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        val sign = if (value < 0) "-" else ""
        val absolute = abs(value)
        for ((index, unit) in units.withIndex()) {
            val (suffix, size) = unit
            val scaled = absolute / size
            val decimals = if (scaled >= 100) 0 else 1
            val rounded = roundTo(scaled, decimals)
            if (rounded < 1000 || index == units.lastIndex) {
                if (rounded == 0.0) return "0"
                return sign + plain(rounded, decimals) + suffix
            }
        }
        error("unreachable")
    }

    /** [compact] for whole numbers. */
    public fun compact(value: Long): String = compact(value.toDouble())

    /** [compact] for whole numbers. */
    public fun compact(value: Int): String = compact(value.toDouble())

    /** Digits grouped by thousands with a comma: `8421` -> `"8,421"`, `-1234567` -> `"-1,234,567"`. */
    public fun grouped(value: Long): String {
        val digits = abs(value).toString()
        val builder = StringBuilder()
        digits.forEachIndexed { index, char ->
            if (index > 0 && (digits.length - index) % 3 == 0) builder.append(',')
            builder.append(char)
        }
        return (if (value < 0) "-" else "") + builder
    }

    /**
     * A number the way a tile shows it: grouped when it is a whole number below 100,000 and
     * [compact] is false (`"8,421"`), compact otherwise (`"8.4k"`, `"124k"`, `"3.5"`).
     */
    public fun number(value: Double, compact: Boolean = false): String {
        val whole = value == value.roundToLong().toDouble()
        return if (!compact && whole && abs(value) < 100_000) grouped(value.roundToLong()) else compact(value)
    }

    /** A fraction as a whole percentage: `0.842` -> `"84%"`. */
    public fun percent(fraction: Double): String = "${(fraction * 100).roundToInt()}%"

    /**
     * A duration in the two largest units: `"45s"`, `"12m"`, `"1h 05m"`, `"2d 4h"`.
     * Negative durations format as `"0s"`.
     */
    public fun duration(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0) / 1000
        val days = totalSeconds / 86_400
        val hours = totalSeconds % 86_400 / 3600
        val minutes = totalSeconds % 3600 / 60
        val seconds = totalSeconds % 60
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${two(minutes)}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }

    /**
     * Remaining time of a countdown, rounded down to the minute: `"2d 4h"`, `"1h 05m"`, `"25m"`,
     * `"<1m"` in the last minute and [doneLabel] once the time is up.
     *
     * The tiles module renders the same text live on the watch with ProtoLayout dynamic
     * expressions, so the static and the live text always agree.
     */
    public fun countdown(remainingMillis: Long, doneLabel: String = "Now"): String = when {
        remainingMillis <= 0 -> doneLabel
        remainingMillis < 60_000 -> "<1m"
        else -> duration(remainingMillis)
    }

    /** How old some data is: `"Just now"`, `"5 min ago"`, `"2 h ago"`, `"3 d ago"`. */
    public fun age(updatedAtMillis: Long, nowMillis: Long): String {
        val minutes = (nowMillis - updatedAtMillis).coerceAtLeast(0) / 60_000
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            minutes < 24 * 60 -> "${minutes / 60} h ago"
            else -> "${minutes / (24 * 60)} d ago"
        }
    }

    /** A 24 hour clock time in [zone]: `"14:30"`. */
    public fun timeOfDay(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        timeOfDay.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    private fun two(value: Long): String = value.toString().padStart(2, '0')

    private fun roundTo(value: Double, decimals: Int): Double =
        if (decimals == 0) value.roundToLong().toDouble() else (value * 10).roundToLong() / 10.0

    private fun plain(value: Double, decimals: Int): String {
        if (decimals == 0) return value.roundToLong().toString()
        val tenths = (value * 10).roundToLong()
        return if (tenths % 10 == 0L) (tenths / 10).toString() else "${tenths / 10}.${tenths % 10}"
    }
}
