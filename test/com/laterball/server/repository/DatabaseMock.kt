package com.laterball.server.repository

import com.google.gson.Gson
import com.laterball.server.api.model.*
import com.laterball.server.data.Database
import com.laterball.server.model.LeagueId

class DatabaseMock : Database {

    private val map = HashMap<String, HashMap<String, String>>()
    private val gson = Gson()

    init {
        map["fixtures"] = HashMap()
        map["stats"] = HashMap()
        map["events"] = HashMap()
        map["odds"] = HashMap()
    }

    override fun storeFixtures(fixtures: Map<LeagueId, ApiFixtureList>) {
        fixtures.forEach {
            map["fixtures"]!![it.key.name] = gson.toJson(it.value)
        }
    }

    override fun getFixtures(): Map<LeagueId, ApiFixtureList> {
        val decoded = HashMap<LeagueId, ApiFixtureList>()
        LeagueId.values().forEach { leagueId ->
            map["fixtures"]!![leagueId.name]?.let {
                decoded[leagueId] = gson.fromJson(it, ApiFixtureList::class.java)
            }
        }
        return decoded
    }

    override fun getStats(): Map<Fixture, Statistics> {
        val decoded = HashMap<Fixture, Statistics>()
        map["stats"]?.entries?.forEach { entry ->
            val fixture = gson.fromJson(entry.key, Fixture::class.java)
            val stat = gson.fromJson(entry.value, Statistics::class.java)
            decoded[fixture] = stat
        }
        return decoded
    }

    override fun getEvents(): Map<Fixture, ApiFixtureEvents> {
        val decoded = HashMap<Fixture, ApiFixtureEvents>()
        map["events"]?.entries?.forEach { entry ->
            val fixture = gson.fromJson(entry.key, Fixture::class.java)
            val stat = gson.fromJson(entry.value, ApiFixtureEvents::class.java)
            decoded[fixture] = stat
        }
        return decoded
    }

    override fun getOdds(): Map<Fixture, Bet> {
        val decoded = HashMap<Fixture, Bet>()
        map["odds"]?.entries?.forEach { entry ->
            val fixture = gson.fromJson(entry.key, Fixture::class.java)
            val stat = gson.fromJson(entry.value, Bet::class.java)
            decoded[fixture] = stat
        }
        return decoded
    }

    override fun storeStats(stats: Map<Fixture, Statistics>) {
        stats.forEach {
            map["stats"]!![gson.toJson(it.key)] = gson.toJson(it.value)
        }
    }

    override fun storeEvents(events: Map<Fixture, ApiFixtureEvents>) {
        events.forEach {
            map["events"]!![gson.toJson(it.key)] = gson.toJson(it.value)
        }
    }

    override fun storeOdds(odds: Map<Fixture, Bet>) {
        odds.forEach {
            map["odds"]!![gson.toJson(it.key)] = gson.toJson(it.value)
        }
    }
}