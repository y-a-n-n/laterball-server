package com.laterball.server

import com.laterball.server.api.DataApi
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
import io.ktor.locations.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.html.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@OptIn(KtorExperimentalAPI::class)
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val logger = LoggerFactory.getLogger(Application::class.java)

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
    val api by inject<DataApi>()
    val config by inject<ApplicationConfig>()

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        host("laterball.com", schemes = listOf("https"))
        host("laterball.et.r.appspot.com", schemes = listOf("https"))
//        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    // Init data, slowly to not hit api request limits
    api.requestDelay = 15000
    repo.getRatingsForLeague(LeagueId.EPL)
    api.requestDelay = null

    val generateHeader = { html: HTML ->
        val analyticsTag = config.propertyOrNull("ktor.analytics.tag")?.getString() ?: ""
        html.head {
            styleLink("/static/laterball.css")
            link(href = "https://fonts.googleapis.com/css2?family=Roboto+Slab&family=Turret+Road:wght@800&display=swap", rel = "stylesheet")
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
    }

    routing {
        get("/about") {
            call.respondHtml {
                generateHeader(this)
                body {
                    div {
                        style = "width: 100%; text-align:center"
                        img(src = "/static/laterball_transparent.svg")
                        h2(classes = "subtitle") { +"What is Laterball?" }
                        div(classes = "center") {
                            style = "width:200px"
                            a(classes = "link", href = "./") { +"Home ↠" }
                        }
                        h3(classes = "block center") {
                            +"Love to watch football on demand? Laterball tells you which games are the best to watch this week without spoiling the score for you."
                            br {  }
                            br {  }
                            +"Currently, Laterball lists the best English Premier League games of the week, ranked by watchability."
                            br {  }
                            br {  }
                        }
                    }
                    span(classes = "subtitle center") { +"feedback: email hi at laterball dot com" }
                    span(classes = "subtitle center") { +"© ${SimpleDateFormat("YYYY").format(Date())} Laterball" }
                }
            }
        }

        get("/") {
            val ratings = repo.getRatingsForLeague(LeagueId.EPL)
            logger.info("Returning ratings: ${ratings?.joinToString{ it.toString() }}")
            call.respondHtml {
                generateHeader(this)
                body {
                    div {
                        style = "width: 100%; text-align:center"
                        img(src = "/static/laterball_transparent.svg")
                        h2(classes = "subtitle") { +"The best football games of the week, ranked" }
                        div(classes = "center") {
                            style = "width:200px"
                            a(classes = "link", href = "./about") { +"What is Laterball? ↠" }
                        }
                    }
                    br {}
                    br {}
                    div(classes = "center") {
                        style = "width: 100%; text-align:center"
                        div(classes = "lb-container") {
                            style = "max-width: 1200px"
                            ratings?.let {
                                ul(classes = "lb-ul lb-card-4") {
                                    ratings.forEach { rating ->
                                        li(classes = "lb-bar lb-border lb-round-xlarge fade-in") {
                                            a(
                                                    classes = "lb-bar-item lb-medium lb-right subtitle link",
                                                    href = "https://www.google.com/search?q=${rating.homeTeam}+vs+${rating.awayTeam}+streaming+on+demand",
                                                    target = "_blank") {
                                                +"Where to watch? ↠"
                                            }
                                            div(classes = "center") {
                                                img(classes = "lb-bar-item lb-circle lb-hide-small", src = rating.homeLogo) {
                                                    style = "width:85px"
                                                }
                                                div(classes = "lb-bar-item") {
                                                    span(classes = "lb-xxlarge  match") { +"${rating.homeTeam} vs ${rating.awayTeam}" }
                                                }
                                                img(classes = "lb-bar-item lb-circle lb-hide-small", src = rating.awayLogo) {
                                                    style = "width:85px"
                                                }
                                            }
                                            br {}
                                            div {
                                                for (i in 1..rating.rating.toInt()) {
                                                    if (i == rating.rating.toInt() && i % 2 != 0) {
                                                        img(src = "/static/half_star.svg") { style = "height:50px" }
                                                    } else if (i % 2 == 0) {
                                                        img(src = "/static/star.svg") { style = "height:50px" }
                                                    }
                                                }
                                                for (i in (rating.rating.toInt() + 1)..10) {
                                                    if (i % 2 == 0) {
                                                        img(src = "/static/empty_star.svg") { style = "height:50px" }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } ?: h2(classes = "subtitle") {
                                style = "width: 100%; text-align:center"
                                +"No recent games, check back later!"
                            }
                        }
                    }
                    span(classes = "subtitle center") { +"© ${SimpleDateFormat("YYYY").format(Date())} Laterball" }
                }
            }
        }

        get("/about") {

        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }
    }
}

