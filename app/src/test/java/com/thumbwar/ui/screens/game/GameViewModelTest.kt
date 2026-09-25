package com.thumbwar.ui.screens.game

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.audio.SoundManager
import com.thumbwar.data.StatsRepository
import com.thumbwar.engine.GameConfig
import com.thumbwar.engine.GamePhase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Runs the real game loop on a virtual clock: the loop's delay() and its clock both follow the
 * test scheduler, so advancing N ms of virtual time plays N ms of game.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private val store = ViewModelStore()

    private val soundManager = mockk<SoundManager>(relaxed = true)
    private val soundEnabled = MutableStateFlow(true)
    private val vibrationEnabled = MutableStateFlow(true)
    private val results = mutableListOf<Boolean>() // true = win recorded, false = loss
    private val stats = mockk<StatsRepository> {
        coEvery { recordWin() } answers { results += true }
        coEvery { recordLoss() } answers { results += false }
    }

    private lateinit var vm: GameViewModel

    private val size = 1000f
    private var nextPointerId = 1L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val factory = viewModelFactory {
            initializer {
                GameViewModel(
                    soundManager = soundManager,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    statsRepository = stats,
                    clock = { dispatcher.scheduler.currentTime },
                    aiRandom = Random(42)
                )
            }
        }
        vm = ViewModelProvider(store, factory)[GameViewModel::class.java]
        vm.setArenaSize(size, size)
    }

    @After
    fun tearDown() {
        store.clear() // cancels the game loop and settings collectors
        Dispatchers.resetMain()
    }

    private fun advance(ms: Long) {
        scope.testScheduler.advanceTimeBy(ms)
        scope.testScheduler.runCurrent()
    }

    private fun state() = vm.gameState.value

    private fun advanceToPlaying() {
        advance(GameConfig.COUNTDOWN_BEAT_DURATION_MS * GameConfig.COUNTDOWN_BEATS + GameConfig.COUNTDOWN_DECLARE_DURATION_MS + 200)
        assertEquals(GamePhase.PLAYING, state().phase)
    }

    /** Presses a new finger at normalized (x, y) and returns its pointer id. */
    private fun press(x: Float, y: Float): Long {
        val id = nextPointerId++
        vm.processPointer(id, x * size, y * size, pressed = true, previousPressed = false, canvasWidth = size, canvasHeight = size)
        return id
    }

    private fun drag(id: Long, x: Float, y: Float) =
        vm.processPointer(id, x * size, y * size, pressed = true, previousPressed = true, canvasWidth = size, canvasHeight = size)

    /** P1 presses on its own half, then drags across the midline onto a stationary P2. */
    private fun drivePlayer1OntoPlayer2() {
        val finger = press(GameConfig.PLAYER1_START_X, GameConfig.PLAYER1_START_Y)
        drag(finger, GameConfig.PLAYER2_START_X, GameConfig.PLAYER2_START_Y)
    }

    @Test
    fun `initialize starts the countdown, which leads to PLAYING`() {
        assertEquals(GamePhase.READY, state().phase)
        vm.initialize(isTwoPlayer = false, aiDifficulty = AiDifficulty.EASY)
        advance(100)
        assertEquals(GamePhase.COUNTDOWN, state().phase)
        advanceToPlaying()
    }

    @Test
    fun `single player - a touch anywhere controls player 1`() {
        vm.initialize(isTwoPlayer = false, aiDifficulty = AiDifficulty.EASY)
        advanceToPlaying()
        val start = state().thumb1.position

        press(0.8f, 0.9f) // on the right half, which would be P2's in 2-player mode
        advance(300)

        assertTrue("P1 should move toward the touch", state().thumb1.position.y > start.y + 0.1f)
    }

    @Test
    fun `two player - each half of the screen controls its own thumb`() {
        vm.initialize(isTwoPlayer = true, aiDifficulty = AiDifficulty.MEDIUM)
        advanceToPlaying()
        val p1Start = state().thumb1.position
        val p2Start = state().thumb2.position

        press(0.3f, 0.1f)
        press(0.7f, 0.9f)
        advance(300)

        assertTrue("P1 moves up", state().thumb1.position.y < p1Start.y - 0.1f)
        assertTrue("P2 moves down", state().thumb2.position.y > p2Start.y + 0.1f)
    }

    @Test
    fun `best of 3 - a round win leads to round 2, not the end of the match`() {
        vm.initialize(isTwoPlayer = true, aiDifficulty = AiDifficulty.MEDIUM, winsNeeded = 2)
        advanceToPlaying()

        drivePlayer1OntoPlayer2()
        advance(((GameConfig.PIN_DURATION_SECONDS + 1) * 1000).toLong())

        assertEquals(GamePhase.GAME_OVER, state().phase)
        assertEquals(1, state().winner)
        assertEquals(false, state().isMatchOver)

        vm.startNextRound()
        advance(100)
        assertEquals(2, state().roundNumber)
        assertEquals(1, state().p1RoundWins)
        assertEquals(GamePhase.COUNTDOWN, state().phase)
    }

    @Test
    fun `single player - finishing a match records exactly one result`() {
        vm.initialize(isTwoPlayer = false, aiDifficulty = AiDifficulty.HARD)
        advanceToPlaying()

        // Chase the AI's thumb until someone wins the match
        val finger = press(state().thumb1.position.x, state().thumb1.position.y)
        var elapsed = 0L
        while (!state().isMatchOver && elapsed < 30_000) {
            drag(finger, state().thumb2.position.x, state().thumb2.position.y)
            advance(GameConfig.TICK_RATE_MS)
            elapsed += GameConfig.TICK_RATE_MS
        }
        assertTrue("Match should finish", state().isMatchOver)

        advance(1000) // loop keeps running in GAME_OVER; nothing more should be recorded
        assertEquals(listOf(state().winner == 1), results)
    }

    @Test
    fun `two player - finishing a match records nothing`() {
        vm.initialize(isTwoPlayer = true, aiDifficulty = AiDifficulty.MEDIUM)
        advanceToPlaying()
        drivePlayer1OntoPlayer2()
        advance(((GameConfig.PIN_DURATION_SECONDS + 1) * 1000).toLong())

        assertTrue(state().isMatchOver)
        assertEquals(emptyList<Boolean>(), results)
    }

    @Test
    fun `sound and vibration settings are applied to the sound manager`() {
        advance(1)
        verify { soundManager.setSoundEnabled(true) }
        verify { soundManager.setVibrationEnabled(true) }

        soundEnabled.value = false
        vibrationEnabled.value = false
        advance(1)

        verify { soundManager.setSoundEnabled(false) }
        verify { soundManager.setVibrationEnabled(false) }
    }

    @Test
    fun `pause freezes the game and resume continues it`() {
        vm.initialize(isTwoPlayer = false, aiDifficulty = AiDifficulty.EASY)
        advance(1000)
        vm.pause()
        val frozen = state()
        advance(5000)
        assertEquals(frozen, state())

        vm.resume()
        advance(5000)
        assertEquals(GamePhase.PLAYING, state().phase)
    }
}
