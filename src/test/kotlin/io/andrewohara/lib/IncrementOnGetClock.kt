package io.andrewohara.lib

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class IncrementOnGetClock(
    start: Instant = Instant.parse("2021-07-30T12:00:00Z"),
    private val increment: Duration = Duration.ofSeconds(5),
    private val zone: ZoneId = ZoneId.of("America/Montreal")
): Clock() {
    private var time = start

    override fun getZone() = zone

    override fun withZone(zone: ZoneId) = IncrementOnGetClock(time, increment, zone)

    override fun instant(): Instant {
        val result = time
        time += increment
        return result
    }
}