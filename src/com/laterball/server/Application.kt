package com.laterball.server

import com.laterball.server.api.DataApi
import com.laterball.server.html.Generator
import com.laterball.server.model.LeagueId
import com.laterball.server.repository.RatingsRepository
import io.ktor.application.*
import io.ktor.config.ApplicationConfig
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.html.respondHtml
import io.ktor.response.respondRedirect
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@OptIn(KtorExperimentalAPI::class)
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {

    install(ContentNegotiation) {
        gson {}
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    // Lazy inject HelloService
    val repo by inject<RatingsRepository>()
    val api by inject<DataApi>()
    val config by inject<ApplicationConfig>()

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        host("laterball.com", schemes = listOf("https"))
        host("laterball.et.r.appspot.com", schemes = listOf("https"))
    }

    val generator = Generator(repo, config)

    // Init data, slowly to not hit api request limits
    api.requestDelay = 15000
    repo.getRatingsForLeague(LeagueId.EPL)
    repo.getRatingsForLeague(LeagueId.CHAMPIONS_LEAGUE)
    api.requestDelay = null

    routing {
        get("/about") {
            call.respondHtml {
                generator.generateAbout(this)
            }
        }

        get("/") {
            call.respondRedirect("/${LeagueId.EPL.path}")
        }

        LeagueId.values().forEach {leagueId ->
            get("/${leagueId.path}") {
                val sortByDate = call.request.queryParameters["sort"] == "date"
                call.respondHtml {
                    generator.generateForLeague(this, leagueId, sortByDate)
                }
            }
        }

        static("/static") {
            resources("static")
        }
    }
}

