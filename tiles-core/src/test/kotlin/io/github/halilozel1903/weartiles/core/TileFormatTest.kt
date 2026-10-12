package io.github.halilozel1903.weartiles.core

import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TileFormatTest {

    @Test
    fun compactNumbers() {
        assertEquals("0", TileFormat.compact(0))
        assertEquals("950", TileFormat.compact(950))
        assertEquals("72.5", TileFormat.compact(72.5))
        assertEquals("1k", TileFormat.compact(1_000))
        assertEquals("1.2k", TileFormat.compact(1_234))
        assertEquals("12.4k", TileFormat.compact(12_400))
        assertEquals("12k", TileFormat.compact(12_000))
        assertEquals("123k", TileFormat.compact(123_456))
        assertEquals("1M", TileFormat.compact(999_960))
        assertEquals("1.3M", TileFormat.compact(1_250_000))
        assertEquals("2.5B", TileFormat.compact(2_500_000_000L))
        assertEquals("-3.4k", TileFormat.compact(-3_400))
        assertEquals("0", TileFormat.compact(-0.01))
    }

    @Test
    fun compactRoundsUpIntoTheNextUnit() {
        assertEquals("1k", TileFormat.compact(999.96))
        assertEquals("100k", TileFormat.compact(99_960))
    }

    @Test
    fun compactRejectsNaN() {
        assertFailsWith<IllegalArgumentException> { TileFormat.compact(Double.NaN) }
    }

    @Test
    fun groupedNumbers() {
        assertEquals("0", TileFormat.grouped(0))
        assertEquals("999", TileFormat.grouped(999))
        assertEquals("8,421", TileFormat.grouped(8_421))
        assertEquals("1,234,567", TileFormat.grouped(1_234_567))
        assertEquals("-1,234,567", TileFormat.grouped(-1_234_567))
    }

    @Test
    fun numbersForTiles() {
        assertEquals("8,421", TileFormat.number(8_421.0))
        assertEquals("8.4k", TileFormat.number(8_421.0, compact = true))
        assertEquals("124k", TileFormat.number(123_900.0))
        assertEquals("3.5", TileFormat.number(3.5))
    }

    @Test
    fun percent() {
        assertEquals("84%", TileFormat.percent(0.842))
        assertEquals("120%", TileFormat.percent(1.2))
    }

    @Test
    fun durations() {
        assertEquals("0s", TileFormat.duration(-5_000))
        assertEquals("45s", TileFormat.duration(45_000))
        assertEquals("12m", TileFormat.duration(12 * 60_000L + 30_000))
        assertEquals("1h 05m", TileFormat.duration(65 * 60_000L))
        assertEquals("2d 4h", TileFormat.duration((2 * 24 + 4) * 3_600_000L + 59 * 60_000L))
    }

    @Test
    fun countdowns() {
        assertEquals("Now", TileFormat.countdown(0))
        assertEquals("Go!", TileFormat.countdown(-1, doneLabel = "Go!"))
        assertEquals("<1m", TileFormat.countdown(59_999))
        assertEquals("1m", TileFormat.countdown(60_000))
        assertEquals("1h 25m", TileFormat.countdown(85 * 60_000L + 59_000))
    }

    @Test
    fun ages() {
        val now = 10_000_000_000L
        assertEquals("Just now", TileFormat.age(now - 30_000, now))
        assertEquals("Just now", TileFormat.age(now + 60_000, now))
        assertEquals("5 min ago", TileFormat.age(now - 5 * 60_000, now))
        assertEquals("2 h ago", TileFormat.age(now - 150 * 60_000, now))
        assertEquals("3 d ago", TileFormat.age(now - 3 * 86_400_000L, now))
    }

    @Test
    fun timeOfDay() {
        // 2026-10-06T14:30:00Z
        assertEquals("14:30", TileFormat.timeOfDay(1_791_297_000_000L, ZoneOffset.UTC))
        assertEquals("16:30", TileFormat.timeOfDay(1_791_297_000_000L, ZoneOffset.ofHours(2)))
    }
}
