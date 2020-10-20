package com.laterball.server.com.laterball.server.repository

import com.laterball.server.repository.Clock
import java.util.*

class ClockMock : Clock {
    override var dayOfYear = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
    override var time = System.currentTimeMillis()
}