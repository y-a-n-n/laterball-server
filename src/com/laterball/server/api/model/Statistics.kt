package com.laterball.server.api.model

data class Statistics(
    val Ball Possession: BallPossession,
    val Blocked Shots: BlockedShots,
    val Corner Kicks: CornerKicks,
    val Fouls: Fouls,
    val Goalkeeper Saves: GoalkeeperSaves,
    val Offsides: Offsides,
    val Passes %: Passes,
    val Passes accurate: PassesAccurate,
    val Red Cards: RedCards,
    val Shots insidebox: ShotsInsidebox,
    val Shots off Goal: ShotsOffGoal,
    val Shots on Goal: ShotsOnGoal,
    val Shots outsidebox: ShotsOutsidebox,
    val Total Shots: TotalShots,
    val Total passes: TotalPasses,
    val Yellow Cards: YellowCards
)