package com.laterball.server.data

import com.google.cloud.datastore.*
import com.google.gson.Gson
import com.laterball.server.api.model.*
import com.laterball.server.model.LeagueId
import com.laterball.server.repository.Clock
import com.laterball.server.repository.SystemClock
import org.slf4j.LoggerFactory

class AppEngineDatastore : Database {

    private val gson = Gson()

    private val logger = LoggerFactory.getLogger(AppEngineDatastore::class.java)
    private val datastore: Datastore = DatastoreOptions.getDefaultInstance().service
    private val clock: Clock = SystemClock()

    companion object {
        private const val KIND = "laterball"
        private const val FIXTURES = "fixtures"
        private const val EVENTS = "events"
        private const val STATS = "stats"
        private const val ODDS = "odds"
        private const val TWEET_TIME = "tweet_time"
    }

    override fun storeFixtures(fixtures: Map<LeagueId, ApiFixtureList>) {
        try {
            logger.info("Storing ${fixtures.size} fixtures")
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(FIXTURES)
            val builder = Entity.newBuilder(taskKey)
            fixtures.entries.forEach {
                builder.set(it.key.name, StringValue.newBuilder(gson.toJson(it.value)).setExcludeFromIndexes(true).build())
            }
            val task = builder.build()
            datastore.put(task)
        } catch (e: Exception) {
            logger.error("Failed to store fixtures", e)
        }
    }

    override fun getFixtures(): Map<LeagueId, ApiFixtureList> {
        logger.info("Retrieving fixtures")
        return try {
            val map = HashMap<LeagueId, ApiFixtureList>()
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(FIXTURES)
            val data = datastore.get(taskKey)
            data.properties.keys.forEach { key ->
                val leagueId = LeagueId.valueOf(key)
                data.getString(key)?.let {
                    val list = gson.fromJson(it, ApiFixtureList::class.java)
                    map[leagueId] = list
                }
            }
            map
        } catch (e: Exception) {
            logger.error("Failed to retrieve fixtures", e)
            HashMap()
        }
    }


    override fun getStats(): Map<Fixture, Statistics> {
        logger.info("Retrieving stats")
        return try {
            val map = HashMap<Fixture, Statistics>()
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(STATS)
            val data = datastore.get(taskKey)
            data.properties.keys.forEach { key ->
                val fixture = gson.fromJson(key, Fixture::class.java)
                data.getString(key)?.let {
                    val stat = gson.fromJson(it, Statistics::class.java)
                    map[fixture] = stat
                }
            }
            map
        } catch (e: Exception) {
            logger.error("Failed to retrieve stats", e)
            HashMap()
        }
    }

    override fun getEvents(): Map<Fixture, ApiFixtureEvents> {
        logger.info("Retrieving events")
        return try {
            val map = HashMap<Fixture, ApiFixtureEvents>()
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(EVENTS)
            val data = datastore.get(taskKey)
            data.properties.keys.forEach { key ->
                val fixture = gson.fromJson(key, Fixture::class.java)
                data.getString(key)?.let {
                    val events = gson.fromJson(it, ApiFixtureEvents::class.java)
                    map[fixture] = events
                }
            }
            map
        } catch (e: Exception) {
            logger.error("Failed to retrieve events", e)
            HashMap()
        }
    }

    override fun getOdds(): Map<Fixture, Bet> {
        logger.info("Retrieving odds")
        return try {
            val map = HashMap<Fixture, Bet>()
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(ODDS)
            val data = datastore.get(taskKey)
            data.properties.keys.forEach { key ->
                val fixture = gson.fromJson(key, Fixture::class.java)
                data.getString(key)?.let {
                    val odds = gson.fromJson(it, Bet::class.java)
                    map[fixture] = odds
                }
            }
            map
        } catch (e: Exception) {
            logger.error("Failed to retrieve odds", e)
            HashMap()
        }
    }

    override fun storeStats(stats: Map<Fixture, Statistics>) {
        try {
            logger.info("Storing ${stats.size} stats")
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(STATS)
            val builder = Entity.newBuilder(taskKey)
            stats.entries.forEach {
                builder.set(gson.toJson(it.key), StringValue.newBuilder(gson.toJson(it.value)).setExcludeFromIndexes(true).build())
            }
            val task = builder.build()
            datastore.put(task)
        } catch (e: Exception) {
            logger.error("Failed to store stats", e)
        }
    }

    override fun storeEvents(events: Map<Fixture, ApiFixtureEvents>) {
        try {
            logger.info("Storing ${events.size} events")
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(EVENTS)
            val builder = Entity.newBuilder(taskKey)
            events.entries.forEach {
                builder.set(gson.toJson(it.key), StringValue.newBuilder(gson.toJson(it.value)).setExcludeFromIndexes(true).build())
            }
            val task = builder.build()
            datastore.put(task)
        } catch (e: Exception) {
            logger.error("Failed to store events", e)
        }
    }

    override fun storeOdds(odds: Map<Fixture, Bet>) {
        try {
            logger.info("Storing ${odds.size} odds")
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(ODDS)
            val builder = Entity.newBuilder(taskKey)
            odds.entries.forEach {
                builder.set(gson.toJson(it.key), StringValue.newBuilder(gson.toJson(it.value)).setExcludeFromIndexes(true).build())
            }
            val task = builder.build()
            datastore.put(task)
        } catch (e: Exception) {
            logger.error("Failed to store odds", e)
        }
    }

    override fun storeLastTweetTime(time: Long) {
        try {
            logger.info("Storing last tweet time as $time")
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(TWEET_TIME)
            val builder = Entity.newBuilder(taskKey)
            builder.set(TWEET_TIME, time)
            datastore.put(builder.build())
        } catch (e: Exception) {
            logger.error("Failed to store tweet time", e)
        }

    }

    override fun getLastTweetTime(): Long {
        return try {
            val taskKey = datastore.newKeyFactory().setKind(KIND).newKey(TWEET_TIME)
            datastore.get(taskKey).getLong(TWEET_TIME) ?: 0
        } catch (e: Exception) {
            logger.error("Failed to get last tweet time; assume now to prevent too many tweets", e)
            clock.time
        }
    }
}