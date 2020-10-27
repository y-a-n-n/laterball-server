package com.laterball.server.repository

import com.laterball.server.model.LeagueId
import com.laterball.server.api.DataApi
import com.laterball.server.api.model.ApiFixtureList
import com.laterball.server.data.Database
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class FixtureRepository(private val dataApi: DataApi, private val database: Database, private val clock: Clock = SystemClock()) {

    private val fixtureCache: ConcurrentHashMap<LeagueId, ApiFixtureList>
    private val lastUpdatedMap = ConcurrentHashMap<LeagueId, Long>()
    private val nextFixtureUpdateTime = ConcurrentHashMap<LeagueId, Long>()
    private val logger = LoggerFactory.getLogger(FixtureRepository::class.java.name)

    init {
        val storedFixtures = database.getFixtures()
        fixtureCache = ConcurrentHashMap(storedFixtures)

        logger.info("Initialised with fixtureCache size ${fixtureCache.size}")
    }

    fun getFixturesForLeague(leagueId: LeagueId): ApiFixtureList? {
        val current = fixtureCache[leagueId]
        if (needsUpdate(leagueId)) {
            try {
                lastUpdatedMap[leagueId] = clock.time
                val updated = dataApi.getPreviousFixtures(leagueId.id)
                val next = dataApi.getNextFixtures(leagueId.id)
                next?.let {
                    val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
                    val nextUpdate = next.fixtures
                        ?.map { Date.from(Instant.from(timeFormatter.parse(it.event_date))).time }
                        ?.minOrNull()
                    if (nextUpdate != null) {
                        val value = nextUpdate + 10800000L // 3 hours
                        nextFixtureUpdateTime[leagueId] = value
                        logger.info("Next update time for $leagueId is $value")
                    } else {
                        nextFixtureUpdateTime.remove(leagueId)
                    }
                }

                updated?.let {
                    fixtureCache[leagueId] = it
                }
                database.storeFixtures(fixtureCache)
                return updated ?: current
            } catch (e: Exception) {
                return current
            }
        }
        return current
    }

    private fun needsUpdate(leagueId: LeagueId): Boolean {
        val currentTime = clock.time
        val lastUpdate = lastUpdatedMap[leagueId] ?: 0L
        val nextUpdate = nextFixtureUpdateTime[leagueId] ?: currentTime
        return (currentTime - lastUpdate > 86400000) || (currentTime > nextUpdate)
    }
}
