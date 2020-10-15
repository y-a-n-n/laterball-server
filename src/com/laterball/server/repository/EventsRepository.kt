package com.laterball.server.repository

import com.laterball.server.api.DataApi
import com.laterball.server.api.model.ApiFixtureEvents
import com.laterball.server.api.model.Fixture

class EventsRepository(private val dataApi: DataApi) : DataRepository<ApiFixtureEvents>() {
    override fun fetch(fixture: Fixture): ApiFixtureEvents? {
        return dataApi.getEvents(fixture.fixture_id)
    }
}