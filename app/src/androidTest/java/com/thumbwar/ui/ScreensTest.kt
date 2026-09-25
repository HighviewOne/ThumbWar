package com.thumbwar.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thumbwar.ui.components.CountdownOverlay
import com.thumbwar.ui.components.ScoreDisplay
import com.thumbwar.ui.screens.gameover.GameOverScreen
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
}
