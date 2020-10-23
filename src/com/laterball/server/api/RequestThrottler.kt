package com.laterball.server.api

import com.laterball.server.repository.Clock
import com.laterball.server.repository.SystemClock

class RequestThrottler(private val clock: Clock = SystemClock(), private val maxPerDay: Int = 100) {

    private var lastRolloverDay = clock.dayOfYear

    var requestsRemainingToday = maxPerDay

    val canRequest: Boolean get() {
        return if (lastRolloverDay == clock.dayOfYear) {
            requestsRemainingToday--
            requestsRemainingToday > 0
        } else {
            lastRolloverDay = clock.dayOfYear
            requestsRemainingToday = maxPerDay - 1
            true
        }
    }
}