package com.laterball.server.api

import com.laterball.server.repository.Clock
import com.laterball.server.repository.SystemClock

class RequestThrottler(private val clock: Clock = SystemClock(), private val maxPerDay: Int = 100) {

    private var lastRolloverDay = clock.dayOfYear

    private var requestsToday = 0

    val canRequest: Boolean get() {
        return if (lastRolloverDay == clock.dayOfYear) {
            requestsToday++
            requestsToday < maxPerDay
        } else {
            lastRolloverDay = clock.dayOfYear
            requestsToday = 1
            true
        }
    }
}