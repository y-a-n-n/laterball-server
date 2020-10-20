package com.laterball.server.api.model

import com.google.gson.annotations.SerializedName

data class Statistics(
    @SerializedName("Ball Possession")
    val BallPossession: HomeAwayStat,
    @SerializedName("Blocked Shots")
    val BlockedShots: HomeAwayStat,
    @SerializedName("Corner Kicks")
    val CornerKicks: HomeAwayStat,
    @SerializedName("Fouls")
    val Fouls: HomeAwayStat,
    @SerializedName("Goalkeeper Saves")
    val GoalkeeperSaves: HomeAwayStat,
    @SerializedName("Offsides")
    val Offsides: HomeAwayStat,
    @SerializedName("Passes %")
    val PassesPercent: HomeAwayStat,
    @SerializedName("Passes accurate")
    val PassesAccurate: HomeAwayStat,
    @SerializedName("Red Cards")
    val RedCards: HomeAwayStat,
    @SerializedName("Shots insidebox")
    val ShotsInsidebox: HomeAwayStat,
    @SerializedName("Shots off Goal")
    val ShotsOffGoal: HomeAwayStat,
    @SerializedName("Shots on Goal")
    val ShotsOnGoal: HomeAwayStat,
    @SerializedName("Shots outsidebox")
    val ShotsOutsidebox: HomeAwayStat,
    @SerializedName("Total Shots")
    val TotalShots: HomeAwayStat,
    @SerializedName("Total passes")
    val TotalPasses: HomeAwayStat,
    @SerializedName("Yellow Cards")
    val YellowCards: HomeAwayStat
)