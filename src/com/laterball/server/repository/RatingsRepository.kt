package com.laterball.server.repository

import com.laterball.server.alg.determineRating
import com.laterball.server.api.model.Fixture
import com.laterball.server.model.LeagueId
import com.laterball.server.model.Rating
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor
import kotlin.math.min

class RatingsRepository(
    private val fixtureRepository: FixtureRepository,
    private val statsRepository: StatsRepository,
    private val eventsRepository: EventsRepository,
    private val oddsRepository: OddsRepository,
) {

    private val logger = LoggerFactory.getLogger(RatingsRepository::class.java)
    private val ratingsMap = ConcurrentHashMap<Fixture, Rating>()


    fun getRatingsForLeague(leagueId: LeagueId): List<Rating>? {
        val currentTime = System.currentTimeMillis()
        // Get completed fixtures in this league less that 1 week old
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val relevantFixtures = fixtureRepository.getFixturesForLeague(leagueId)
            ?.fixtures
            ?.filter {
                it.status == STATUS_FINISHED && currentTime - Date.from(Instant.from(timeFormatter.parse(it.event_date))).time < 604_800_000
            }

        val removeList = ratingsMap.entries.filter {
            relevantFixtures?.contains(it.key) == false
        }.map { it.key }

        // Remove old data from caches
        removeList.forEach {
            ratingsMap.remove(it)
            statsRepository.removeFromCache(it)
            eventsRepository.removeFromCache(it)
            oddsRepository.removeFromCache(it)
        }

        if (removeList.isNotEmpty()) {
            statsRepository.syncDatabase()
            eventsRepository.syncDatabase()
            oddsRepository.syncDatabase()
        }

        val ratings = relevantFixtures?.mapNotNull { fixture ->
            // Calculate the rating only if we don't already have it
            val existing = ratingsMap[fixture]
            if (existing != null) {
                existing
            } else {
                val calculated = calculateRating(fixture)
                calculated?.let { ratingsMap[fixture] = it }
                calculated
            }
        }?.sortedByDescending { it.rating }

        if (!ratings.isNullOrEmpty()) normalize(ratings)

        return ratings
    }

    private fun normalize(ratings: List<Rating>) {
        val maxRating = ratings[0].rating
        val maxStars = min(ratings[0].totalGoals * 2, 10).toFloat()
        val starsFactor = maxStars / maxRating
        ratings.forEach { it.rating = floor(it.rating * starsFactor).coerceAtLeast(1f) }
    }

    private fun calculateRating(fixture: Fixture): Rating? {
        logger.info("Cache miss!")
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
        private const val KEY_RATINGS = "fixtures"
    }
}