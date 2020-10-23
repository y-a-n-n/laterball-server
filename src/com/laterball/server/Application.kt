package com.laterball.server

import com.laterball.server.api.DataApi
import com.laterball.server.model.LeagueId
import com.laterball.server.repository.RatingsRepository
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.auth.*
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
    val api by inject<DataApi>()

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

    // Init data, slowly to not hit api request limits
    api.requestDelay = 4000
    repo.getRatingsForLeague(LeagueId.EPL)
    api.requestDelay = null

    routing {
        get("/ratings") {
            val ratings = repo.getRatingsForLeague(LeagueId.EPL)
            val analyticsTag = System.getenv("LATERBALL_ANALYTICS_TAG")
            call.respondHtml {
                head {
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
                body {
                    div {
                        style = "width: 100%; text-align:center"
                        img(src = "/static/laterball_transparent.svg")
                        h2(classes = "subtitle") { +"The best football games of the week, ranked" }
                        a(classes = "subtitle", href = "./static/about.html") { +"What is Laterball? ↠" }
                    }
                    br {}
                    br {}
                    div(classes = "lb-container") {
                        style = "max-width: 1200px"
                        ratings?.let {
                            ul(classes = "lb-ul lb-card-4") {
                                ratings.forEachIndexed { index, rating ->
                                    li(classes = "lb-bar lb-border lb-round-xlarge") {
                                        a(classes = "lb-bar-item lb-medium lb-right subtitle", href = "") { +"Where to watch ↠?"  }
                                        img(classes = "lb-bar-item lb-circle lb-hide-small", src = rating.homeLogo) {
                                            style = "width:85px"
                                        }
                                        div(classes = "lb-bar-item") {
                                            span(classes = "lb-large  block") { +"${rating.homeTeam} vs ${rating.awayTeam}" }
                                            span(classes = "block") { +"#${index + 1} match this week" }
                                        }
                                        img(classes = "lb-bar-item lb-circle lb-hide-small", src = rating.awayLogo) {
                                            style = "width:85px"
                                        }
                                    }
                                }
                            }
                        } ?: h2(classes = "subtitle") {
                            style = "width: 100%; text-align:center"
                            +"No recent games, check back later!"
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

