package com.laterball.server.repository

import com.laterball.server.model.LeagueId
import com.laterball.server.api.DataApi
import com.laterball.server.api.model.ApiFixtureList
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class FixtureRepository(private val dataApi: DataApi, private val clock: Clock = SystemClock()) {

    private val fixtureCache: ConcurrentHashMap<Int, ApiFixtureList> = ConcurrentHashMap()
    private val lastUpdatedMap: ConcurrentHashMap<Int, Long> = ConcurrentHashMap()
    private val nextFixtureUpdateTime: HashMap<LeagueId, Long> = HashMap()

    fun getFixturesForLeague(leagueId: LeagueId): ApiFixtureList? {
        val current = fixtureCache[leagueId.id]
        return if (needsUpdate(leagueId)) {
            try {
                lastUpdatedMap[leagueId.id] = clock.time
                val updated = dataApi.getPreviousFixtures(leagueId.id)
                val next = dataApi.getNextFixtures(leagueId.id)
                next?.let {
                    val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
                    val nextUpdate = it.fixtures
                        .map { Date.from(Instant.from(timeFormatter.parse(it.event_date))).time }
                        .minOrNull()
                    if (nextUpdate != null) {
                        nextFixtureUpdateTime[leagueId] = nextUpdate + 10800000L // 3 hours
                    } else {
                        nextFixtureUpdateTime.remove(leagueId)
                    }
                }
                updated?.let { fixtureCache[leagueId.id] = it }
                updated
            } catch (e: Exception) {
                null
            }
        } else {
            current
        }
    }

    private fun needsUpdate(leagueId: LeagueId): Boolean {
        val currentTime = clock.time
        val lastUpdate = lastUpdatedMap[leagueId.id] ?: 0L
        val nextUpdate = nextFixtureUpdateTime[leagueId] ?: currentTime
        return (currentTime - lastUpdate > 86400000) || (currentTime > nextUpdate)
    }
}
