package com.example.data

import com.example.model.BotDifficulty
import com.example.model.Category
import com.example.model.CategoryEvaluation
import com.example.model.PersianTextHelper
import kotlin.random.Random

object RoundEvaluator {

    fun evaluateAgainstBot(
        targetLetter: String,
        playerAnswers: Map<Category, String>,
        difficulty: BotDifficulty,
        random: Random = Random.Default,
    ): List<CategoryEvaluation> {
        val botAnswers = Category.entries.associateWith { category ->
            val botCandidates = WordBank.getEntries(targetLetter, category)
            if (botCandidates.isNotEmpty() && random.nextFloat() <= difficulty.fillRate) {
                botCandidates.random(random)
            } else {
                ""
            }
        }
        return evaluateTwoPlayers(
            targetLetter = targetLetter,
            localAnswers = playerAnswers,
            opponentAnswers = botAnswers,
        )
    }

    fun evaluateTwoPlayers(
        targetLetter: String,
        localAnswers: Map<Category, String>,
        opponentAnswers: Map<Category, String>,
    ): List<CategoryEvaluation> {
        return Category.entries.map { category ->
            val rawLocal = localAnswers[category].orEmpty().trim()
            val rawOpponent = opponentAnswers[category].orEmpty().trim()

            val localStartsCorrect = rawLocal.isNotBlank() &&
                PersianTextHelper.startsWithTargetLetter(rawLocal, targetLetter)
            val localKnownInBank = localStartsCorrect &&
                WordBank.isKnownWord(targetLetter, category, rawLocal)
            val localValid = localStartsCorrect &&
                (localKnownInBank || PersianTextHelper.isPlausibleWord(rawLocal, targetLetter))

            val oppStartsCorrect = rawOpponent.isNotBlank() &&
                PersianTextHelper.startsWithTargetLetter(rawOpponent, targetLetter)
            val oppKnownInBank = oppStartsCorrect &&
                WordBank.isKnownWord(targetLetter, category, rawOpponent)
            val oppValid = oppStartsCorrect &&
                (oppKnownInBank || PersianTextHelper.isPlausibleWord(rawOpponent, targetLetter))

            val isShared = localValid && oppValid &&
                PersianTextHelper.normalize(rawLocal) == PersianTextHelper.normalize(rawOpponent)

            val (localScore, oppScore) = computeScores(
                localValid = localValid,
                opponentValid = oppValid,
                isShared = isShared,
            )

            CategoryEvaluation(
                category = category,
                playerAnswer = rawLocal,
                botAnswer = rawOpponent,
                isPlayerValid = localValid,
                isKnownInWordBank = localKnownInBank,
                isSharedAnswer = isShared,
                playerScore = localScore,
                botScore = oppScore,
                startsWithCorrectLetter = localStartsCorrect,
            )
        }
    }

    fun computeScores(
        localValid: Boolean,
        opponentValid: Boolean,
        isShared: Boolean,
    ): Pair<Int, Int> {
        return when {
            localValid && opponentValid && isShared -> 5 to 5
            localValid && opponentValid -> 10 to 10
            localValid && !opponentValid -> 10 to 0
            !localValid && opponentValid -> 0 to 10
            else -> 0 to 0
        }
    }
}
