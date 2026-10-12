package io.github.halilozel1903.weartiles.core

import kotlin.math.max

/**
 * When a tile should ask for new data on its own.
 *
 * The tiles module turns a policy into `Tile.freshnessIntervalMillis` with [Freshness.intervalMillis].
 * For data that changes because something happened (a workout was logged), request an update
 * instead of polling.
 */
public sealed interface RefreshPolicy {

    /** No automatic refresh: the tile only updates when it is requested or opened again. */
    public data object Never : RefreshPolicy

    /** Refresh every [intervalMillis] after each update. */
    public data class Every(public val intervalMillis: Long) : RefreshPolicy {
        init {
            require(intervalMillis > 0) { "intervalMillis must be positive, was $intervalMillis" }
        }
    }

    /**
     * Refresh on the next multiple of [periodMillis] since the epoch, shifted by [offsetMillis]: with an
     * hour period the tile refreshes at the top of every hour, however late the last update was.
     */
    public data class Aligned(public val periodMillis: Long, public val offsetMillis: Long = 0) : RefreshPolicy {
        init {
            require(periodMillis > 0) { "periodMillis must be positive, was $periodMillis" }
        }
    }

    /** Refresh once at [epochMillis] (for example when a countdown ends). Nothing happens after it. */
    public data class At(public val epochMillis: Long) : RefreshPolicy

    /** Refresh at the earliest moment any of [policies] asks for. */
    public data class Earliest(public val policies: List<RefreshPolicy>) : RefreshPolicy

    public companion object {
        /** Every [minutes] minutes. */
        public fun everyMinutes(minutes: Int): RefreshPolicy = Every(minutes * MINUTE)

        /** Every [hours] hours. */
        public fun everyHours(hours: Int): RefreshPolicy = Every(hours * HOUR)

        /** At the top of every hour. */
        public fun hourly(): RefreshPolicy = Aligned(HOUR)

        /** The earliest of [policies]. */
        public fun earliestOf(vararg policies: RefreshPolicy): RefreshPolicy = Earliest(policies.toList())

        internal const val MINUTE: Long = 60_000L
        internal const val HOUR: Long = 3_600_000L
    }
}

/** Freshness rules for tiles. */
public object Freshness {

    /**
     * The shortest automatic refresh interval worth asking for. Wear OS throttles tiles that ask for
     * more, so shorter intervals are raised to this.
     */
    public const val MIN_INTERVAL_MILLIS: Long = 60_000L

    /** The value of `Tile.freshnessIntervalMillis` that turns automatic refreshes off. */
    public const val NO_REFRESH: Long = 0L

    /**
     * The freshness interval for a tile built at [nowMillis]: milliseconds until the next refresh, at
     * least [MIN_INTERVAL_MILLIS], or [NO_REFRESH].
     */
    public fun intervalMillis(policy: RefreshPolicy, nowMillis: Long): Long = when (policy) {
        RefreshPolicy.Never -> NO_REFRESH
        is RefreshPolicy.Every -> clamp(policy.intervalMillis)
        is RefreshPolicy.Aligned -> {
            val sinceOffset = nowMillis - policy.offsetMillis
            val next = (Math.floorDiv(sinceOffset, policy.periodMillis) + 1) * policy.periodMillis + policy.offsetMillis
            clamp(next - nowMillis)
        }
        is RefreshPolicy.At -> if (policy.epochMillis <= nowMillis) NO_REFRESH else clamp(policy.epochMillis - nowMillis)
        is RefreshPolicy.Earliest -> policy.policies
            .map { intervalMillis(it, nowMillis) }
            .filter { it > NO_REFRESH }
            .minOrNull() ?: NO_REFRESH
    }

    /** When the tile built at [nowMillis] refreshes next, or null when it does not refresh on its own. */
    public fun nextRefreshAtMillis(policy: RefreshPolicy, nowMillis: Long): Long? =
        intervalMillis(policy, nowMillis).takeIf { it > NO_REFRESH }?.let { nowMillis + it }

    /** True when data updated at [updatedAtMillis] is older than [maxAgeMillis] at [nowMillis]. */
    public fun isStale(updatedAtMillis: Long, nowMillis: Long, maxAgeMillis: Long): Boolean =
        nowMillis - updatedAtMillis > maxAgeMillis

    private fun clamp(millis: Long): Long = max(MIN_INTERVAL_MILLIS, millis)
}
