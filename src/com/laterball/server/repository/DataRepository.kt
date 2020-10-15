package com.laterball.server.repository

import com.laterball.server.api.model.Fixture
import java.util.concurrent.ConcurrentHashMap

abstract class DataRepository<T> {

    private val cache = ConcurrentHashMap<Fixture, T>()

    fun getData(fixture: Fixture): T? {
        return cache[fixture] ?: fetchAndCache(fixture)
    }

    private fun fetchAndCache(fixture: Fixture): T? {
        val result = fetch(fixture)
        result?.let { cache[fixture] = it }
        return result
    }

    abstract fun fetch(fixture: Fixture): T?
}