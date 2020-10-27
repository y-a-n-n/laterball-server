package com.laterball.server

import com.laterball.server.api.ApiFootball
import com.laterball.server.api.DataApi
import com.laterball.server.data.AppEngineDatastore
import com.laterball.server.data.Database
import com.laterball.server.repository.*
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.koin.dsl.module
import org.koin.experimental.builder.singleBy

@OptIn(KtorExperimentalAPI::class)
val appModule = module(createdAtStart = true) {
    factory<ApplicationConfig> { HoconApplicationConfig(ConfigFactory.load()) }
    factory<DataApi> { ApiFootball(get()) }
    singleBy<Database, AppEngineDatastore>()
    single { FixtureRepository(get(), get()) }
    single { EventsRepository(get(), get()) }
    single { StatsRepository(get(), get()) }
    single { OddsRepository(get(), get()) }
    single { RatingsRepository(get(), get(), get(), get()) }
}