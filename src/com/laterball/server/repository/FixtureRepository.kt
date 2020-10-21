package com.laterball.server.repository

import com.laterball.server.model.LeagueId
import com.laterball.server.api.DataApi
import com.laterball.server.api.model.ApiFixtureList
import java.util.concurrent.ConcurrentHashMap

class FixtureRepository(private val dataApi: DataApi, private val clock: Clock = SystemClock()) {

    private val fixtureCache: ConcurrentHashMap<Int, ApiFixtureList> = ConcurrentHashMap()
    private val lastUpdatedMap: ConcurrentHashMap<Int, Long> = ConcurrentHashMap()

    fun getFixturesForLeague(leagueId: LeagueId): ApiFixtureList? {
        val current = fixtureCache[leagueId.id]
        return if (needsUpdate(leagueId)) {
            try {
                lastUpdatedMap[leagueId.id] = clock.time
                val updated = dataApi.getFixtures(leagueId.id)
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
        return (currentTime - (lastUpdatedMap[leagueId.id] ?: 0L)) > 86400000
    }
}
