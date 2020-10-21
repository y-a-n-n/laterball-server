package com.laterball.server

import com.laterball.server.model.LeagueId
import com.laterball.server.repository.RatingsRepository
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.auth.*
import io.ktor.client.engine.jetty.Jetty
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.html.respondHtml
import io.ktor.locations.*
import kotlinx.html.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(Authentication) {
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate { if (it.name == "test" && it.password == "password") UserIdPrincipal(it.name) else null }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    install(Locations) {
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    // Lazy inject HelloService
    val repo by inject<RatingsRepository>()

    // https://ktor.io/servers/features/https-redirect.html#testing
    if (!testing) {
//        install(HttpsRedirect) {
//            // The port to redirect to. By default 443, the default HTTPS port.
//            sslPort = 443
//            // 301 Moved Permanently, or 302 Found redirect.
//            permanentRedirect = true
//        }
//        install(HSTS) {
//            includeSubDomains = true
//        }

    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    routing {
        get("/ratings") {
            val ratings = repo.getRatingsForLeague(LeagueId.EPL)
            call.respondHtml {
                body {
                    h1 { +"LATERBALL" }
                    ratings?.let {
                        ul {
                            for (rating in it) {
                                li { +"${rating.homeTeam} ${rating.rating} ${rating.awayTeam}" }
                            }
                        }
                    } ?: h2 { +"No recent games, check back later!" }
                }
            }
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
//        static("/static") {
//            resources("static")
//        }
    }
}

