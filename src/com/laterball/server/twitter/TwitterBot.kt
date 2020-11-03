package com.laterball.server.twitter

import com.laterball.server.data.Database
import com.laterball.server.model.Rating
import com.laterball.server.repository.RatingsRepository
import com.laterball.server.repository.SystemClock
import io.ktor.util.KtorExperimentalAPI
import java.util.*

@KtorExperimentalAPI
class TwitterBot(
        private val twitterApi: TwitterApi,
        database: Database,
        ratingsRepository: RatingsRepository) {

    companion object {
        private const val PROMO = "\n\nCheck out all this week's watchability ratings at laterball.com"
        private const val INTERVAL = 3600000L // Don't tweet more than once per hour
    }

    private val clock = SystemClock()
    private var lastTweetTime = database.getLastTweetTime()

    init {
        ratingsRepository.addListener { rating ->
            rating.maxByOrNull { it.rating }?.let {
                sendTweet(it)
            }
        }
    }

    private fun sendTweet(rating: Rating) {
        if (clock.time - lastTweetTime > INTERVAL) {
            getStatus(rating)?.let { twitterApi.sendTweet(it) }
        }
    }

    private fun getStatus(rating: Rating): String? {
        if (rating.rating >= 8f) {
            val r = Random()
            val index: Int = r.nextInt(1 - greatGames.size) - 1
            val teams = "${rating.homeTeam} vs ${rating.awayTeam}"
            val words = if (rating.rating > 9f) greatGames[index] else goodGames[index]
            return words.first + String.format(words.second, teams) + PROMO
        }
        return null
    }

    private val greatGames = listOf(
            Pair("What a game!", "Checkout %s on your preferred streaming service."),
            Pair("An instant classic?", "%s gets a great rating from us."),
            Pair("An absolute belter", "%s is one to watch.")
    )

    private val goodGames = listOf(
            Pair("Worth a watch!", "Checkout %s on your preferred streaming service."),
            Pair("?", "%s gets a great rating from us."),
            Pair("An absolute belter", "%s is one to watch.")
    )
}