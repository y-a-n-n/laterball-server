package com.laterball.server.api

import java.util.*

class RequestThrottler {

    companion object {
        private const val REQUESTS_PER_DAY = 100
    }

    private var lastRolloverDay = Calendar.getInstance()

    private var requestsToday = 0

    val canRequest: Boolean get() {
        val currentTime = Calendar.getInstance()
        return if (lastRolloverDay[Calendar.DATE] == currentTime[Calendar.DATE]) {
            requestsToday++
            requestsToday < REQUESTS_PER_DAY
        } else {
            lastRolloverDay[Calendar.DATE] = currentTime[Calendar.DATE]
            requestsToday = 1
            true
        }
    }
}