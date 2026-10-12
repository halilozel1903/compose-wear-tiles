package io.github.halilozel1903.weartiles.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreshnessTest {

    private val hour = 3_600_000L
    private val minute = 60_000L

    @Test
    fun neverMeansNoRefresh() {
        assertEquals(Freshness.NO_REFRESH, Freshness.intervalMillis(RefreshPolicy.Never, 0))
        assertNull(Freshness.nextRefreshAtMillis(RefreshPolicy.Never, 0))
    }

    @Test
    fun everyIsRaisedToTheMinimum() {
        assertEquals(30 * minute, Freshness.intervalMillis(RefreshPolicy.everyMinutes(30), 0))
        assertEquals(Freshness.MIN_INTERVAL_MILLIS, Freshness.intervalMillis(RefreshPolicy.Every(5_000), 0))
        assertEquals(2 * hour, Freshness.intervalMillis(RefreshPolicy.everyHours(2), 0))
        assertFailsWith<IllegalArgumentException> { RefreshPolicy.Every(0) }
    }

    @Test
    fun alignedRefreshesAtTheNextBoundary() {
        val now = 10 * hour + 20 * minute
        assertEquals(40 * minute, Freshness.intervalMillis(RefreshPolicy.hourly(), now))
        // Exactly on a boundary: the next one.
        assertEquals(hour, Freshness.intervalMillis(RefreshPolicy.hourly(), 10 * hour))
        // Offset by 15 minutes: refresh at 10:15, 11:15, ...
        assertEquals(55 * minute, Freshness.intervalMillis(RefreshPolicy.Aligned(hour, 15 * minute), now))
        // Less than a minute before the boundary: raised to the minimum.
        assertEquals(minute, Freshness.intervalMillis(RefreshPolicy.hourly(), 11 * hour - 10_000))
    }

    @Test
    fun atRefreshesOnceAndThenStops() {
        assertEquals(5 * minute, Freshness.intervalMillis(RefreshPolicy.At(15 * minute), 10 * minute))
        assertEquals(Freshness.NO_REFRESH, Freshness.intervalMillis(RefreshPolicy.At(10 * minute), 10 * minute))
        assertEquals(15 * minute, Freshness.nextRefreshAtMillis(RefreshPolicy.At(15 * minute), 10 * minute))
    }

    @Test
    fun earliestPicksTheSoonestRefresh() {
        val policy = RefreshPolicy.earliestOf(RefreshPolicy.everyMinutes(30), RefreshPolicy.At(12 * minute), RefreshPolicy.Never)
        assertEquals(2 * minute, Freshness.intervalMillis(policy, 10 * minute))
        // Once the moment passed only the interval is left.
        assertEquals(30 * minute, Freshness.intervalMillis(policy, 20 * minute))
        assertEquals(Freshness.NO_REFRESH, Freshness.intervalMillis(RefreshPolicy.Earliest(emptyList()), 0))
    }

    @Test
    fun staleData() {
        assertTrue(Freshness.isStale(updatedAtMillis = 0, nowMillis = 31 * minute, maxAgeMillis = 30 * minute))
        assertFalse(Freshness.isStale(updatedAtMillis = 0, nowMillis = 30 * minute, maxAgeMillis = 30 * minute))
    }
}
