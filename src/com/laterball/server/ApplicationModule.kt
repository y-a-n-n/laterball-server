package com.laterball.server

import com.laterball.server.api.ApiFootball
import com.laterball.server.api.DataApi
import org.koin.dsl.module
import org.koin.experimental.builder.singleBy

val appModule = module(createdAtStart = true) {
    singleBy<DataApi, ApiFootball>()
}