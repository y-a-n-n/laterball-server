package com.laterball.server.html

import com.laterball.server.model.LeagueId
import com.laterball.server.repository.RatingsRepository
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import kotlinx.html.*
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

@KtorExperimentalAPI
class Generator(private val repo: RatingsRepository, private val config: ApplicationConfig) {

    private val logger = LoggerFactory.getLogger(Generator::class.java)

    private val generateAdsense = { head: HEAD ->
        val adsenseTag = config.propertyOrNull("ktor.analytics.adsense")?.getString() ?: ""
        head.script(src = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js") {
            attributes.putIfAbsent("async", "true")
            attributes.putIfAbsent("data-ad-client", "ca-pub-$adsenseTag")
        }
    }

    private val generateHeader = { html: HTML ->
        val analyticsTag = config.propertyOrNull("ktor.analytics.tag")?.getString() ?: ""
        html.head {
            generateAdsense(this)
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

    val generateForLeague = { html: HTML, leagueId: LeagueId ->
        val ratings = repo.getRatingsForLeague(leagueId)
        logger.info("Returning ratings: ${ratings?.joinToString{ it.toString() }}")
        generateHeader(html)
        html.body {
            div {
                style = "width: 100%; text-align:center"
                img(src = "/static/laterball_transparent.svg")
                h2(classes = "subtitle") { +"The best football games of the week, ranked by watchability" }
                div(classes = "center") {
                    style = "width:200px"
                    a(classes = "link", href = "./about") { +"What is Laterball? ↠" }
                }
                div(classes = "center") {
                    style = "width:500px"
                    LeagueId.values().forEach {
                        a(classes = if (it == leagueId) "static" else "link", href = "./${it.path}") {
                            style = "margin-left: 15px; margin-right: 15px;"
                            +it.title
                        }
                    }
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
                                        var starsAdded = 0
                                        for (i in 1..rating.rating.toInt()) {
                                            if (i == rating.rating.toInt() && i % 2 != 0) {
                                                img(src = "/static/half_star.svg") { style = "height:50px" }
                                                starsAdded++
                                            } else if (i % 2 == 0) {
                                                img(src = "/static/star.svg") { style = "height:50px" }
                                                starsAdded++
                                            }
                                        }
                                        for (i in (starsAdded..4)) {
                                            img(src = "/static/empty_star.svg") { style = "height:50px" }
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

    val generateAbout = { html: HTML ->
        generateHeader(html)
        html.body {
            div {
                style = "width: 100%; text-align:center"
                img(src = "/static/laterball_transparent.svg")
                h2(classes = "subtitle") { +"What is Laterball?" }
                div(classes = "center") {
                    style = "width:200px"
                    a(classes = "link", href = "./") { +"↞ Home" }
                }
                h3(classes = "block center") {
                    +"Love to watch football on demand? Laterball tells you which games are the best to watch this week without spoiling the score for you."
                    br {  }
                    br {  }
                    +"Currently, Laterball lists the best English Premier League and Champions League games of the week, ranked by watchability."
                    br {  }
                    br {  }
                }
            }
            span(classes = "subtitle center") { +"feedback: email hi at laterball dot com" }
            span(classes = "subtitle center") {
                a(href = "https://twitter.com/laterball") {
                    img(src = "/static/twitter.jpg") { style = "width: 50px; height: 50px"}
                }
            }
            span(classes = "subtitle center") { +"© ${SimpleDateFormat("YYYY").format(Date())} Laterball" }
            span(classes = "subtitle center") { +"Version 2.0.4" }
        }
    }
}