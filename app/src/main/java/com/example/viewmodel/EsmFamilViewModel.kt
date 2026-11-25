package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.SettingsRepository
import com.example.data.WordBank
import com.example.model.ActiveRoundState
import com.example.model.AppStats
import com.example.model.BotDifficulty
import com.example.model.Category
import com.example.model.CategoryEvaluation
import com.example.model.GameSettings
import com.example.model.LetterPickMode
import com.example.model.MatchSummary
import com.example.model.PERSIAN_ALPHABET
import com.example.model.POPULAR_LETTERS
import com.example.model.PersianTextHelper
import com.example.model.PlayerAvatar
import com.example.model.RoundResult
import com.example.model.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class EsmFamilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    val soundManager = SoundManager()

    val settings: StateFlow<GameSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameSettings())

    val stats: StateFlow<AppStats> = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppStats())

    private val _activeRound = MutableStateFlow<ActiveRoundState?>(null)
    val activeRound: StateFlow<ActiveRoundState?> = _activeRound.asStateFlow()

    private val _currentRoundResult = MutableStateFlow<RoundResult?>(null)
    val currentRoundResult: StateFlow<RoundResult?> = _currentRoundResult.asStateFlow()

    private val _completedRounds = MutableStateFlow<List<RoundResult>>(emptyList())
    val completedRounds: StateFlow<List<RoundResult>> = _completedRounds.asStateFlow()

    private val _matchSummary = MutableStateFlow<MatchSummary?>(null)
    val matchSummary: StateFlow<MatchSummary?> = _matchSummary.asStateFlow()

    private var timerJob: Job? = null
    private var matchStatsRecorded = false

    init {
        viewModelScope.launch {
            settings.collect { current ->
                soundManager.isSoundEnabled = current.soundEnabled
            }
        }
    }

    fun toggleSound() {
        val next = !settings.value.soundEnabled
        soundManager.isSoundEnabled = next
        if (next) soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(soundEnabled = next) }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
        if (enabled) soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(soundEnabled = enabled) }
        }
    }

    fun toggleLetterSelection(letter: String) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { current ->
                val updated = current.selectedLetters.toMutableSet()
                if (letter in updated) {
                    if (updated.size > 1) updated.remove(letter)
                } else {
                    updated.add(letter)
                }
                current.copy(selectedLetters = updated)
            }
        }
    }

    fun selectAllLetters() {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(selectedLetters = PERSIAN_ALPHABET.toSet()) }
        }
    }

    fun selectPopularLetters() {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(selectedLetters = POPULAR_LETTERS) }
        }
    }

    fun selectSingleLetterOnly(letter: String) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(
                    letterPickMode = LetterPickMode.MANUAL_CHOICE,
                    manualLetter = letter,
                    selectedLetters = it.selectedLetters + letter,
                )
            }
        }
    }

    fun setLetterPickMode(mode: LetterPickMode) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(letterPickMode = mode) }
        }
    }

    fun setManualLetter(letter: String) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(
                    manualLetter = letter,
                    selectedLetters = it.selectedLetters + letter,
                )
            }
        }
    }

    fun setTimerSeconds(seconds: Int) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(timerSeconds = seconds) }
        }
    }

    fun setRoundsCount(rounds: Int) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(roundsCount = rounds.coerceIn(1, 5)) }
        }
    }

    fun setBotDifficulty(difficulty: BotDifficulty) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(botDifficulty = difficulty) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateSettings { it.copy(themeMode = mode) }
        }
    }

    fun updateProfile(playerName: String, avatar: PlayerAvatar) {
        soundManager.playTap()
        viewModelScope.launch {
            repository.updateProfile(playerName, avatar)
        }
    }

    fun resetStats() {
        soundManager.playTap()
        viewModelScope.launch {
            repository.resetStats()
        }
    }

    fun startNewMatch() {
        val currentSettings = settings.value
        _completedRounds.value = emptyList()
        _currentRoundResult.value = null
        _matchSummary.value = null
        matchStatsRecorded = false

        val totalRounds = if (currentSettings.letterPickMode == LetterPickMode.MANUAL_CHOICE) {
            1
        } else {
            currentSettings.roundsCount
        }
        val firstLetter = pickNextLetter(currentSettings, emptyList())
        startRound(
            roundNumber = 1,
            totalRounds = totalRounds,
            letter = firstLetter,
            durationSeconds = currentSettings.timerSeconds,
            usedLetters = listOf(firstLetter),
        )
    }

    fun startNextRound() {
        val prevRounds = _completedRounds.value
        val activeTotal = _currentRoundResult.value?.let {
            val currentSettings = settings.value
            if (currentSettings.letterPickMode == LetterPickMode.MANUAL_CHOICE) 1 else currentSettings.roundsCount
        } ?: settings.value.roundsCount

        if (prevRounds.size >= activeTotal) {
            finalizeMatchSummary()
            return
        }

        val used = prevRounds.map { it.targetLetter }
        val nextLetter = pickNextLetter(settings.value, used)
        _currentRoundResult.value = null
        startRound(
            roundNumber = prevRounds.size + 1,
            totalRounds = activeTotal,
            letter = nextLetter,
            durationSeconds = settings.value.timerSeconds,
            usedLetters = used + nextLetter,
        )
    }

    private fun pickNextLetter(currentSettings: GameSettings, usedLetters: List<String>): String {
        if (currentSettings.letterPickMode == LetterPickMode.MANUAL_CHOICE) {
            return currentSettings.manualLetter.takeIf { it in PERSIAN_ALPHABET } ?: "م"
        }
        val pool = currentSettings.selectedLetters.filter { it in PERSIAN_ALPHABET }
            .ifEmpty { PERSIAN_ALPHABET }
        val unusedPool = pool.filterNot { it in usedLetters }
        val candidates = unusedPool.ifEmpty { pool }
        return candidates.random()
    }

    private fun startRound(
        roundNumber: Int,
        totalRounds: Int,
        letter: String,
        durationSeconds: Int,
        usedLetters: List<String>,
    ) {
        timerJob?.cancel()
        _activeRound.value = ActiveRoundState(
            roundNumber = roundNumber,
            totalRounds = totalRounds,
            targetLetter = letter,
            totalDurationSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            usedLetters = usedLetters,
        )
        soundManager.playLetterStart()

        if (durationSeconds > 0) {
            timerJob = viewModelScope.launch {
                var remaining = durationSeconds
                while (remaining > 0) {
                    delay(1000L)
                    remaining -= 1
                    _activeRound.update { state ->
                        state?.copy(remainingSeconds = remaining)
                    }
                    if (remaining in 1..10) {
                        soundManager.playTickWarning()
                    }
                }
                submitCurrentRound()
            }
        }
    }

    fun updateAnswer(category: Category, value: String) {
        _activeRound.update { current ->
            current?.copy(
                answers = current.answers.toMutableMap().apply {
                    this[category] = value
                }
            )
        }
    }

    fun submitCurrentRound() {
        timerJob?.cancel()
        val round = _activeRound.value ?: return
        soundManager.playRoundStop()

        val difficulty = settings.value.botDifficulty
        val evaluations = evaluateRound(round, difficulty)
        val result = RoundResult(
            roundNumber = round.roundNumber,
            targetLetter = round.targetLetter,
            evaluations = evaluations,
            playerRoundScore = evaluations.sumOf { it.playerScore },
            botRoundScore = evaluations.sumOf { it.botScore },
        )

        _activeRound.value = null
        _currentRoundResult.value = result
        _completedRounds.update { it + result }

        if (round.roundNumber >= round.totalRounds) {
            prepareMatchSummary()
        }
    }

    fun toggleCategoryValidity(category: Category) {
        val currentResult = _currentRoundResult.value ?: return
        soundManager.playTap()
        val updatedEvaluations = currentResult.evaluations.map { eval ->
            if (eval.category != category) {
                eval
            } else {
                // Allow toggling validity only if player typed something starting with targetLetter
                if (!eval.startsWithCorrectLetter || eval.playerAnswer.isBlank()) {
                    eval
                } else {
                    val newValid = !eval.isPlayerValid
                    val (pScore, bScore) = computeScores(
                        playerValid = newValid,
                        botHasAnswer = eval.botAnswer.isNotBlank(),
                        isShared = eval.isSharedAnswer,
                    )
                    eval.copy(
                        isPlayerValid = newValid,
                        playerScore = pScore,
                        botScore = bScore,
                    )
                }
            }
        }

        val updatedRound = currentResult.copy(
            evaluations = updatedEvaluations,
            playerRoundScore = updatedEvaluations.sumOf { it.playerScore },
            botRoundScore = updatedEvaluations.sumOf { it.botScore },
        )
        _currentRoundResult.value = updatedRound
        _completedRounds.update { list ->
            list.map { if (it.roundNumber == updatedRound.roundNumber) updatedRound else it }
        }
        if (_matchSummary.value != null) {
            prepareMatchSummary(playChime = false)
        }
    }

    fun finalizeMatchSummary() {
        prepareMatchSummary(playChime = true)
    }

    private fun prepareMatchSummary(playChime: Boolean = true) {
        val rounds = _completedRounds.value
        val playerTotal = rounds.sumOf { it.playerRoundScore }
        val botTotal = rounds.sumOf { it.botRoundScore }
        val existingDelta = _matchSummary.value?.progressionDelta
        _matchSummary.value = MatchSummary(
            rounds = rounds,
            playerTotalScore = playerTotal,
            botTotalScore = botTotal,
            progressionDelta = existingDelta,
        )
        if (!matchStatsRecorded && rounds.isNotEmpty()) {
            matchStatsRecorded = true
            val perfectCount = rounds.count { it.playerRoundScore >= 160 }
            val currentDiff = settings.value.botDifficulty
            viewModelScope.launch {
                val delta = repository.recordMatchResult(
                    playerScore = playerTotal,
                    won = playerTotal > botTotal,
                    difficulty = currentDiff,
                    perfectRoundsEarned = perfectCount,
                )
                _matchSummary.update { current ->
                    current?.copy(progressionDelta = delta)
                }
            }
        }
        if (playChime && playerTotal >= botTotal) {
            soundManager.playVictoryChime()
        }
    }

    fun abandonActiveGame() {
        timerJob?.cancel()
        _activeRound.value = null
        _currentRoundResult.value = null
        _completedRounds.value = emptyList()
        _matchSummary.value = null
    }

    private fun evaluateRound(
        round: ActiveRoundState,
        difficulty: BotDifficulty,
    ): List<CategoryEvaluation> {
        val letter = round.targetLetter
        return Category.entries.map { category ->
            val rawPlayer = round.answers[category].orEmpty().trim()
            val startsCorrect = rawPlayer.isNotBlank() &&
                PersianTextHelper.startsWithTargetLetter(rawPlayer, letter)
            val knownInBank = startsCorrect && WordBank.isKnownWord(letter, category, rawPlayer)
            val isValid = startsCorrect && (knownInBank || PersianTextHelper.isPlausibleWord(rawPlayer, letter))

            val botCandidates = WordBank.getEntries(letter, category)
            val botFills = botCandidates.isNotEmpty() && Random.nextFloat() <= difficulty.fillRate
            val botAnswer = if (botFills) {
                botCandidates.random()
            } else {
                ""
            }

            val isShared = isValid && botAnswer.isNotBlank() &&
                PersianTextHelper.normalize(rawPlayer) == PersianTextHelper.normalize(botAnswer)

            val (pScore, bScore) = computeScores(
                playerValid = isValid,
                botHasAnswer = botAnswer.isNotBlank(),
                isShared = isShared,
            )

            CategoryEvaluation(
                category = category,
                playerAnswer = rawPlayer,
                botAnswer = botAnswer,
                isPlayerValid = isValid,
                isKnownInWordBank = knownInBank,
                isSharedAnswer = isShared,
                playerScore = pScore,
                botScore = bScore,
                startsWithCorrectLetter = startsCorrect,
            )
        }
    }

    private fun computeScores(
        playerValid: Boolean,
        botHasAnswer: Boolean,
        isShared: Boolean,
    ): Pair<Int, Int> {
        return when {
            playerValid && botHasAnswer && isShared -> 5 to 5
            playerValid && botHasAnswer -> 10 to 10
            playerValid && !botHasAnswer -> 10 to 0
            !playerValid && botHasAnswer -> 0 to 10
            else -> 0 to 0
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.release()
    }
}
