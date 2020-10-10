package com.laterball.server.api.model

data class Bookmaker(
    val bets: List<Bet>,
    val bookmaker_id: Int,
    val bookmaker_name: String
)