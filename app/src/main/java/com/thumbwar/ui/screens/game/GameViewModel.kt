package com.thumbwar.ui.screens.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thumbwar.ai.AiController
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.audio.GameSound
import com.thumbwar.audio.SoundManager
import com.thumbwar.engine.GameConfig
import com.thumbwar.engine.GameEngine
import com.thumbwar.engine.GamePhase
import com.thumbwar.engine.GameState
import com.thumbwar.input.InputEvent
import com.thumbwar.input.InputManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private var engine = GameEngine()
    private var inputManager: InputManager? = null
    private var aiController: AiController? = null
    private var gameLoopJob: Job? = null
    private var roundTransitionJob: Job? = null
    private val soundManager = SoundManager(application)

    private val _gameState = MutableStateFlow(engine.getState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var isTwoPlayer = false
    private var lastPhase = GamePhase.READY
    private var lastCountdownText = ""
    private var winsNeeded = 1
    private var arenaWidth = 0f
    private var arenaHeight = 0f

    fun initialize(isTwoPlayer: Boolean, aiDifficulty: AiDifficulty, winsNeeded: Int = 1) {
        this.isTwoPlayer = isTwoPlayer
        this.winsNeeded = winsNeeded
        engine = newEngine()
        inputManager = InputManager(isTwoPlayer)

        if (!isTwoPlayer) {
            aiController = AiController(aiDifficulty)
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
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
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
                }
                else -> {}
            }
            lastPhase = state.phase
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

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        roundTransitionJob?.cancel()
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
