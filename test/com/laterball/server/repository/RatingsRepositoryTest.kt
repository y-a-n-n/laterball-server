package com.laterball.server.repository

import com.laterball.server.api.ApiFootball
import com.laterball.server.api.DataApi
import com.laterball.server.model.LeagueId
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.config.MapApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

internal class RatingsRepositoryTest {

    private lateinit var dataApi: DataApi
    private lateinit var clockMock: ClockMock
    private lateinit var client: HttpClient
    private lateinit var ratingsRepository: RatingsRepository
    private lateinit var databaseMock: DatabaseMock

    @Before
    fun setUp() {
        clockMock = ClockMock()
        databaseMock = DatabaseMock()
        val mockData = getResourceAsText("mockdata.txt").lines()
        var req = -1

        client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    req++
                    val data = mockData[req]
                    val responseHeaders = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                    respond(ByteReadChannel(data.toByteArray(Charsets.UTF_8)), headers = responseHeaders)
                }
            }
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
            install(DefaultRequest) {
                headers.append("Accept", ContentType.Application.Json.toString())
            }

        }
        val config = MapApplicationConfig().apply {
            put("ktor.api.apiKey", "foobar")
        }
        dataApi = ApiFootball(config, client)
        val fixtureRepository = FixtureRepository(dataApi, databaseMock, clockMock)
        val statsRepository = StatsRepository(dataApi, databaseMock)
        val eventsRepository = EventsRepository(dataApi, databaseMock)
        val oddsRepository = OddsRepository(dataApi, databaseMock)

        ratingsRepository = RatingsRepository(fixtureRepository, statsRepository, eventsRepository, oddsRepository)
    }

    @Test
    fun testRatings() {
        val ratings = ratingsRepository.getRatingsForLeague(LeagueId.EPL)
        assertNotNull(ratingsRepository.getRatingsForLeague(LeagueId.EPL))
    }

    private fun getResourceAsText(path: String): String {
        return RatingsRepositoryTest::class.java.classLoader.getResource(path).readText()
    }
}