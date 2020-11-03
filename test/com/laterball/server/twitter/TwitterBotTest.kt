package com.laterball.server.twitter

import com.laterball.server.api.ApiFootball
import com.laterball.server.api.DataApi
import com.laterball.server.model.Rating
import com.laterball.server.repository.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.config.MapApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class TwitterBotTest {

    private lateinit var databaseMock: DatabaseMock
    private lateinit var dataApi: DataApi
    private lateinit var clockMock: ClockMock
    private lateinit var client: HttpClient
    private lateinit var ratingsRepository: RatingsRepository
    private lateinit var twitterBot: TwitterBot
    private lateinit var twitterApiMock: TwitterApiMock

    @OptIn(KtorExperimentalAPI::class)
    @Before
    fun setUp() {
        databaseMock = DatabaseMock()
        clockMock = ClockMock()
        databaseMock = DatabaseMock()
        val config = MapApplicationConfig().apply {
            put("ktor.api.apiKey", "foobar")
        }
        client = HttpClient(MockEngine) {
            engine {
                addHandler { respond("") }
            }
        }
        dataApi = ApiFootball(config, client)
        val fixtureRepository = FixtureRepository(dataApi, databaseMock, clockMock)
        val statsRepository = StatsRepository(dataApi, databaseMock)
        val eventsRepository = EventsRepository(dataApi, databaseMock)
        val oddsRepository = OddsRepository(dataApi, databaseMock)

        twitterApiMock = TwitterApiMock()
        ratingsRepository = RatingsRepository(fixtureRepository, statsRepository, eventsRepository, oddsRepository)
        twitterBot = TwitterBot(twitterApiMock, databaseMock, ratingsRepository, clockMock)
    }

    @Test
    fun greatGame() {
        val rating1 = Rating("Foo", "Bar", "", "", "", 10f, "", 1f)
        val rating2 = Rating("Fizz", "Buss", "", "", "", 8f, "", 1f)
        val rating3 = Rating("Foo2", "Bar2", "", "", "", 4f, "", 1f)
        for (i in 0..100) {
            clockMock.time += 3600001L
            twitterBot.tweetForRatings(listOf(rating1, rating2, rating3))
            assertEquals(1, twitterApiMock.sent.size)
            assertTrue(twitterApiMock.sent[0].contains("Foo vs Bar"))
            twitterApiMock.sent.clear()
        }
    }

    @Test
    fun dontTweetTooOften() {

        clockMock.time += 3600001L

        val rating2 = Rating("Fizz", "Buss", "", "", "", 8f, "", 1f)
        val rating3 = Rating("Foo2", "Bar2", "", "", "", 4f, "", 1f)
        twitterBot.tweetForRatings(listOf(rating2, rating3))
        assertEquals(1, twitterApiMock.sent.size)
        assertTrue(twitterApiMock.sent[0].contains("Fizz vs Buss"))

        clockMock.time += 30000

        twitterBot.tweetForRatings(listOf(rating2, rating3))
        assertEquals(1, twitterApiMock.sent.size)
        assertTrue(twitterApiMock.sent[0].contains("Fizz vs Buss"))

        clockMock.time += 3600000L

        twitterBot.tweetForRatings(listOf(rating2, rating3))
        assertEquals(2, twitterApiMock.sent.size)
        assertTrue(twitterApiMock.sent[1].contains("Fizz vs Buss"))
    }
}