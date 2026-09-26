package com.thumbwar.navigation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thumbwar.ui.theme.ThumbWarTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var nav: NavHostController

    @Before
    fun setUp() {
        composeTestRule.setContent {
            nav = rememberNavController()
            ThumbWarTheme { NavGraph(nav) }
        }
    }

    /** Lands on the game-over screen the way a finished game does: game screen popped, menu below. */
    private fun finishGame(mode: String, difficulty: String, winner: Int, winsNeeded: Int) {
        composeTestRule.runOnUiThread {
            nav.navigate(Routes.gameOver(mode, difficulty, winner, p1Score = 2, p2Score = 1, winsNeeded = winsNeeded)) {
                popUpTo(Routes.MAIN_MENU)
            }
        }
        composeTestRule.waitForIdle()
    }

    /** The top two back stack routes, bottom first (enough to see what Back would return to). */
    private fun backStackRoutes() =
        listOfNotNull(nav.previousBackStackEntry?.destination?.route, nav.currentBackStackEntry?.destination?.route)

    @Test
    fun gameOver_showsTheWinnerAndMatchLabel() {
        finishGame("single", "hard", winner = 1, winsNeeded = 2)
        composeTestRule.onNodeWithText("Player 1").assertExists()
        composeTestRule.onNodeWithText("WINS THE MATCH!").assertExists()
    }

    @Test
    fun rematch_startsANewGameWithTheSameSettings() {
        finishGame("single", "hard", winner = 2, winsNeeded = 2)
        composeTestRule.onNodeWithText("REMATCH").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(Routes.MAIN_MENU, Routes.GAME), backStackRoutes())
        val args = nav.currentBackStackEntry!!.arguments!!
        assertEquals("single", args.getString("mode"))
        assertEquals("hard", args.getString("difficulty"))
        assertEquals("2", args.getString("winsNeeded"))
        composeTestRule.onNodeWithText("Round 1").assertExists()
    }

    @Test
    fun mainMenu_fromGameOverReturnsToTheMenu() {
        finishGame("two_player", "medium", winner = 1, winsNeeded = 1)
        composeTestRule.onNodeWithText("WINNER!").assertExists()
        composeTestRule.onNodeWithText("MAIN MENU").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(Routes.MAIN_MENU), backStackRoutes())
        composeTestRule.onNodeWithText("VS COMPUTER").assertExists()
    }
}
