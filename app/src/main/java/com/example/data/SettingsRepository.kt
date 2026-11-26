package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppStats
import com.example.model.BotDifficulty
import com.example.model.GameMode
import com.example.model.GameSettings
import com.example.model.HonorBadge
import com.example.model.LetterPickMode
import com.example.model.MatchProgressionDelta
import com.example.model.PERSIAN_ALPHABET
import com.example.model.PlayerAvatar
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "esm_famil_prefs")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SELECTED_LETTERS = stringSetPreferencesKey("selected_letters")
        val TIMER_SECONDS = intPreferencesKey("timer_seconds")
        val ROUNDS_COUNT = intPreferencesKey("rounds_count")
        val BOT_DIFFICULTY = stringPreferencesKey("bot_difficulty")
        val LETTER_PICK_MODE = stringPreferencesKey("letter_pick_mode")
        val MANUAL_LETTER = stringPreferencesKey("manual_letter")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")

        val PLAYER_NAME = stringPreferencesKey("player_name")
        val PLAYER_AVATAR = stringPreferencesKey("player_avatar")
        val TOTAL_GAMES = intPreferencesKey("total_games")
        val TOTAL_WINS = intPreferencesKey("total_wins")
        val TOTAL_CAREER_SCORE = intPreferencesKey("total_career_score")
        val HIGH_SCORE = intPreferencesKey("high_score")
        val CURRENT_WIN_STREAK = intPreferencesKey("current_win_streak")
        val BEST_WIN_STREAK = intPreferencesKey("best_win_streak")
        val HARD_WINS = intPreferencesKey("hard_wins")
        val PERFECT_ROUNDS = intPreferencesKey("perfect_rounds")
    }

    val settingsFlow: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        val rawLetters = prefs[Keys.SELECTED_LETTERS]
        val validLetters = if (rawLetters.isNullOrEmpty()) {
            PERSIAN_ALPHABET.toSet()
        } else {
            rawLetters.filter { it in PERSIAN_ALPHABET }.toSet().ifEmpty { PERSIAN_ALPHABET.toSet() }
        }

        GameSettings(
            selectedLetters = validLetters,
            timerSeconds = prefs[Keys.TIMER_SECONDS] ?: 120,
            roundsCount = (prefs[Keys.ROUNDS_COUNT] ?: 3).coerceIn(1, 5),
            botDifficulty = prefs[Keys.BOT_DIFFICULTY]?.let { name ->
                BotDifficulty.entries.find { it.name == name }
            } ?: BotDifficulty.MEDIUM,
            letterPickMode = prefs[Keys.LETTER_PICK_MODE]?.let { name ->
                LetterPickMode.entries.find { it.name == name }
            } ?: LetterPickMode.RANDOM_FROM_SELECTED,
            manualLetter = prefs[Keys.MANUAL_LETTER]?.takeIf { it in PERSIAN_ALPHABET } ?: "م",
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            themeMode = prefs[Keys.THEME_MODE]?.let { name ->
                ThemeMode.entries.find { it.name == name }
            } ?: ThemeMode.SYSTEM,
        )
    }

    val statsFlow: Flow<AppStats> = context.dataStore.data.map { prefs ->
        readStatsFromPrefs(prefs)
    }

    private fun readStatsFromPrefs(prefs: Preferences): AppStats {
        return AppStats(
            playerName = prefs[Keys.PLAYER_NAME]?.takeIf { it.isNotBlank() } ?: "بازیکن",
            avatar = prefs[Keys.PLAYER_AVATAR]?.let { id ->
                PlayerAvatar.entries.find { it.name == id }
            } ?: PlayerAvatar.PEN_MASTER,
            totalGamesPlayed = prefs[Keys.TOTAL_GAMES] ?: 0,
            totalWins = prefs[Keys.TOTAL_WINS] ?: 0,
            totalCareerScore = prefs[Keys.TOTAL_CAREER_SCORE] ?: 0,
            highScore = prefs[Keys.HIGH_SCORE] ?: 0,
            currentWinStreak = prefs[Keys.CURRENT_WIN_STREAK] ?: 0,
            bestWinStreak = prefs[Keys.BEST_WIN_STREAK] ?: 0,
            hardWins = prefs[Keys.HARD_WINS] ?: 0,
            perfectRounds = prefs[Keys.PERFECT_ROUNDS] ?: 0,
        )
    }

    suspend fun updateSettings(transform: (GameSettings) -> GameSettings) {
        context.dataStore.edit { prefs ->
            val currentLetters = prefs[Keys.SELECTED_LETTERS]
                ?.filter { it in PERSIAN_ALPHABET }
                ?.toSet()
                ?.ifEmpty { PERSIAN_ALPHABET.toSet() }
                ?: PERSIAN_ALPHABET.toSet()

            val current = GameSettings(
                selectedLetters = currentLetters,
                timerSeconds = prefs[Keys.TIMER_SECONDS] ?: 120,
                roundsCount = (prefs[Keys.ROUNDS_COUNT] ?: 3).coerceIn(1, 5),
                botDifficulty = prefs[Keys.BOT_DIFFICULTY]?.let { name ->
                    BotDifficulty.entries.find { it.name == name }
                } ?: BotDifficulty.MEDIUM,
                letterPickMode = prefs[Keys.LETTER_PICK_MODE]?.let { name ->
                    LetterPickMode.entries.find { it.name == name }
                } ?: LetterPickMode.RANDOM_FROM_SELECTED,
                manualLetter = prefs[Keys.MANUAL_LETTER]?.takeIf { it in PERSIAN_ALPHABET } ?: "م",
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                themeMode = prefs[Keys.THEME_MODE]?.let { name ->
                    ThemeMode.entries.find { it.name == name }
                } ?: ThemeMode.SYSTEM,
            )

            val updated = transform(current)
            prefs[Keys.SELECTED_LETTERS] = updated.selectedLetters.ifEmpty { PERSIAN_ALPHABET.toSet() }
            prefs[Keys.TIMER_SECONDS] = updated.timerSeconds
            prefs[Keys.ROUNDS_COUNT] = updated.roundsCount.coerceIn(1, 5)
            prefs[Keys.BOT_DIFFICULTY] = updated.botDifficulty.name
            prefs[Keys.LETTER_PICK_MODE] = updated.letterPickMode.name
            prefs[Keys.MANUAL_LETTER] = updated.manualLetter
            prefs[Keys.SOUND_ENABLED] = updated.soundEnabled
            prefs[Keys.THEME_MODE] = updated.themeMode.name
        }
    }

    suspend fun updateProfile(playerName: String, avatar: PlayerAvatar) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PLAYER_NAME] = playerName.trim().ifEmpty { "بازیکن" }
            prefs[Keys.PLAYER_AVATAR] = avatar.name
        }
    }

    suspend fun recordMatchResult(
        playerScore: Int,
        won: Boolean,
        difficulty: BotDifficulty,
        perfectRoundsEarned: Int,
        gameMode: GameMode = GameMode.AI,
    ): MatchProgressionDelta {
        var delta: MatchProgressionDelta? = null
        context.dataStore.edit { prefs ->
            val oldStats = readStatsFromPrefs(prefs)
            val oldLevel = oldStats.levelInfo
            val oldBadges = oldStats.unlockedBadges.toSet()

            val winBonus = if (won) 50 else 0
            val earnedCareer = playerScore.coerceAtLeast(0) + winBonus
            val nextWins = if (won) oldStats.totalWins + 1 else oldStats.totalWins
            val nextStreak = if (won) oldStats.currentWinStreak + 1 else 0
            val nextBestStreak = maxOf(oldStats.bestWinStreak, nextStreak)
            val nextHardWins = if (won && gameMode == GameMode.AI && difficulty == BotDifficulty.HARD) {
                oldStats.hardWins + 1
            } else {
                oldStats.hardWins
            }
            val nextCareerScore = oldStats.totalCareerScore + earnedCareer
            val nextHighScore = maxOf(oldStats.highScore, playerScore)
            val nextPerfectRounds = oldStats.perfectRounds + perfectRoundsEarned

            prefs[Keys.TOTAL_GAMES] = oldStats.totalGamesPlayed + 1
            prefs[Keys.TOTAL_WINS] = nextWins
            prefs[Keys.TOTAL_CAREER_SCORE] = nextCareerScore
            prefs[Keys.HIGH_SCORE] = nextHighScore
            prefs[Keys.CURRENT_WIN_STREAK] = nextStreak
            prefs[Keys.BEST_WIN_STREAK] = nextBestStreak
            prefs[Keys.HARD_WINS] = nextHardWins
            prefs[Keys.PERFECT_ROUNDS] = nextPerfectRounds

            val newStats = oldStats.copy(
                totalGamesPlayed = oldStats.totalGamesPlayed + 1,
                totalWins = nextWins,
                totalCareerScore = nextCareerScore,
                highScore = nextHighScore,
                currentWinStreak = nextStreak,
                bestWinStreak = nextBestStreak,
                hardWins = nextHardWins,
                perfectRounds = nextPerfectRounds,
            )
            val newLevel = newStats.levelInfo
            val newlyUnlocked = newStats.unlockedBadges.filterNot { it in oldBadges }

            delta = MatchProgressionDelta(
                earnedCareerScore = earnedCareer,
                winBonusPoints = winBonus,
                oldLevel = oldLevel,
                newLevel = newLevel,
                newlyUnlockedBadges = newlyUnlocked,
            )
        }
        return delta ?: MatchProgressionDelta(
            earnedCareerScore = playerScore,
            winBonusPoints = 0,
            oldLevel = AppStats().levelInfo,
            newLevel = AppStats().levelInfo,
            newlyUnlockedBadges = emptyList(),
        )
    }

    suspend fun resetStats() {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTAL_GAMES] = 0
            prefs[Keys.TOTAL_WINS] = 0
            prefs[Keys.TOTAL_CAREER_SCORE] = 0
            prefs[Keys.HIGH_SCORE] = 0
            prefs[Keys.CURRENT_WIN_STREAK] = 0
            prefs[Keys.BEST_WIN_STREAK] = 0
            prefs[Keys.HARD_WINS] = 0
            prefs[Keys.PERFECT_ROUNDS] = 0
        }
    }
}
