package com.laterball.server.twitter

import com.laterball.server.data.Database
import com.laterball.server.model.Rating
import com.laterball.server.model.TwitterData
import com.laterball.server.repository.Clock
import com.laterball.server.repository.RatingsRepository
import com.laterball.server.repository.SystemClock
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.max

@KtorExperimentalAPI
class TwitterBot(
        private val twitterApi: TwitterApi,
        private val database: Database,
        ratingsRepository: RatingsRepository,
        private val clock: Clock = SystemClock()
) {

    companion object {
        private val PROMO = listOf(
                "\n\nSee all this week's watchability ratings at laterball.com",
                "\n\nFor a full list of watchability ratings, head to laterball.com",
                "\n\nTo see what else is worth watching this week, visit laterball.com",
        )

        private const val INTERVAL = 3600000L * 4 // Don't tweet more than once per four hours
    }

    private val logger = LoggerFactory.getLogger(TwitterBot::class.java)


    private var lastFixtureId: Int

    private var lastTweetTime: Long

    init {
        ratingsRepository.addListener { ratings ->
            tweetForRatings(ratings)
        }
        val twitterData = database.getTwitterData()
        lastFixtureId = twitterData.lastFixtureTweeted
        lastTweetTime = twitterData.lastTweetTime
    }

    fun tweetForRatings(ratings: List<Rating>) {
        logger.info("Received ${ratings.size} new ratings")
        ratings.maxByOrNull { it.rating }?.let {
            logger.info("Top rating is ${it.rating}")
            sendTweet(it)
        }
    }

    private fun randIndex(size: Int): Int {
        val r = Random()
        return max(0, r.nextInt(size) - 1)
    }

    private fun sendTweet(rating: Rating) {
        val currentTime = clock.time
        if (lastFixtureId != rating.fixtureId && currentTime - lastTweetTime > INTERVAL) {
            getStatus(rating)?.let {
                logger.info("Tweeting: $it")
                lastTweetTime = currentTime
                lastFixtureId = rating.fixtureId
                database.storeTwitterData(TwitterData(lastTweetTime, lastFixtureId))
                twitterApi.sendTweet(it)
            }
        } else {
            logger.info("Not tweeting; too soon or already tweeted this rating")
        }
    }

    private fun getStatus(rating: Rating): String? {
        if (rating.rating >= 8f) {
            val teams = "${rating.homeTeam} vs ${rating.awayTeam}"
            val words = if (rating.rating >= 10f) greatGames[randIndex(greatGames.size)] else goodGames[randIndex(goodGames.size)]
            return words.first + String.format(words.second, teams) + PROMO[randIndex(PROMO.size)]
        }
        return null
    }

    private val greatGames = listOf(
            Pair("⭐⭐⭐⭐⭐ What a game!", " Checkout %s on your preferred streaming service."),
            Pair("⭐⭐⭐⭐⭐ Five star game alert!", " Checkout %s on your preferred streaming service."),
            Pair("⭐⭐⭐⭐⭐ An instant classic?", " %s gets a great rating from us."),
            Pair("An absolute belter ⭐⭐⭐⭐⭐!", " %s is one to watch.")
    )

    private val goodGames = listOf(
            Pair("⭐⭐⭐⭐ Worth a watch!", " Checkout %s on your preferred streaming service."),
            Pair("With ⭐⭐⭐⭐ four stars,", " %s gets a great rating from us."),
            Pair("⭐⭐⭐⭐ Nice one!", " %s is one to watch.")
    )
}