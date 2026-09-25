package com.thumbwar.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thumbwar.ai.AiController
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.audio.GameSound
import com.thumbwar.audio.SoundManager
import com.thumbwar.data.PreferencesRepository
import com.thumbwar.data.StatsRepository
import com.thumbwar.engine.GameConfig
import com.thumbwar.engine.GameEngine
import com.thumbwar.engine.GamePhase
import com.thumbwar.engine.GameState
import com.thumbwar.input.InputEvent
import com.thumbwar.input.InputManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    private val soundManager: SoundManager,
    soundEnabled: Flow<Boolean>,
    vibrationEnabled: Flow<Boolean>,
    private val statsRepository: StatsRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val aiRandom: Random = Random.Default
) : ViewModel() {

    private var engine = GameEngine()
    private var inputManager: InputManager? = null
    private var aiController: AiController? = null
    private var gameLoopJob: Job? = null

    private val _gameState = MutableStateFlow(engine.getState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var isTwoPlayer = false
    private var lastPhase = GamePhase.READY
    private var lastCountdownText = ""
    private var winsNeeded = 1
    private var arenaWidth = 0f
    private var arenaHeight = 0f

    init {
        viewModelScope.launch { soundEnabled.collect { soundManager.setSoundEnabled(it) } }
        viewModelScope.launch { vibrationEnabled.collect { soundManager.setVibrationEnabled(it) } }
    }

    fun initialize(isTwoPlayer: Boolean, aiDifficulty: AiDifficulty, winsNeeded: Int = 1) {
        this.isTwoPlayer = isTwoPlayer
        this.winsNeeded = winsNeeded
        engine = newEngine()
        inputManager = InputManager(isTwoPlayer)

        if (!isTwoPlayer) {
            aiController = AiController(aiDifficulty, aiRandom)
        }

        startGame()
    }

    private fun newEngine() = GameEngine(winsNeeded).also { it.setArenaSize(arenaWidth, arenaHeight) }

    fun setArenaSize(width: Float, height: Float) {
        arenaWidth = width
        arenaHeight = height
        engine.setArenaSize(width, height)
    }

    private fun startGame() {
        engine.resetRound()
        engine.startGame()
        lastPhase = GamePhase.READY
        lastCountdownText = ""
        _gameState.value = engine.getState()
        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = clock()
            while (isActive) {
                val now = clock()
                val delta = (now - lastTime).coerceIn(1, 50)
                lastTime = now

                // AI update
                aiController?.let { ai ->
                    val state = engine.getState()
                    if (state.phase == GamePhase.PLAYING || state.phase == GamePhase.PIN_IN_PROGRESS) {
                        val aiTarget = ai.update(state, delta)
                        if (aiTarget != null) {
                            engine.setPlayer2Target(aiTarget)
                        } else {
                            engine.releasePlayer2()
                        }
                    }
                }

                engine.tick(delta)
                val newState = engine.getState()
                handleSoundAndHaptics(newState)
                _gameState.value = newState

                delay(GameConfig.TICK_RATE_MS)
            }
        }
    }

    private fun handleSoundAndHaptics(state: GameState) {
        // Countdown beats
        if (state.phase == GamePhase.COUNTDOWN) {
            countdownSoundFor(lastCountdownText, state.countdownText)?.let { sound ->
                soundManager.play(sound)
                soundManager.vibrate(if (sound == GameSound.COUNTDOWN_DECLARE) 50 else 30)
            }
            lastCountdownText = state.countdownText
        }

        // Phase transitions
        if (state.phase != lastPhase) {
            when (state.phase) {
                GamePhase.PIN_IN_PROGRESS -> {
                    soundManager.play(GameSound.COLLISION_THUD)
                    soundManager.vibrate(100)
                }
                GamePhase.GAME_OVER -> {
                    soundManager.play(GameSound.VICTORY_FANFARE)
                    soundManager.vibratePattern(longArrayOf(0, 100, 50, 100, 50, 200))
                    recordMatchResult(state)
                }
                else -> {}
            }
            lastPhase = state.phase
        }
    }

    private fun recordMatchResult(state: GameState) {
        val playerWon = singlePlayerMatchResult(state, isTwoPlayer) ?: return
        viewModelScope.launch {
            if (playerWon) statsRepository.recordWin() else statsRepository.recordLoss()
        }
    }

    fun onInputEvent(event: InputEvent) {
        when (event) {
            is InputEvent.Move -> {
                when (event.player) {
                    1 -> engine.setPlayer1Target(event.normalizedPosition)
                    2 -> if (isTwoPlayer) engine.setPlayer2Target(event.normalizedPosition)
                }
            }
            is InputEvent.Release -> {
                when (event.player) {
                    1 -> engine.releasePlayer1()
                    2 -> if (isTwoPlayer) engine.releasePlayer2()
                }
            }
        }
    }

    fun processPointer(
        pointerId: Long,
        x: Float,
        y: Float,
        pressed: Boolean,
        previousPressed: Boolean,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        inputManager?.processPointer(pointerId, x, y, pressed, previousPressed, canvasWidth, canvasHeight)
            ?.let { onInputEvent(it) }
    }

    fun startNextRound() {
        engine.nextRound()
        engine.startGame()
        lastPhase = GamePhase.READY
        lastCountdownText = ""
        _gameState.value = engine.getState()
        startGameLoop()
    }

    fun pause() {
        gameLoopJob?.cancel()
    }

    fun resume() {
        if (_gameState.value.phase != GamePhase.GAME_OVER) {
            startGameLoop()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val prefs = PreferencesRepository(app)
                GameViewModel(
                    soundManager = SoundManager(app),
                    soundEnabled = prefs.soundEnabled,
                    vibrationEnabled = prefs.vibrationEnabled,
                    statsRepository = StatsRepository(app)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        soundManager.release()
    }
}

/**
 * Sound to play when the countdown text changes: a beep per number, a distinct cue for
 * "I declare a thumb war!". Keyed on the text because the beat number stays at 4 during
 * the declare phase.
 */
internal fun countdownSoundFor(previousText: String, text: String): GameSound? = when {
    text == previousText || text.isEmpty() -> null
    text.length <= 1 -> GameSound.COUNTDOWN_BEAT
    else -> GameSound.COUNTDOWN_DECLARE
}

/**
 * Stats track single-player matches against the computer: true if the human (player 1) won
 * the match, false if the AI won, null when there's nothing to record (2-player, or the match
 * isn't over yet — e.g. the end of round 1 in best-of-3).
 */
internal fun singlePlayerMatchResult(state: GameState, isTwoPlayer: Boolean): Boolean? = when {
    isTwoPlayer || state.phase != GamePhase.GAME_OVER || !state.isMatchOver -> null
    else -> state.winner == 1
}
