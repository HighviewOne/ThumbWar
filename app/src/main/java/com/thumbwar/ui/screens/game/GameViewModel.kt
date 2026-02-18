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
    private var lastCountdownBeat = 0
    private var winsNeeded = 1

    fun initialize(isTwoPlayer: Boolean, aiDifficulty: AiDifficulty, winsNeeded: Int = 1) {
        this.isTwoPlayer = isTwoPlayer
        this.winsNeeded = winsNeeded
        engine = GameEngine(winsNeeded)
        inputManager = InputManager(isTwoPlayer)

        if (!isTwoPlayer) {
            aiController = AiController(aiDifficulty)
        }

        startGame()
    }

    private fun startGame() {
        engine.resetRound()
        engine.startGame()
        lastPhase = GamePhase.READY
        lastCountdownBeat = 0
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
        if (state.phase == GamePhase.COUNTDOWN && state.countdownBeat != lastCountdownBeat) {
            lastCountdownBeat = state.countdownBeat
            if (state.countdownText.length <= 1) {
                soundManager.play(GameSound.COUNTDOWN_BEAT)
                soundManager.vibrate(30)
            } else {
                soundManager.play(GameSound.COUNTDOWN_DECLARE)
                soundManager.vibrate(50)
            }
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

    fun processPointerDown(pointerId: Long, x: Float, y: Float, canvasWidth: Float, canvasHeight: Float) {
        inputManager?.processPointerDown(pointerId, x, y, canvasWidth, canvasHeight)?.let { onInputEvent(it) }
    }

    fun processPointerMove(pointerId: Long, x: Float, y: Float, canvasWidth: Float, canvasHeight: Float) {
        inputManager?.processPointerMove(pointerId, x, y, canvasWidth, canvasHeight)?.let { onInputEvent(it) }
    }

    fun processPointerUp(pointerId: Long) {
        inputManager?.processPointerUp(pointerId)?.let { onInputEvent(it) }
    }

    fun startNextRound() {
        engine.nextRound()
        engine.startGame()
        lastPhase = GamePhase.READY
        lastCountdownBeat = 0
        _gameState.value = engine.getState()
        startGameLoop()
    }

    fun rematch() {
        engine = GameEngine(winsNeeded)
        startGame()
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
