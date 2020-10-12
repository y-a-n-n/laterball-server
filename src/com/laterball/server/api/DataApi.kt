package com.laterball.server.api

import com.laterball.server.api.model.ApiFixtureEvents
import com.laterball.server.api.model.ApiFixtureList
import com.laterball.server.api.model.ApiFixtureStats
import com.laterball.server.api.model.ApiOdds

interface DataApi {
    fun getFixtures(leagueId: Int): ApiFixtureList
    fun getStats(fixtureId: Int): ApiFixtureStats
    fun getOdds(fixtureId: Int): ApiOdds
    fun getEvents(fixtureId: Int): ApiFixtureEvents
}