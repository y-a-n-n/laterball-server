package com.laterball.server

import com.laterball.server.api.ApiFootball
import com.laterball.server.api.DataApi
import com.laterball.server.repository.*
import org.koin.dsl.module
import org.koin.experimental.builder.singleBy

val appModule = module(createdAtStart = true) {
    singleBy<DataApi, ApiFootball>()
    single { FixtureRepository(get()) }
    single { EventsRepository(get()) }
    single { StatsRepository(get()) }
    single { OddsRepository(get()) }
    single { RatingsRepository(get(), get(), get(), get()) }
}