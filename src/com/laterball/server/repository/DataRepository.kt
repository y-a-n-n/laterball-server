package com.laterball.server.repository

import com.laterball.server.api.model.Fixture
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

abstract class DataRepository<T> {

    protected lateinit var cache: ConcurrentHashMap<Fixture, T>

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private var initialised = false

    abstract val storedData: Map<Fixture, T>?

    fun getData(fixture: Fixture): T? {
        if (!initialised) loadData()
        return cache[fixture] ?: fetchAndCache(fixture)
    }

    private fun loadData() {
        val stored = storedData
        cache = if (stored != null) ConcurrentHashMap(stored) else  ConcurrentHashMap()
        logger.info("Initialised with cache size ${cache.size}")
    }

    fun removeFromCache(fixture: Fixture) {
        if (!initialised) loadData()
        cache.remove(fixture)
        // Caller's rsponsbility to sync database
    }

    private fun fetchAndCache(fixture: Fixture): T? {
        val result = fetch(fixture)
        result?.let { cache[fixture] = it }
        syncDatabase()
        return result
    }

    abstract fun syncDatabase()

    abstract fun fetch(fixture: Fixture): T?
}