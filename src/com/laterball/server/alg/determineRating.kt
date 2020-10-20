package com.laterball.server.alg

import com.laterball.server.api.model.*
import com.laterball.server.model.Rating
import java.util.Collections.max
import kotlin.math.abs

fun determineRating(fixture: Fixture, odd: Bet, stats: Statistics, events: ApiFixtureEvents): Rating {
 val totalGoalts = fixture.goalsAwayTeam + fixture.goalsHomeTeam
 val homeTeamId = fixture.homeTeam.team_id
 return Rating("", "", "", "", "", 3, "")
}

fun ApiFixtureEvents.biggestLead(homeTeamId: Int): Int {
 var homeLead = 0
 var maxLead = 0
 this.events
  .filter { it.type == "Goal" }
  .sortedBy { it.elapsed }
  .forEach {
   if (it.team_id == homeTeamId) {
    homeLead++
   } else {
    homeLead--
   }
  }
 return maxLead
}