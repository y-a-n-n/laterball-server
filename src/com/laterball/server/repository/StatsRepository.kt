package com.laterball.server.repository

import com.laterball.server.api.DataApi
import com.laterball.server.api.model.Fixture
import com.laterball.server.api.model.Statistics

class StatsRepository(private val dataApi: DataApi) : DataRepository<Statistics>() {
    override fun fetch(fixture: Fixture): Statistics? {
        return dataApi.getStats(fixture.fixture_id)?.statistics
    }
}