package com.example

import com.example.data.RoundEvaluator
import com.example.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncAndScoringTest {

    @Test
    fun evaluateTwoPlayers_awardsTenForUniqueAndFiveForSharedAndZeroForInvalid() {
        val player1 = Category.entries.associateWith { "" }.toMutableMap().apply {
            this[Category.BOY_NAME] = "آرش" // unique valid -> 10
            this[Category.GIRL_NAME] = "آیدا" // shared with player2 -> 5
            this[Category.FRUIT] = "انار" // player1 valid, player2 invalid letter -> 10 to 0
            this[Category.CITY] = "شیراز" // wrong letter for "ا" -> 0
        }
        val player2 = Category.entries.associateWith { "" }.toMutableMap().apply {
            this[Category.BOY_NAME] = "امیر" // unique valid -> 10
            this[Category.GIRL_NAME] = "آیدا" // shared -> 5
            this[Category.FRUIT] = "سیب" // wrong letter for "ا" -> 0
            this[Category.CITY] = "اصفهان" // valid -> 10
        }

        val evals = RoundEvaluator.evaluateTwoPlayers(
            targetLetter = "ا",
            localAnswers = player1,
            opponentAnswers = player2,
        )

        val boy = evals.first { it.category == Category.BOY_NAME }
        assertEquals(10, boy.playerScore)
        assertEquals(10, boy.botScore)
        assertFalse(boy.isSharedAnswer)

        val girl = evals.first { it.category == Category.GIRL_NAME }
        assertEquals(5, girl.playerScore)
        assertEquals(5, girl.botScore)
        assertTrue(girl.isSharedAnswer)

        val fruit = evals.first { it.category == Category.FRUIT }
        assertEquals(10, fruit.playerScore)
        assertEquals(0, fruit.botScore)

        val city = evals.first { it.category == Category.CITY }
        assertEquals(0, city.playerScore)
        assertEquals(10, city.botScore)

        // Symmetric check: reversed arguments produce exact swapped scores
        val reversed = RoundEvaluator.evaluateTwoPlayers(
            targetLetter = "ا",
            localAnswers = player2,
            opponentAnswers = player1,
        )
        assertEquals(evals.sumOf { it.playerScore }, reversed.sumOf { it.botScore })
        assertEquals(evals.sumOf { it.botScore }, reversed.sumOf { it.playerScore })
    }
}
