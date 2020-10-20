package com.laterball.server.repository

import com.laterball.server.api.DataApiMock
import com.laterball.server.api.model.*
import com.laterball.server.com.laterball.server.repository.ClockMock
import com.laterball.server.model.LeagueId
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class FixtureRepositoryTest {

    private lateinit var dataApiMock: DataApiMock
    private lateinit var clockMock: ClockMock
    private lateinit var fixtureRepository: FixtureRepository

    @Before
    fun setUp() {
        clockMock = ClockMock()
        dataApiMock = DataApiMock()
        fixtureRepository = FixtureRepository(dataApiMock, clockMock)
    }

    @Test
    fun testRequestNull() {
        assertNull(fixtureRepository.getFixturesForLeague(LeagueId.EPL))
    }

    @Test
    fun testCached() {
        dataApiMock.testFixtures = ApiFixtureList(listOf(randomFixture, randomFixture), 2)
        assertEquals(fixtureRepository.getFixturesForLeague(LeagueId.EPL)!!.fixtures.size, 2)
        dataApiMock.testFixtures = null
        assertEquals(fixtureRepository.getFixturesForLeague(LeagueId.EPL)!!.fixtures.size, 2)
        clockMock.time += 86400001
        assertNull(fixtureRepository.getFixturesForLeague(LeagueId.EPL))
    }

    private val randomFixture: Fixture get() {
        return Fixture(
            FixtureTeam(Math.random().toString(), (1000 * Math.random()).toInt(), Math.random().toString()),
            (1000 * Math.random()).toInt(),
            Date().toString(),
            (1000 * Math.random()).toInt(),
            (1000 * Math.random()).toInt(),
            (1000 * Math.random()).toInt(),
            (1000 * Math.random()).toInt(),
            (1000 * Math.random()).toInt(),
            FixtureTeam(Math.random().toString(), (1000 * Math.random()).toInt(), Math.random().toString()),
            League(Math.random().toString(), Math.random().toString(), Math.random().toString(), Math.random().toString()),
            LeagueId.EPL.id,
            (1000 * Math.random()).toInt(),
            Math.random().toString(),
            Score(Math.random().toString(), Math.random().toString(), Math.random().toString(), Math.random().toString()),
            (1000 * Math.random()).toInt(),
            Math.random().toString(),
            Math.random().toString(),
            Math.random().toString()
        )
    }
}