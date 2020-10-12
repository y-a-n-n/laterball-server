package com.laterball.server.repository

import com.laterball.server.alg.determineRating
import com.laterball.server.model.LeagueId
import com.laterball.server.model.Rating
import java.util.*

class RatingsRepository(
    private val fixtureRepository: FixtureRepository,
    private val statsRepository: StatsRepository,
    private val eventsRepository: EventsRepository,
    private val oddsRepository: OddsRepository
) {
    fun getRatingsForLeague(leagueId: LeagueId): List<Rating>? {
        val currentTime = System.currentTimeMillis()
        return fixtureRepository.getFixturesForLeague(leagueId)
            ?.fixtures
            ?.filter { currentTime - Date(it.event_date).time < 604_800_000 }
            ?.map {
                val stats = statsRepository.getStatsForFixture(leagueId, it.fixture_id)
                val odds = oddsRepository.getOddsForFixture(leagueId, it.fixture_id)
                val events = eventsRepository.getEventsForFixture(leagueId, it.fixture_id)
                determineRating(it, odds, stats.statistics, events)
            }
    }
}