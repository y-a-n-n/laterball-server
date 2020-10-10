package com.laterball.server.api.model

data class Fixture(
    val awayTeam: FixtureTeam,
    val elapsed: Int,
    val event_date: String,
    val event_timestamp: Int,
    val firstHalfStart: Int,
    val fixture_id: Int,
    val goalsAwayTeam: Int,
    val goalsHomeTeam: Int,
    val homeTeam: FixtureTeam,
    val league: League,
    val league_id: Int,
    val referee: Any?,
    val round: String,
    val score: Score,
    val secondHalfStart: Int,
    val status: String,
    val statusShort: String,
    val venue: String
)