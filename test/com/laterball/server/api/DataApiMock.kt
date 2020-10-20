package com.laterball.server.api

import com.laterball.server.api.model.ApiFixtureEvents
import com.laterball.server.api.model.ApiFixtureList
import com.laterball.server.api.model.ApiFixtureStats
import com.laterball.server.api.model.ApiOdds

class DataApiMock : DataApi {

    var testFixtures: ApiFixtureList? = null
    var testStats: ApiFixtureStats? = null
    var testOdds: ApiOdds? = null
    var testEvents: ApiFixtureEvents? = null

    override fun getFixtures(leagueId: Int): ApiFixtureList? {
        return testFixtures
    }

    override fun getStats(fixtureId: Int): ApiFixtureStats? {
        return testStats
    }

    override fun getOdds(fixtureId: Int): ApiOdds? {
        return testOdds
    }

    override fun getEvents(fixtureId: Int): ApiFixtureEvents? {
        return testEvents
    }
}