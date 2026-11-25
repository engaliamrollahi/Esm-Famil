package com.example

import com.example.model.AppStats
import com.example.model.HonorBadge
import com.example.model.LevelProgression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {

    @Test
    fun `level progression scales accurately with wins`() {
        assertEquals(1, LevelProgression.fromWins(0).level)
        assertEquals(1, LevelProgression.fromWins(1).level)
        assertEquals(2, LevelProgression.fromWins(2).level)
        assertEquals(3, LevelProgression.fromWins(5).level)
        assertEquals(5, LevelProgression.fromWins(11).level)
        assertEquals(8, LevelProgression.fromWins(31).level)
        assertTrue(LevelProgression.fromWins(31).isMaxLevel)
    }

    @Test
    fun `honor badges unlock according to wins and milestones`() {
        val beginner = AppStats(totalGamesPlayed = 1, totalWins = 0, totalCareerScore = 100)
        assertTrue(beginner.unlockedBadges.isEmpty())

        val fiveWins = AppStats(
            totalGamesPlayed = 6,
            totalWins = 5,
            totalCareerScore = 1050,
            bestWinStreak = 3,
            hardWins = 1,
        )
        assertTrue(HonorBadge.FIRST_VICTORY in fiveWins.unlockedBadges)
        assertTrue(HonorBadge.RISING_STAR in fiveWins.unlockedBadges)
        assertTrue(HonorBadge.FIVE_WINS_WARRIOR in fiveWins.unlockedBadges)
        assertTrue(HonorBadge.STREAK_CHAMPION in fiveWins.unlockedBadges)
        assertTrue(HonorBadge.HARD_MODE_CONQUEROR in fiveWins.unlockedBadges)
        assertTrue(HonorBadge.CAREER_SCORER in fiveWins.unlockedBadges)
        assertFalse(HonorBadge.TEN_WINS_MASTER in fiveWins.unlockedBadges)
    }
}
