package com.laterball.server.alg

import com.laterball.server.api.model.*
import com.laterball.server.model.Rating
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

private const val UPSET_ODDS_MARGIN = 2.0
private const val UPSET_FACTOR = 7.0f
private const val GOALS_FACTOR = 50.0f
private const val YELLOW_FACTOR = 5.0f
private const val RED_FACTOR = 15.0f
private const val COMEBACK_FACTOR = 20.0f
private const val LONG_SHOTS_FACTOR = 0.1f
private const val CLOSE_SHOTS_FACTOR = 0.2f

fun determineRating(fixture: Fixture, odd: Bet, stats: Statistics, events: ApiFixtureEvents): Rating {

 val homeWinValue = odd.values.find { it.value == "Home" }?.odd?.toFloat() ?: 0f
 val awayWinValue = odd.values.find { it.value == "Away" }?.odd?.toFloat() ?: 0f

 // If it's a big win for favourites it's less interesting so their goals should count less
 val isFavouriteWin = (awayWinValue - homeWinValue > UPSET_ODDS_MARGIN && fixture.goalsHomeTeam >= fixture.goalsAwayTeam) ||
         (homeWinValue - awayWinValue > UPSET_ODDS_MARGIN && fixture.goalsAwayTeam >= fixture.goalsHomeTeam)
 val winningMargin = abs(fixture.goalsAwayTeam - fixture.goalsHomeTeam)
 var homeGoalsFactor = 1f
 var awayGoalsFactor = 1f
 if (winningMargin >= 4 && isFavouriteWin && min(fixture.goalsAwayTeam, fixture.goalsHomeTeam) < 2) {
  if (homeWinValue < awayWinValue) {
   homeGoalsFactor = 0.75f
  } else {
   awayGoalsFactor = 0.75f
  }
 }

 val totalGoals = awayGoalsFactor * fixture.goalsAwayTeam + homeGoalsFactor * fixture.goalsHomeTeam
 val closeShots = stats.ShotsInsidebox.sumStat() * CLOSE_SHOTS_FACTOR
 val longShots = stats.ShotsOutsidebox.sumStat() * LONG_SHOTS_FACTOR
 val goalStat = max(totalGoals, + closeShots + longShots)

 val totalReds = stats.RedCards.sumStat()
 val totalYellows = stats.YellowCards.sumStat()

 val isUpset = (homeWinValue - awayWinValue > UPSET_ODDS_MARGIN && fixture.goalsHomeTeam >= fixture.goalsAwayTeam) ||
         (awayWinValue - homeWinValue > UPSET_ODDS_MARGIN && fixture.goalsAwayTeam >= fixture.goalsHomeTeam)
 val upsetFactor = if (isUpset) {
   UPSET_FACTOR * abs(homeWinValue - awayWinValue) * (1 + abs(fixture.goalsAwayTeam - fixture.goalsHomeTeam))
 } else {
  0f
 }

 val homeTeamId = fixture.homeTeam.team_id
 val leadInfo = events.leadInfo(homeTeamId)
 val swing = abs(leadInfo.first - leadInfo.second)
 val comeback = sign(leadInfo.first.toFloat()) != sign(leadInfo.second.toFloat())
 // A big swing in score is fun and a comeback is even better
 val swingFactor = swing * COMEBACK_FACTOR * if (comeback) 2f else 1f
 val averagePassAccuracy = stats.PassesPercent.averagePercent() / 100f
 val rating = (upsetFactor +
         GOALS_FACTOR * totalGoals +
         RED_FACTOR * totalReds.toFloat() +
         YELLOW_FACTOR * totalYellows.toFloat() +
         swingFactor) * averagePassAccuracy // Condition quality of play on pass accuracy

 val date = try {
  Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(fixture.event_date)))
 } catch (e: Exception) {
  Date()
 }

 return Rating(
  fixture.fixture_id,
  fixture.homeTeam.team_name,
  fixture.awayTeam.team_name,
  date,
  fixture.homeTeam.logo,
  fixture.awayTeam.logo,
  rating,
  "${fixture.goalsHomeTeam} - ${fixture.goalsAwayTeam}",
  goalStat
 )
}

private fun HomeAwayStat.sumStat(): Int {
 return try { home.toInt() + away.toInt() } catch (e: Exception) { 0 }
}

private fun HomeAwayStat.averagePercent(): Int {
 val homePerc = home.replace("%", "")
 val awayPerc = away.replace("%", "")
 return try { (homePerc.toInt() + awayPerc.toInt()) / 2 } catch (e: Exception) { 0 }
}

fun ApiFixtureEvents.leadInfo(homeTeamId: Int): Triple<Int, Int, Int> {
 var homeLead = 0
 var maxLead = 0
 var minLead = Int.MAX_VALUE
 this.events
  .filter { it.type == "Goal" }
  .sortedBy { it.elapsed }
  .forEach {
   if (it.team_id == homeTeamId) {
    homeLead++
    maxLead = max(maxLead, homeLead)
   } else {
    homeLead--
    minLead = min(minLead, homeLead)
   }
  }
 return Triple(minLead, maxLead, homeLead)
}