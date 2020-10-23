package com.laterball.server

import com.laterball.server.api.ApiFootball
import com.laterball.server.api.DataApi
import com.laterball.server.repository.*
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.koin.dsl.module

@OptIn(KtorExperimentalAPI::class)
val appModule = module(createdAtStart = true) {
    factory<ApplicationConfig> { HoconApplicationConfig(ConfigFactory.load()) }
    factory<DataApi> { ApiFootball(get()) }
    single { FixtureRepository(get()) }
    single { EventsRepository(get()) }
    single { StatsRepository(get()) }
    single { OddsRepository(get()) }
    single { RatingsRepository(get(), get(), get(), get()) }
}