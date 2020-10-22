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
import java.text.SimpleDateFormat
import java.util.*

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
            val analyticsTag = System.getenv("LATERBALL_ANALYTICS_TAG")
            call.respondHtml {
                head {
                    link(href = "/static/laterball.css", rel = "stylesheet")
                    link(href = "https://fonts.googleapis.com/css2?family=Bungee&display=swap", rel = "stylesheet")
                    script(type = ScriptType.textJavaScript, src = "https://www.googletagmanager.com/gtag/js?id=$analyticsTag") {}
                    script(type = ScriptType.textJavaScript) {
                        unsafe {
                            raw("""
                                window.dataLayer = window.dataLayer || [];
                                function gtag(){dataLayer.push(arguments);}
                                gtag('js', new Date());
                                gtag('config', '$analyticsTag');
                            """)
                        }
                    }
                }
                body {
                    h1 { +"LATERBALL" }
                    h2 { +"The best football games of the week, ranked without spoilers" }
                    div(classes = "w3-container") {
                        ratings?.let {
                            ul(classes = "w3-ul w3-card-4") {
                                ratings.forEachIndexed { index, rating ->
                                    li(classes = "w3-bar") {
                                        span(classes = "w3-bar-item w3-button w3-white w3-medium w3-right") { +"Where to watch?"  }
                                        img(classes = "w3-bar-item w3-circle w3-hide-small", src = rating.homeLogo) {
                                            style = "width:85px"
                                        }
                                        div(classes = "w3-bar-item") {
                                            span(classes = "w3-large") { +"${rating.homeTeam} vs ${rating.awayTeam}" }
                                            span { +"#${index + 1} match this week" }
                                        }
                                        img(classes = "w3-bar-item w3-circle w3-hide-small", src = rating.awayLogo) {
                                            style = "width:85px"
                                        }
                                    }
                                }
                            }
                        } ?: h2 { +"No recent games, check back later!" }
                        a(href = "/about") {
                            +"About Laterball"
                        }
                        div { +"© ${SimpleDateFormat("YYYY").format(Date())} Laterball" }
                    }
                }
            }
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }
    }
}

