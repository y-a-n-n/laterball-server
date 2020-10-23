package com.laterball.server.alg

import com.laterball.server.api.model.ApiFixtureEvents
import com.laterball.server.api.model.Bet
import com.laterball.server.api.model.Fixture
import com.laterball.server.api.model.Statistics
import com.laterball.server.model.Rating
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

private const val UPSET_ODDS_MARGIN = 2.0
private const val UPSET_FACTOR = 7.0f
private const val GOALS_FACTOR = 50.0f
private const val YELLOW_FACTOR = 5.0f
private const val RED_FACTOR = 10.0f
private const val COMEBACK_FACTOR = 20.0

fun determineRating(fixture: Fixture, odd: Bet, stats: Statistics, events: ApiFixtureEvents): Rating {
 val totalGoals = fixture.goalsAwayTeam + fixture.goalsHomeTeam
 val totalReds = try { stats.RedCards.away.toInt() + stats.RedCards.home.toInt() } catch (e: Exception) { 0 }
 val totalYellows = try { stats.YellowCards.away.toInt() + stats.YellowCards.home.toInt() } catch (e: Exception) { 0 }
 val homeWinValue = odd.values.find { it.value == "Home" }?.odd?.toFloat() ?: 0f
 val awayWinValue = odd.values.find { it.value == "Away" }?.odd?.toFloat() ?: 0f
 val isUpset = (homeWinValue - awayWinValue > UPSET_ODDS_MARGIN && fixture.goalsHomeTeam >= fixture.goalsAwayTeam) ||
         (awayWinValue - homeWinValue > UPSET_ODDS_MARGIN && fixture.goalsAwayTeam >= fixture.goalsHomeTeam)
 val upsetFactor = if (isUpset) {
   UPSET_FACTOR * abs(homeWinValue - awayWinValue) * (1 + abs(fixture.goalsAwayTeam - fixture.goalsHomeTeam))
 } else {
  0f
 }
 val homeTeamId = fixture.homeTeam.team_id
 val leadInfo = events.leadInfo(homeTeamId)
 val swing = abs(leadInfo.first) + abs(leadInfo.second)
 val comeback = sign(leadInfo.first.toFloat()) != sign(leadInfo.second.toFloat())
 // A big swing in score is fun and a comeback is even better
 val swingFactor = swing * COMEBACK_FACTOR * if (comeback) 2f else 1f
 val rating = upsetFactor +
         GOALS_FACTOR * totalGoals +
         RED_FACTOR * totalReds.toFloat() +
         YELLOW_FACTOR * totalYellows.toFloat() +
         COMEBACK_FACTOR * swingFactor
 return Rating(
  fixture.homeTeam.team_name,
  fixture.awayTeam.team_name,
  fixture.event_date,
  fixture.homeTeam.logo,
  fixture.awayTeam.logo,
  rating.toInt(),
  "${fixture.goalsHomeTeam} - ${fixture.goalsAwayTeam}"
 )
}

fun ApiFixtureEvents.leadInfo(homeTeamId: Int): Triple<Int, Int, Int> {
 var homeLead = 0
 var maxLead = 0
 var minLead = 0
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