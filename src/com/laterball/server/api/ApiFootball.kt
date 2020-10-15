package com.laterball.server.api

import com.laterball.server.api.model.ApiFixtureEvents
import com.laterball.server.api.model.ApiFixtureList
import com.laterball.server.api.model.ApiFixtureStats
import com.laterball.server.api.model.ApiOdds
import io.ktor.client.HttpClient
import io.ktor.client.engine.jetty.Jetty
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.url
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking

class ApiFootball : DataApi {

    companion object {
        private const val BASE_URL = "https://api-football-v1.p.rapidapi.com/v2/"
        private const val REQUESTS_PER_DAY = 100
        private const val RESET_TIME = 100
    }

    private var requestsToday = 0

    private val client = HttpClient(Jetty) {
        install(HttpTimeout) {
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    private fun canRequest(): Boolean {
        return true
    }

    override fun getFixtures(leagueId: Int): ApiFixtureList? {
        if (!canRequest()) return null
        return runBlocking {
            return@runBlocking client.get<ApiFixtureList> {
                url("$BASE_URL/league/$leagueId/last/50")
            }
        }
    }

    override fun getStats(fixtureId: Int): ApiFixtureStats? {
        if (!canRequest()) return null
        return runBlocking {
            return@runBlocking client.get<ApiFixtureStats> {
                url("$BASE_URL/statistics/fixture/$fixtureId")
            }
        }
    }

    override fun getOdds(fixtureId: Int): ApiOdds? {
        if (!canRequest()) return null
        return runBlocking {
            return@runBlocking client.get<ApiOdds> {
                url("$BASE_URL/odds/fixture/$fixtureId")
            }
        }
    }

    override fun getEvents(fixtureId: Int): ApiFixtureEvents? {
        if (!canRequest()) return null
        return runBlocking {
            return@runBlocking client.get<ApiFixtureEvents> {
                url("$BASE_URL/events/$fixtureId")
            }
        }
    }
}