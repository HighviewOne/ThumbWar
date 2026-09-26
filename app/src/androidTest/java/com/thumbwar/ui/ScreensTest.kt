package com.thumbwar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thumbwar.ui.components.CountdownOverlay
import com.thumbwar.ui.components.ScoreDisplay
import com.thumbwar.ui.screens.gameover.GameOverScreen
import com.thumbwar.ui.screens.menu.MainMenuScreen
import com.thumbwar.ui.screens.settings.SettingsScreen
import com.thumbwar.ui.theme.ThumbWarTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameOver_showsWinnerScoreAndMatchLabel() {
        composeTestRule.setContent {
            ThumbWarTheme {
                GameOverScreen(winner = 2, p1Score = 1, p2Score = 2, winsNeeded = 2, onRematch = {}, onMainMenu = {})
            }
        }
        composeTestRule.onNodeWithText("Player 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("WINS THE MATCH!").assertIsDisplayed()
    }

    @Test
    fun gameOver_buttonsInvokeTheirCallbacks() {
        var rematches = 0
        var menus = 0
        composeTestRule.setContent {
            ThumbWarTheme {
                GameOverScreen(winner = 1, p1Score = 1, p2Score = 0, onRematch = { rematches++ }, onMainMenu = { menus++ })
            }
        }
        composeTestRule.onNodeWithText("REMATCH").performClick()
        composeTestRule.onNodeWithText("MAIN MENU").performClick()
        assertEquals(1, rematches)
        assertEquals(1, menus)
    }

    @Test
    fun scoreDisplay_showsRoundOnlyInBestOfThree() {
        composeTestRule.setContent {
            ThumbWarTheme { ScoreDisplay(p1Score = 1, p2Score = 0, roundNumber = 2, winsNeeded = 2) }
        }
        composeTestRule.onNodeWithText("Round 2").assertIsDisplayed()
    }

    @Test
    fun countdown_showsDeclareText() {
        composeTestRule.setContent {
            ThumbWarTheme { CountdownOverlay(text = "I declare a thumb war!") }
        }
        composeTestRule.onNodeWithText("I declare a thumb war!").assertExists()
    }

    /** Renders [content] at the size of a phone in landscape (SM-A156U: 756 x 360 dp). */
    private fun setLandscapeContent(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            ThumbWarTheme { Box(Modifier.size(width = 756.dp, height = 360.dp)) { content() } }
        }
    }

    @Test
    fun landscape_mainMenu_settingsButtonIsReachable() {
        setLandscapeContent { MainMenuScreen(onStartSinglePlayer = { _, _ -> }, onStartTwoPlayer = {}, onSettings = {}) }
        composeTestRule.onNodeWithText("SETTINGS").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun landscape_settings_resetStatsIsReachable() {
        setLandscapeContent { SettingsScreen(onBack = {}) }
        composeTestRule.onNodeWithText("Reset Stats").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun landscape_gameOver_mainMenuButtonIsReachable() {
        setLandscapeContent { GameOverScreen(winner = 1, p1Score = 2, p2Score = 1, winsNeeded = 2, onRematch = {}, onMainMenu = {}) }
        composeTestRule.onNodeWithText("MAIN MENU").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun scoreDisplay_hidesRoundWhileAPinLabelNeedsTheSpace() {
        composeTestRule.setContent {
            ThumbWarTheme { ScoreDisplay(p1Score = 1, p2Score = 0, roundNumber = 2, winsNeeded = 2, showRound = false) }
        }
        composeTestRule.onNodeWithText("Round 2").assertDoesNotExist()
    }
}
