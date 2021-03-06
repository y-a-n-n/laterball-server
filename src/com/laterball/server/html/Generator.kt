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

    private fun generateAdsense(head: HEAD) {
        val adsenseTag = config.propertyOrNull("ktor.analytics.adsense")?.getString() ?: ""
        head.script(src = "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js") {
            attributes.putIfAbsent("async", "true")
            attributes.putIfAbsent("data-ad-client", "ca-pub-$adsenseTag")
        }
    }

    private fun generateHeader(html: HTML) {
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

    fun generateForLeague(html: HTML, leagueId: LeagueId, sortByDate: Boolean) {
        val ratings = repo.getRatingsForLeague(leagueId, sortByDate)
        logger.info("Returning ratings: ${ratings?.joinToString{ it.toString() }}, sortByDate: $sortByDate")
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
                    style = "width:600px"
                    LeagueId.values().forEach {
                        h4(classes = "center") {
                            a(classes = if (it == leagueId) "static" else "link", href = "./${it.path}") {
                                style = "margin-left: 30px; margin-right: 30px;"
                                +it.title
                            }
                        }
                    }
                }
            }
            br {}
            br {}
            div(classes = "center") {
                style = "width:500px"
                a(classes = if (sortByDate) "link" else "static", href = "./${leagueId.path}") {
                    style = "margin-left: 15px; margin-right: 15px;"
                    +"↡ Highest Rated"
                }
                a(classes = if (sortByDate) "static" else "link", href = "./${leagueId.path}?sort=date") {
                    style = "margin-left: 15px; margin-right: 15px;"
                    +"Most Recent ↡"
                }
            }
            br {}
            div(classes = "center") {
                style = "width: 100%; text-align:center"
                div(classes = "lb-container") {
                    style = "max-width: 1200px"
                    if (!ratings.isNullOrEmpty()) {
                        val format = SimpleDateFormat("EEEE, d MMMM")
                        ul(classes = "lb-ul lb-card-4") {
                            ratings.forEach { rating ->
                                li(classes = "lb-bar lb-border lb-round-xlarge fade-in") {
                                    a(
                                            classes = "lb-bar-item lb-medium lb-right subtitle link",
                                            href = "https://www.google.com/search?q=${rating.homeTeam}+vs+${rating.awayTeam}+streaming+on+demand",
                                            target = "_blank") {
                                        +"↠"
                                    }
                                    div(classes = "center") {
                                        img(classes = "lb-bar-item lb-circle lb-hide-small", src = rating.homeLogo) {
                                            style = "height:100%"
                                        }
                                        div(classes = "lb-bar-item") {
                                            span(classes = "lb-xxlarge match") { +"${rating.homeTeam} vs ${rating.awayTeam}" }
                                            br {}
                                            span(classes = "center subtitle") { +format.format(rating.date) }
                                            br {}
                                            h5 (classes = "fade-in subtitle tooltip") {
                                                +"Show score"
                                                span(classes = "tooltiptext") { +rating.score }
                                            }
                                        }
                                        img(classes = "lb-bar-item lb-circle lb-hide-small", src = rating.awayLogo) {
                                            style = "height:100%"
                                        }
                                    }
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
                                    br {}
                                }
                            }
                        }
                    } else {
                        h2(classes = "subtitle") {
                            style = "width: 100%; text-align:center"
                            +"No recent games, check back later!"
                        }
                    }
                }
            }
            span(classes = "subtitle center") { +"© ${SimpleDateFormat("YYYY").format(Date())} Laterball" }
        }
    }

    fun generateAbout(html: HTML) {
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
                h3(classes = "subtitle center") {
                    +"Love to watch football on demand? Laterball tells you which games are the best to watch this week without spoiling the score for you."
                    br {}
                    br {}
                    +"Currently, Laterball lists the best English Premier League and Champions League games of the week, ranked by watchability."
                    br {}
                    br {}
                }
            }
            span(classes = "subtitle center") { +"feedback: email hi at laterball dot com" }
            span(classes = "subtitle center") {
                a(href = "https://twitter.com/laterball", target = "_blank") {
                    style = "margin-right: 10px;"
                    img(src = "/static/twitter.png") { style = "width: 50px; height: 50px"}
                }
                a(href = "https://github.com/y-a-n-n/laterball-server", target = "_blank") {
                    style = "margin-left: 10px;"
                    img(src = "/static/github.png") { style = "width: 50px; height: 50px"}
                }
            }
            span(classes = "subtitle center") { +"© ${SimpleDateFormat("YYYY").format(Date())} Laterball" }
            span(classes = "subtitle center") { +"Version 2.2.5" }
        }
    }
}