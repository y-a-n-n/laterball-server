package com.laterball.server.model

enum class LeagueId(val id: Int, val title: String, val path: String) {
    EPL(2790, "English Premier League", "epl"),
    CHAMPIONS_LEAGUE(2771, "Champions League", "champions_league")
}