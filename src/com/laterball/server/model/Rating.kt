package com.laterball.server.model

class Rating(
    val homeTeam: String,
    val awayTeam: String,
    val date: String,
    val homeLogo: String,
    val awayLogo: String,
    var rating: Float,
    val score: String,
    val totalGoals: Int
)