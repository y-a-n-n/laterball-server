package com.laterball.server.api.model

data class Event(
    val assist: Any?,
    val assist_id: Any?,
    val comments: Any?,
    val detail: String,
    val elapsed: Int,
    val elapsed_plus: Any?,
    val player: String,
    val player_id: Int,
    val teamName: String,
    val team_id: Int,
    val type: String
)