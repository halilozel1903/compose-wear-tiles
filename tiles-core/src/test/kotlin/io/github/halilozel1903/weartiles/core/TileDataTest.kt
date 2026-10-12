package io.github.halilozel1903.weartiles.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TileDataTest {

    private val minute = 60_000L
    private val hour = 3_600_000L

    @Test
    fun progressGoal() {
        val steps = ProgressGoal("Steps", current = 8_421.0, target = 10_000.0, unit = "steps")
        assertEquals(0.8421f, steps.fraction, 0.0001f)
        assertEquals("84%", steps.percentText)
        assertEquals("8,421", steps.currentText())
        assertEquals("8.4k", steps.currentText(compact = true))
        assertEquals("of 10k steps", steps.targetText())
        assertEquals(1_579.0, steps.remaining)
        assertFalse(steps.isReached)
    }

    @Test
    fun progressGoalOverTheTarget() {
        val water = ProgressGoal("Water", current = 10.0, target = 8.0)
        assertEquals(1f, water.fraction)
        assertTrue(water.isReached)
        assertEquals(0.0, water.remaining)
        assertEquals("125%", water.percentText)
        assertEquals("of 8", water.targetText())
        assertFailsWith<IllegalArgumentException> { ProgressGoal("Bad", 1.0, 0.0) }
    }

    @Test
    fun stats() {
        val list = StatList(
            title = "Today",
            items = listOf(
                StatItem.number("Steps", 12_400.0, trend = Trend.Up),
                StatItem("Temp", "18°"),
                StatItem("Wind", "12", unit = "km/h", trend = Trend.Down),
            ),
            updatedAtMillis = 0,
        )
        assertEquals("12.4k", list.items[0].value)
        assertEquals("Steps ↑", list.items[0].labelWithTrend)
        assertEquals("Temp", list.items[1].labelWithTrend)
        assertEquals(2, list.visibleItems(TileScreen.SmallRound.layout).size)
        assertEquals(3, list.visibleItems(TileScreen.LargeRound.layout).size)
        assertEquals("Updated 5 min ago", list.updatedText(5 * minute))
        assertEquals("Updated just now", list.updatedText(0))
        assertNull(list.copy(updatedAtMillis = null).updatedText(0))
    }

    @Test
    fun shortcutGrid() {
        val grid = ShortcutGrid("Start", listOf(1, 2, 3, 4, 5))
        assertEquals(listOf(listOf(1, 2), listOf(3, 4)), grid.rows(TileScreen.SmallRound.layout))
        assertEquals(1, grid.hiddenCount(TileScreen.SmallRound.layout))
        assertEquals(0, grid.hiddenCount(TileScreen.LargeRound.layout))
    }

    @Test
    fun countdown() {
        val rain = Countdown("Rain in", targetMillis = 2 * hour, startMillis = 0)
        val now = 2 * hour - (85 * minute + 30_000)
        assertEquals("1h 25m", rain.remainingText(now))
        assertEquals(85 * minute + 30_000, rain.remainingMillis(now))
        assertEquals(0.2875f, rain.progress(now), 0.0001f)
        assertFalse(rain.isOver(now))
        assertEquals("Now", rain.remainingText(3 * hour))
        assertEquals(0L, rain.remainingMillis(3 * hour))
        assertEquals(1f, rain.progress(3 * hour))
        assertEquals(0f, rain.copy(startMillis = null).progress(now))
    }

    @Test
    fun countdownChangesOnMinuteAndHourBoundaries() {
        val target = 10 * 24 * hour
        val race = Countdown("Race", targetMillis = target)
        // 1h 25m 30s left: "1h 25m" turns into "1h 24m" 30 s later.
        val now = target - (85 * minute + 30_000)
        assertEquals(now + 30_001, race.nextChangeMillis(now))
        // 2d 4h 10m left: the hours change in 10 minutes.
        val far = target - (52 * hour + 10 * minute)
        assertEquals(far + 10 * minute + 1, race.nextChangeMillis(far))
        // In the last minute: when it is over.
        assertEquals(target, race.nextChangeMillis(target - 30_000))
        assertNull(race.nextChangeMillis(target))
    }

    @Test
    fun countdownRefreshPolicy() {
        val race = Countdown("Race", targetMillis = 10 * hour)
        assertEquals(RefreshPolicy.At(10 * hour), race.refreshPolicy(nowMillis = 0, live = true))
        // Exactly 10 h left: "10h 00m" turns into "9h 59m" right away.
        assertEquals(RefreshPolicy.At(1), race.refreshPolicy(nowMillis = 0, live = false))
        // 1m 30s left: "1m" turns into "<1m" 30 s later.
        assertEquals(RefreshPolicy.At(10 * hour - 60_000 + 1), race.refreshPolicy(nowMillis = 10 * hour - 90_000, live = false))
        assertEquals(RefreshPolicy.Never, race.refreshPolicy(nowMillis = 10 * hour, live = true))
    }

    @Test
    fun resourceVersions() {
        val a = ResourceVersion.of(mapOf("run" to 1, "ride" to 2))
        val b = ResourceVersion.of(linkedMapOf("ride" to 2, "run" to 1))
        val c = ResourceVersion.of(mapOf("run" to 1, "ride" to 3))
        assertEquals(a, b)
        assertTrue(a != c)
        assertEquals(17, a.length)
        assertEquals(ResourceVersion.EMPTY, ResourceVersion.of(emptyMap()))
        assertEquals(ResourceVersion.of(listOf("x", "y")), ResourceVersion.of(listOf("y", "x")))
        assertTrue(ResourceVersion.isCurrent(a, b))
        assertFalse(ResourceVersion.isCurrent(null, a))
    }
}
