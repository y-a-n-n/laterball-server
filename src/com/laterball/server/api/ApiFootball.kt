package com.laterball.server.api

import com.laterball.server.api.model.*
import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.url
import io.ktor.client.request.get
import io.ktor.config.HoconApplicationConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ApiFootball : DataApi {

    companion object {
        private const val BASE_URL = "https://api-football-v1.p.rapidapi.com/v2"
    }

    override var requestDelay: Long? = null
    
    private val requestThrottler = RequestThrottler()

    private val client = HttpClient(OkHttp) {
        val config =  HoconApplicationConfig(ConfigFactory.load())
        val apiKey = config.propertyOrNull("ktor.api.apiKey")?.getString()
        install(HttpTimeout) {
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        install(DefaultRequest) {
            headers.append("X-RapidAPI-Key", apiKey ?: "")
            headers.append("X-RapidAPI-Host", "api-football-v1.p.rapidapi.com")
        }
    }

    // TODO: update request throttler with x-ratelimit-requests-remaining header

    override fun getFixtures(leagueId: Int): ApiFixtureList? {
        if (!requestThrottler.canRequest) return null
        return runBlocking {
            requestDelay?.let { delay(it) }
            return@runBlocking client.get<FixtureList> {
                url("$BASE_URL/fixtures/league/$leagueId/last/50")
            }.api
        }
    }

    override fun getStats(fixtureId: Int): ApiFixtureStats? {
        if (!requestThrottler.canRequest) return null
        return runBlocking {
            requestDelay?.let { delay(it) }
            return@runBlocking client.get<FixtureStats> {
                url("$BASE_URL/statistics/fixture/$fixtureId")
            }.api
        }
    }

    override fun getOdds(fixtureId: Int): ApiOdds? {
        if (!requestThrottler.canRequest) return null
        return runBlocking {
            requestDelay?.let { delay(it) }
            return@runBlocking client.get<Odds> {
                url("$BASE_URL/odds/fixture/$fixtureId")
            }.api
        }
    }

    override fun getEvents(fixtureId: Int): ApiFixtureEvents? {
        if (!requestThrottler.canRequest) return null
        return runBlocking {
            requestDelay?.let { delay(it) }
            return@runBlocking client.get<FixtureEvents> {
                url("$BASE_URL/events/$fixtureId")
            }.api
        }
    }
}