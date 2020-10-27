package com.laterball.server.data

import com.laterball.server.api.model.*
import com.laterball.server.model.LeagueId

interface Database {
    fun storeFixtures(fixtures: Map<LeagueId, ApiFixtureList>)
    fun getFixtures(): Map<LeagueId, ApiFixtureList>
    fun getStats(): Map<Fixture, Statistics>
    fun getEvents(): Map<Fixture, ApiFixtureEvents>
    fun getOdds(): Map<Fixture, Bet>
    fun storeStats(stats: Map<Fixture, Statistics>)
    fun storeEvents(events: Map<Fixture, ApiFixtureEvents>)
    fun storeOdds(odds: Map<Fixture, Bet>)
}