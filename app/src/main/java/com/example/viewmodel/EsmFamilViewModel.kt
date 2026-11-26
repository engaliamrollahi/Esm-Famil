package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.RoundEvaluator
import com.example.data.SettingsRepository
import com.example.model.ActiveRoundState
import com.example.model.AppStats
import com.example.model.BotDifficulty
import com.example.model.Category
import com.example.model.DiscoveredPeer
import com.example.model.GameMode
import com.example.model.GameSettings
import com.example.model.LetterPickMode
import com.example.model.LocalConnectionStatus
import com.example.model.LocalLobbyState
import com.example.model.LocalRole
import com.example.model.LocalTransportType
import com.example.model.MatchSummary
import com.example.model.PERSIAN_ALPHABET
import com.example.model.POPULAR_LETTERS
import com.example.model.PersianTextHelper
import com.example.model.PlayerAvatar
import com.example.model.RoundResult
import com.example.model.ThemeMode
import com.example.network.LocalSyncManager
import com.example.network.SyncMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EsmFamilViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    val soundManager = SoundManager()

    val settings: StateFlow<GameSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameSettings())

    val stats: StateFlow<AppStats> = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppStats())

    private val localSyncManager = LocalSyncManager(
        context = application,
        scope = viewModelScope,
        localProfileProvider = {
            val currentStats = stats.value
            currentStats.playerName to currentStats.avatar
        },
    )

    val localLobbyState: StateFlow<LocalLobbyState> = localSyncManager.lobbyState

    private val _selectedGameMode = MutableStateFlow(GameMode.AI)
    val selectedGameMode: StateFlow<GameMode> = _selectedGameMode.asStateFlow()

    private val _activeRound = MutableStateFlow<ActiveRoundState?>(null)
    val activeRound: StateFlow<ActiveRoundState?> = _activeRound.asStateFlow()

    private val _currentRoundResult = MutableStateFlow<RoundResult?>(null)
    val currentRoundResult: StateFlow<RoundResult?> = _currentRoundResult.asStateFlow()

    private val _completedRounds = MutableStateFlow<List<RoundResult>>(emptyList())
    val completedRounds: StateFlow<List<RoundResult>> = _completedRounds.asStateFlow()

    private val _matchSummary = MutableStateFlow<MatchSummary?>(null)
    val matchSummary: StateFlow<MatchSummary?> = _matchSummary.asStateFlow()

    private val _syncNotice = MutableStateFlow<String?>(null)
    val syncNotice: StateFlow<String?> = _syncNotice.asStateFlow()

    private var timerJob: Job? = null
    private var matchStatsRecorded = false
    private var activeMatchTotalRounds = 3

    // Local multiplayer synchronization buffers keyed by round number
    private var pendingLocalRound: ActiveRoundState? = null
    private val pendingRemoteAnswers = mutableMapOf<Int, Map<Category, String>>()

    init {
        viewModelScope.launch {
            settings.collect { current ->
                soundManager.isSoundEnabled = current.soundEnabled
            }
        }
        viewModelScope.launch {
            localSyncManager.incomingMessages.collect { message ->
                handleIncomingSyncMessage(message)
            }
        }
    }

    fun clearSyncNotice() {
        _syncNotice.value = null
    }

    fun setGameMode(mode: GameMode) {
        soundManager.playTap()
        _selectedGameMode.value = mode
    }

    fun setLocalTransport(transport: LocalTransportType) {
        soundManager.playTap()
        localSyncManager.setTransport(transport)
    }

    fun isBluetoothSupported(): Boolean = localSyncManager.isBluetoothSupported()
    fun isBluetoothEnabled(): Boolean = localSyncManager.isBluetoothEnabled()
    fun missingBluetoothHostPermissions(): List<String> = localSyncManager.missingBluetoothHostPermissions()
    fun missingBluetoothScanPermissions(): List<String> = localSyncManager.missingBluetoothScanPermissions()

    fun startLocalHosting() {
        soundManager.playTap()
        localSyncManager.startHosting()
    }

    fun startLocalDiscovery() {
        soundManager.playTap()
        localSyncManager.startDiscovery()
    }

    fun connectToPeer(peer: DiscoveredPeer) {
        soundManager.playTap()
        localSyncManager.connectToPeer(peer)
    }

    fun connectDirectWifi(ip: String, port: Int) {
        soundManager.playTap()
        localSyncManager.connectDirectWifi(ip, port)
    }

    fun disconnectLocalLobby() {
        soundManager.playTap()
        localSyncManager.disconnect()
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
        val mode = _selectedGameMode.value
        val currentSettings = settings.value
        val totalRounds = if (currentSettings.letterPickMode == LetterPickMode.MANUAL_CHOICE) {
            1
        } else {
            currentSettings.roundsCount
        }
        val firstLetter = pickNextLetter(currentSettings, emptyList())

        if (mode == GameMode.LOCAL_NETWORK) {
            val lobby = localLobbyState.value
            if (lobby.status != LocalConnectionStatus.CONNECTED) {
                _syncNotice.value = "ابتدا از طریق وای‌فای یا بلوتوث به بازیکن مقابل متصل شوید."
                return
            }
            if (lobby.role != LocalRole.HOST) {
                _syncNotice.value = "فقط میزبان می‌تواند شروع بازی را بزند."
                return
            }

            resetMatchBuffers()
            activeMatchTotalRounds = totalRounds
            val opponent = lobby.opponentName ?: "حریف شبکه محلی"

            viewModelScope.launch {
                val sent = localSyncManager.sendMessage(
                    SyncMessage.RoundStart(
                        roundNumber = 1,
                        totalRounds = totalRounds,
                        targetLetter = firstLetter,
                        timerSeconds = currentSettings.timerSeconds,
                        isNewMatch = true,
                    )
                )
                if (!sent) {
                    _syncNotice.value = "ارسال پیام شروع به حریف ناموفق بود."
                    return@launch
                }
                startRound(
                    roundNumber = 1,
                    totalRounds = totalRounds,
                    letter = firstLetter,
                    durationSeconds = currentSettings.timerSeconds,
                    usedLetters = listOf(firstLetter),
                    gameMode = GameMode.LOCAL_NETWORK,
                    opponentName = opponent,
                    isHost = true,
                )
            }
            return
        }

        // AI Solo Mode
        resetMatchBuffers()
        activeMatchTotalRounds = totalRounds
        startRound(
            roundNumber = 1,
            totalRounds = totalRounds,
            letter = firstLetter,
            durationSeconds = currentSettings.timerSeconds,
            usedLetters = listOf(firstLetter),
            gameMode = GameMode.AI,
            opponentName = "حریف هوشمند",
            isHost = true,
        )
    }

    fun startNextRound() {
        val lastResult = _currentRoundResult.value
        val mode = lastResult?.gameMode ?: _selectedGameMode.value
        val prevRounds = _completedRounds.value

        if (prevRounds.size >= activeMatchTotalRounds) {
            finalizeMatchSummary()
            return
        }

        val used = prevRounds.map { it.targetLetter }
        val nextLetter = pickNextLetter(settings.value, used)
        val nextRoundNumber = prevRounds.size + 1

        if (mode == GameMode.LOCAL_NETWORK) {
            val lobby = localLobbyState.value
            if (lobby.status != LocalConnectionStatus.CONNECTED) {
                _syncNotice.value = "ارتباط شبکه محلی قطع شده است."
                return
            }
            if (lobby.role != LocalRole.HOST) {
                _syncNotice.value = "منتظر شروع دور بعدی توسط میزبان بمانید."
                return
            }
            val opponent = lobby.opponentName ?: "حریف شبکه محلی"
            viewModelScope.launch {
                val sent = localSyncManager.sendMessage(
                    SyncMessage.RoundStart(
                        roundNumber = nextRoundNumber,
                        totalRounds = activeMatchTotalRounds,
                        targetLetter = nextLetter,
                        timerSeconds = settings.value.timerSeconds,
                        isNewMatch = false,
                    )
                )
                if (!sent) {
                    _syncNotice.value = "خطا در ارسال شروع دور جدید."
                    return@launch
                }
                _currentRoundResult.value = null
                startRound(
                    roundNumber = nextRoundNumber,
                    totalRounds = activeMatchTotalRounds,
                    letter = nextLetter,
                    durationSeconds = settings.value.timerSeconds,
                    usedLetters = used + nextLetter,
                    gameMode = GameMode.LOCAL_NETWORK,
                    opponentName = opponent,
                    isHost = true,
                )
            }
            return
        }

        _currentRoundResult.value = null
        startRound(
            roundNumber = nextRoundNumber,
            totalRounds = activeMatchTotalRounds,
            letter = nextLetter,
            durationSeconds = settings.value.timerSeconds,
            usedLetters = used + nextLetter,
            gameMode = GameMode.AI,
            opponentName = "حریف هوشمند",
            isHost = true,
        )
    }

    private fun resetMatchBuffers() {
        _completedRounds.value = emptyList()
        _currentRoundResult.value = null
        _matchSummary.value = null
        matchStatsRecorded = false
        pendingLocalRound = null
        pendingRemoteAnswers.clear()
        localSyncManager.setWaitingForOpponentAnswers(false)
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
        gameMode: GameMode,
        opponentName: String,
        isHost: Boolean,
    ) {
        timerJob?.cancel()
        pendingLocalRound = null
        localSyncManager.setWaitingForOpponentAnswers(false)

        _activeRound.value = ActiveRoundState(
            roundNumber = roundNumber,
            totalRounds = totalRounds,
            targetLetter = letter,
            totalDurationSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            usedLetters = usedLetters,
            gameMode = gameMode,
            opponentName = opponentName,
            isHost = isHost,
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
                submitCurrentRound(triggeredByPeerStop = false)
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

    fun submitCurrentRound(triggeredByPeerStop: Boolean = false) {
        timerJob?.cancel()
        val round = _activeRound.value ?: return

        if (round.gameMode == GameMode.LOCAL_NETWORK) {
            if (pendingLocalRound?.roundNumber == round.roundNumber) {
                return
            }
            soundManager.playRoundStop()
            pendingLocalRound = round
            localSyncManager.setWaitingForOpponentAnswers(true)

            viewModelScope.launch {
                if (!triggeredByPeerStop) {
                    localSyncManager.sendMessage(SyncMessage.StopPressed(round.roundNumber))
                }
                localSyncManager.sendMessage(
                    SyncMessage.RoundAnswers(
                        roundNumber = round.roundNumber,
                        answers = round.answers,
                    )
                )
                tryCompleteLocalSyncedRound(round.roundNumber)
            }
            return
        }

        // AI Solo Mode
        soundManager.playRoundStop()
        val difficulty = settings.value.botDifficulty
        val evaluations = RoundEvaluator.evaluateAgainstBot(
            targetLetter = round.targetLetter,
            playerAnswers = round.answers,
            difficulty = difficulty,
        )
        val result = RoundResult(
            roundNumber = round.roundNumber,
            totalRounds = round.totalRounds,
            targetLetter = round.targetLetter,
            evaluations = evaluations,
            playerRoundScore = evaluations.sumOf { it.playerScore },
            botRoundScore = evaluations.sumOf { it.botScore },
            gameMode = GameMode.AI,
            opponentName = "حریف هوشمند",
            isHost = true,
        )

        _activeRound.value = null
        _currentRoundResult.value = result
        _completedRounds.update { it + result }

        if (round.roundNumber >= round.totalRounds) {
            prepareMatchSummary()
        }
    }

    private fun tryCompleteLocalSyncedRound(roundNumber: Int) {
        val localRound = pendingLocalRound ?: return
        if (localRound.roundNumber != roundNumber) return
        val remoteAnswers = pendingRemoteAnswers.remove(roundNumber) ?: return

        pendingLocalRound = null
        localSyncManager.setWaitingForOpponentAnswers(false)

        val evaluations = RoundEvaluator.evaluateTwoPlayers(
            targetLetter = localRound.targetLetter,
            localAnswers = localRound.answers,
            opponentAnswers = remoteAnswers,
        )
        val result = RoundResult(
            roundNumber = localRound.roundNumber,
            totalRounds = localRound.totalRounds,
            targetLetter = localRound.targetLetter,
            evaluations = evaluations,
            playerRoundScore = evaluations.sumOf { it.playerScore },
            botRoundScore = evaluations.sumOf { it.botScore },
            gameMode = GameMode.LOCAL_NETWORK,
            opponentName = localRound.opponentName,
            isHost = localRound.isHost,
        )

        _activeRound.value = null
        _currentRoundResult.value = result
        _completedRounds.update { it + result }

        if (localRound.roundNumber >= localRound.totalRounds) {
            prepareMatchSummary()
        }
    }

    private fun handleIncomingSyncMessage(message: SyncMessage) {
        when (message) {
            is SyncMessage.Hello -> {
                _activeRound.update { current ->
                    current?.copy(opponentName = message.playerName)
                }
            }
            is SyncMessage.RoundStart -> {
                if (message.isNewMatch) {
                    resetMatchBuffers()
                } else {
                    _currentRoundResult.value = null
                }
                activeMatchTotalRounds = message.totalRounds
                val lobby = localLobbyState.value
                val opponent = lobby.opponentName ?: "میزبان"
                val used = _completedRounds.value.map { it.targetLetter } + message.targetLetter
                _selectedGameMode.value = GameMode.LOCAL_NETWORK
                startRound(
                    roundNumber = message.roundNumber,
                    totalRounds = message.totalRounds,
                    letter = message.targetLetter,
                    durationSeconds = message.timerSeconds,
                    usedLetters = used,
                    gameMode = GameMode.LOCAL_NETWORK,
                    opponentName = opponent,
                    isHost = false,
                )
            }
            is SyncMessage.StopPressed -> {
                val active = _activeRound.value
                if (active != null && active.gameMode == GameMode.LOCAL_NETWORK && active.roundNumber == message.roundNumber) {
                    _syncNotice.value = "${active.opponentName} دکمه «استپ!» را زد — پاسخ‌ها ثبت شد."
                    submitCurrentRound(triggeredByPeerStop = true)
                }
            }
            is SyncMessage.RoundAnswers -> {
                pendingRemoteAnswers[message.roundNumber] = message.answers
                tryCompleteLocalSyncedRound(message.roundNumber)
            }
            is SyncMessage.LeaveMatch -> {
                if (_activeRound.value?.gameMode == GameMode.LOCAL_NETWORK ||
                    (_currentRoundResult.value?.gameMode == GameMode.LOCAL_NETWORK && _matchSummary.value == null)
                ) {
                    timerJob?.cancel()
                    pendingLocalRound = null
                    pendingRemoteAnswers.clear()
                    _activeRound.value = null
                    _currentRoundResult.value = null
                    _completedRounds.value = emptyList()
                    _matchSummary.value = null
                    _syncNotice.value = "بازیکن مقابل از بازی خارج شد و مسابقه لغو گردید."
                }
            }
        }
    }

    fun toggleCategoryValidity(category: Category) {
        val currentResult = _currentRoundResult.value ?: return
        if (currentResult.gameMode != GameMode.AI) {
            // In synchronized multiplayer, scoring is deterministic on both devices.
            return
        }
        soundManager.playTap()
        val updatedEvaluations = currentResult.evaluations.map { eval ->
            if (eval.category != category) {
                eval
            } else {
                if (!eval.startsWithCorrectLetter || eval.playerAnswer.isBlank()) {
                    eval
                } else {
                    val newValid = !eval.isPlayerValid
                    val (pScore, bScore) = RoundEvaluator.computeScores(
                        localValid = newValid,
                        opponentValid = eval.botAnswer.isNotBlank(),
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
        val mode = rounds.lastOrNull()?.gameMode ?: _selectedGameMode.value
        val opponentName = rounds.lastOrNull()?.opponentName ?: "حریف هوشمند"
        val existingDelta = _matchSummary.value?.progressionDelta
        _matchSummary.value = MatchSummary(
            rounds = rounds,
            playerTotalScore = playerTotal,
            botTotalScore = botTotal,
            gameMode = mode,
            opponentName = opponentName,
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
                    gameMode = mode,
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
        val wasLocalActiveMidMatch = (_activeRound.value?.gameMode == GameMode.LOCAL_NETWORK) ||
            (_currentRoundResult.value?.gameMode == GameMode.LOCAL_NETWORK && _matchSummary.value == null)

        timerJob?.cancel()
        pendingLocalRound = null
        pendingRemoteAnswers.clear()
        localSyncManager.setWaitingForOpponentAnswers(false)
        _activeRound.value = null
        _currentRoundResult.value = null
        _completedRounds.value = emptyList()
        _matchSummary.value = null

        if (wasLocalActiveMidMatch) {
            localSyncManager.disconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        localSyncManager.release()
        soundManager.release()
    }
}
