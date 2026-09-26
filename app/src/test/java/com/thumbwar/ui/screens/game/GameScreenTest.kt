package com.thumbwar.ui.screens.game

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.data.StatsRepository
import com.thumbwar.engine.GameConfig
import com.thumbwar.engine.GamePhase
import com.thumbwar.ui.theme.ThumbWarTheme
import io.mockk.mockk
import java.time.Duration
import kotlin.random.Random
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

/**
 * Plays real games through GameScreen's touch handling. The game loop runs on the main looper
 * and the screen's effects on Compose's clock; [advance] steps them together frame by frame.
 */
@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /** Game time, advanced only by [advance], so the loop's timing doesn't depend on Robolectric's clocks. */
    private var now = 0L

    private val vm = GameViewModel(
        soundManager = mockk(relaxed = true),
        soundEnabled = flowOf(true),
        vibrationEnabled = flowOf(true),
        statsRepository = mockk<StatsRepository>(relaxed = true),
        clock = { now },
        aiRandom = Random(42) // same computer moves every run
    )
    private var gameOver: Triple<Int, Int, Int>? = null
    private var backPressed = false

    /** Toggling this removes and re-adds GameScreen, like the activity being recreated. */
    private val onScreen = mutableStateOf(true)

    private fun show(isTwoPlayer: Boolean, winsNeeded: Int) {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            ThumbWarTheme {
                Box(Modifier.size(400.dp).testTag("game")) {
                    if (onScreen.value) {
                        GameScreen(
                            isTwoPlayer = isTwoPlayer,
                            aiDifficulty = AiDifficulty.EASY,
                            winsNeeded = winsNeeded,
                            onGameOver = { winner, p1, p2 -> gameOver = Triple(winner, p1, p2) },
                            onBack = { backPressed = true },
                            viewModel = vm
                        )
                    }
                }
            }
        }
    }

    /**
     * One frame at a time: move game time, let the main looper run the game loop's delay(), and
     * advance Compose's clock for the screen's effects (round/match-end delays, animations).
     */
    private fun advance(ms: Long) {
        repeat((ms / GameConfig.TICK_RATE_MS).toInt()) {
            now += GameConfig.TICK_RATE_MS
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(GameConfig.TICK_RATE_MS))
            composeTestRule.mainClock.advanceTimeBy(GameConfig.TICK_RATE_MS)
        }
    }

    private fun state() = vm.gameState.value

    private fun advanceToPlaying() {
        advance(GameConfig.COUNTDOWN_BEAT_DURATION_MS * GameConfig.COUNTDOWN_BEATS + GameConfig.COUNTDOWN_DECLARE_DURATION_MS + 300)
        assertEquals(GamePhase.PLAYING, state().phase)
    }

    /** Point on the arena at normalized (x, y), using its laid-out size (the box may be shrunk to fit the screen). */
    private fun at(x: Float, y: Float): Offset {
        val size = composeTestRule.onNodeWithTag("game").fetchSemanticsNode().size
        return Offset(x * size.width, y * size.height)
    }

    private fun pinPlayer2WithPlayer1() {
        // P1 presses on its own half and drags onto P2, then holds
        composeTestRule.onNodeWithTag("game").performTouchInput {
            down(0, at(GameConfig.PLAYER1_START_X, GameConfig.PLAYER1_START_Y))
            moveTo(0, at(GameConfig.PLAYER2_START_X, GameConfig.PLAYER2_START_Y))
        }
        advance(((GameConfig.PIN_DURATION_SECONDS + 1.5f) * 1000).toLong())
    }

    @Test
    fun countdownShowsThenPlayBegins() {
        show(isTwoPlayer = false, winsNeeded = 1)
        advance(100)
        composeTestRule.onNodeWithText("1").assertExists()
        advance(GameConfig.COUNTDOWN_BEAT_DURATION_MS * GameConfig.COUNTDOWN_BEATS)
        assertEquals("I declare a thumb war!", state().countdownText)
        composeTestRule.onNodeWithText("I declare a thumb war!").assertExists()
        // Only the rest of the countdown: going further gives the computer time to pin the idle thumb
        advance(GameConfig.COUNTDOWN_DECLARE_DURATION_MS + 100)
        assertEquals(GamePhase.PLAYING, state().phase)
    }

    @Test
    fun twoPlayer_eachFingerDrivesItsOwnThumb() {
        show(isTwoPlayer = true, winsNeeded = 1)
        advanceToPlaying()
        val p1Start = state().thumb1.position
        val p2Start = state().thumb2.position

        composeTestRule.onNodeWithTag("game").performTouchInput {
            down(0, at(0.3f, 0.1f))
            down(1, at(0.7f, 0.9f))
        }
        advance(300)
        assertTrue("P1 moves up", state().thumb1.position.y < p1Start.y - 0.1f)
        assertTrue("P2 moves down", state().thumb2.position.y > p2Start.y + 0.1f)

        // P2 lifts; P1's finger keeps control
        composeTestRule.onNodeWithTag("game").performTouchInput {
            up(1)
            moveTo(0, at(0.1f, 0.1f))
        }
        val beforeDrag = state().thumb1.position
        advance(300)
        assertTrue("P1 still follows its finger", state().thumb1.position.x < beforeDrag.x - 0.05f)
    }

    @Test
    fun bestOfThree_roundWinShowsBannerThenStartsRoundTwo() {
        show(isTwoPlayer = true, winsNeeded = 2)
        advanceToPlaying()
        pinPlayer2WithPlayer1()

        assertEquals(GamePhase.GAME_OVER, state().phase)
        composeTestRule.onNodeWithText("Player 1 wins the round!").assertExists()

        advance(GameConfig.ROUND_TRANSITION_DELAY_MS + 200)
        assertEquals(2, state().roundNumber)
        composeTestRule.onNodeWithText("Round 2").assertExists()
    }

    @Test
    fun matchWin_reportsTheResultToTheCaller() {
        show(isTwoPlayer = true, winsNeeded = 1)
        advanceToPlaying()
        composeTestRule.onNodeWithText("Player 1 pinning!").assertDoesNotExist()
        pinPlayer2WithPlayer1()

        advance(2000)
        assertEquals(Triple(1, 1, 0), gameOver)
    }

    @Test
    fun pinShowsProgressWhileHeld() {
        show(isTwoPlayer = true, winsNeeded = 2)
        advanceToPlaying()
        composeTestRule.onNodeWithTag("game").performTouchInput {
            down(0, at(GameConfig.PLAYER1_START_X, GameConfig.PLAYER1_START_Y))
            moveTo(0, at(GameConfig.PLAYER2_START_X, GameConfig.PLAYER2_START_Y))
        }
        advance(800)

        assertEquals(GamePhase.PIN_IN_PROGRESS, state().phase)
        composeTestRule.onNodeWithText("Player 1 pinning!").assertExists()
        composeTestRule.onNodeWithText("Round 1").assertDoesNotExist() // hidden so the labels don't overlap
    }

    private fun pressBack() =
        composeTestRule.runOnUiThread { composeTestRule.activity.onBackPressedDispatcher.onBackPressed() }

    @Test
    fun backAsksBeforeQuitting_quitCallsOnBack() {
        show(isTwoPlayer = false, winsNeeded = 1)
        advance(100)
        pressBack()
        advance(100)
        assertEquals(false, backPressed)
        composeTestRule.onNodeWithText("Quit match?").assertExists()

        composeTestRule.onNodeWithText("Quit").performClick()
        assertTrue(backPressed)
    }

    @Test
    fun backThenKeepPlaying_freezesTheMatchUntilAnswered() {
        show(isTwoPlayer = false, winsNeeded = 1)
        advanceToPlaying()
        pressBack()
        advance(100)
        composeTestRule.onNodeWithText("Quitting now counts as a loss.").assertExists()
        val frozen = state()
        advance(1000)
        assertEquals(frozen, state())

        composeTestRule.onNodeWithText("Keep Playing").performClick()
        advance(100)
        composeTestRule.onNodeWithText("Quit match?").assertDoesNotExist()
        assertEquals(false, backPressed)
        assertTrue("Game time moves again", state().elapsedTimeMs > frozen.elapsedTimeMs)
    }

    @Test
    fun backgroundedWithQuitPromptOpen_staysFrozenOnReturn() {
        show(isTwoPlayer = false, winsNeeded = 1)
        advanceToPlaying()
        pressBack()
        advance(100)
        val frozen = state()

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        advance(1000)
        assertEquals(frozen, state())
        composeTestRule.onNodeWithText("Quit match?").assertExists()
    }

    @Test
    fun backgroundedDuringRoundBanner_nextRoundWaitsForTheReturn() {
        show(isTwoPlayer = true, winsNeeded = 2)
        advanceToPlaying()
        pinPlayer2WithPlayer1()
        composeTestRule.onNodeWithText("Player 1 wins the round!").assertExists()

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        advance(8_000)
        assertEquals("Round 2 must not play in the background", GamePhase.COUNTDOWN, state().phase)
        assertEquals(0, state().p2RoundWins)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        advanceToPlaying()
        assertEquals(2, state().roundNumber)
    }

    @Test
    fun screenRebuiltMidMatch_keepsTheScore() {
        show(isTwoPlayer = true, winsNeeded = 2)
        advanceToPlaying()
        pinPlayer2WithPlayer1()
        assertEquals(1, state().p1RoundWins)

        onScreen.value = false
        advance(100)
        onScreen.value = true
        advance(100)

        assertEquals(1, state().p1RoundWins)
        assertEquals(1, state().roundNumber)
    }
}
