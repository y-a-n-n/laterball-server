package com.laterball.server.api.model

data class Bet(
    val label_id: Int,
    val label_name: String,
    val values: List<Value>
)