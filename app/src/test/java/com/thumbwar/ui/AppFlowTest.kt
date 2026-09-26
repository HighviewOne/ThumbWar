package com.thumbwar.ui

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thumbwar.MainActivity
import com.thumbwar.data.PreferencesRepository
import com.thumbwar.data.StatsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Drives the real app (MainActivity + NavGraph) on the JVM under Robolectric. */
@RunWith(AndroidJUnit4::class)
class AppFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val prefs by lazy { PreferencesRepository(composeTestRule.activity) }
    private val stats by lazy { StatsRepository(composeTestRule.activity) }

    @Before
    fun resetStoredState() = runBlocking {
        // DataStore instances live for the whole test process, so start every test from defaults
        prefs.setSoundEnabled(true)
        prefs.setVibrationEnabled(true)
        prefs.setDefaultDifficulty("MEDIUM")
        stats.resetStats()
    }

    private fun click(text: String) = composeTestRule.onNodeWithText(text).performClick()

    private fun waitForText(text: String) =
        composeTestRule.waitUntil(5_000) { composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }

    private fun pressBack() = composeTestRule.runOnUiThread { composeTestRule.activity.onBackPressedDispatcher.onBackPressed() }

    @Test
    fun mainMenu_showsTitleAndModes() {
        composeTestRule.onNodeWithText("THUMB\nWAR").assertExists()
        composeTestRule.onNodeWithText("VS COMPUTER").assertExists()
        composeTestRule.onNodeWithText("2 PLAYERS").assertExists()
        composeTestRule.onNodeWithText("SETTINGS").assertExists()
    }

    @Test
    fun mainMenu_showsWinStreakOnceThereIsOne() {
        runBlocking { stats.recordWin() }
        waitForText("Win Streak: 1")
    }

    @Test
    fun vsComputer_pickDifficultyAndMatchLength_startsBestOfThree() {
        click("VS COMPUTER")
        composeTestRule.onNodeWithText("Select Difficulty").assertExists()
        click("HARD")
        composeTestRule.onNodeWithText("Match Length").assertExists()
        click("BEST OF 3")

        composeTestRule.onNodeWithText("Round 1").assertExists()
        composeTestRule.onNodeWithText("P1").assertExists()
    }

    @Test
    fun vsComputer_cancelClosesTheDialogs() {
        click("VS COMPUTER")
        click("Cancel")
        composeTestRule.onNodeWithText("Select Difficulty").assertDoesNotExist()

        click("VS COMPUTER")
        click("EASY")
        click("Cancel")
        composeTestRule.onNodeWithText("Match Length").assertDoesNotExist()
        composeTestRule.onNodeWithText("VS COMPUTER").assertExists()
    }

    @Test
    fun twoPlayer_forcesLandscapeAndBackRestoresIt() {
        click("2 PLAYERS")
        click("SINGLE ROUND")
        composeTestRule.waitForIdle()
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, composeTestRule.activity.requestedOrientation)
        composeTestRule.onNodeWithText("Round 1").assertDoesNotExist() // single round has no round label

        pressBack()
        waitForText("Quit match?")
        click("Quit")
        waitForText("VS COMPUTER")
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, composeTestRule.activity.requestedOrientation)
    }

    @Test
    fun settings_togglesAndDifficultyArePersisted() {
        click("SETTINGS")
        waitForText("Sound Effects")

        val switches = composeTestRule.onAllNodes(isToggleable())
        switches[0].assertIsOn().performClick()
        switches[1].assertIsOn().performClick()
        click("HARD")
        composeTestRule.waitUntil(5_000) {
            runBlocking { !prefs.soundEnabled.first() && !prefs.vibrationEnabled.first() && prefs.defaultDifficulty.first() == "HARD" }
        }
        switches[0].assertIsOff()
        switches[1].assertIsOff()
    }

    @Test
    fun settings_showsStatsAndResetClearsThem() {
        runBlocking {
            stats.recordWin()
            stats.recordLoss()
        }
        click("SETTINGS")
        waitForText("STATISTICS")
        composeTestRule.waitUntil(5_000) { composeTestRule.onAllNodesWithText("1").fetchSemanticsNodes().size == 3 }

        // Below the fold on a small screen: scroll to it like a user would
        composeTestRule.onNodeWithText("Reset Stats").performScrollTo().performClick()
        composeTestRule.waitUntil(5_000) { runBlocking { stats.wins.first() == 0 && stats.losses.first() == 0 } }
    }

    @Test
    fun settings_backReturnsToMenu() {
        click("SETTINGS")
        click("< Back")
        waitForText("VS COMPUTER")
    }
}
