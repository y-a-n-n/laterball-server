package com.laterball.server.repository

import com.laterball.server.alg.determineRating
import com.laterball.server.api.model.Fixture
import com.laterball.server.model.LeagueId
import com.laterball.server.model.Rating
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RatingsRepository(
    private val fixtureRepository: FixtureRepository,
    private val statsRepository: StatsRepository,
    private val eventsRepository: EventsRepository,
    private val oddsRepository: OddsRepository
) {

    private val ratingsMap = ConcurrentHashMap<Fixture, Rating>()

    fun getRatingsForLeague(leagueId: LeagueId): List<Rating>? {
        val currentTime = System.currentTimeMillis()
        // Get completed fixures in this league less that 1 week old
        val relevantFixtures = fixtureRepository.getFixturesForLeague(leagueId)
            ?.fixtures
            ?.filter { it.status == STATUS_FINISHED && currentTime - Date(it.event_date).time < 604_800_000 }

        // Remove old data from the map
        ratingsMap.entries.removeIf { relevantFixtures?.contains(it.key) == false }

        return relevantFixtures?.mapNotNull {
            // Calculate the rating only if we don't already have it
            ratingsMap[it] ?: calculateRating(it)
        }
    }

    private fun calculateRating(fixture: Fixture): Rating? {
        val stats = statsRepository.getData(fixture)
        val odds = oddsRepository.getData(fixture)
        val events = eventsRepository.getData(fixture)
        return if (stats != null && odds != null && events != null) {
            determineRating(fixture, odds, stats, events)
        } else {
            null
        }
    }

    companion object {
        private const val STATUS_FINISHED = "Match Finished"
    }
}