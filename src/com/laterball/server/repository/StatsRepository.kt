package com.laterball.server.repository

import com.laterball.server.model.LeagueId
import com.laterball.server.api.DataApi
import com.laterball.server.api.model.ApiFixtureStats

class StatsRepository(private val fixtureRepository: FixtureRepository, dataApi: DataApi) {

    // Map of league ID to stats for
    private val leagueStatsMap: Map<Int, LeagueStats> = emptyMap()

    fun getStatsForFixture(leagueId: LeagueId, fixtureId: Int): ApiFixtureStats {

    }


}

class LeagueStats {
    val statsMap: Map<Int, ApiFixtureStats> = emptyMap()
}