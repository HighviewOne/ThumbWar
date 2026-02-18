package com.thumbwar.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.ui.screens.game.GameScreen
import com.thumbwar.ui.screens.gameover.GameOverScreen
import com.thumbwar.ui.screens.menu.MainMenuScreen
import com.thumbwar.ui.screens.settings.SettingsScreen

object Routes {
    const val MAIN_MENU = "main_menu"
    const val GAME = "game/{mode}/{difficulty}"
    const val SETTINGS = "settings"
    const val GAME_OVER = "game_over/{winner}/{p1Score}/{p2Score}"

    fun game(mode: String, difficulty: String = "medium") = "game/$mode/$difficulty"
    fun gameOver(winner: Int, p1Score: Int, p2Score: Int) = "game_over/$winner/$p1Score/$p2Score"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.MAIN_MENU) {
        composable(Routes.MAIN_MENU) {
            MainMenuScreen(
                onStartSinglePlayer = { difficulty ->
                    navController.navigate(Routes.game("single", difficulty.name.lowercase()))
                },
                onStartTwoPlayer = {
                    navController.navigate(Routes.game("two_player"))
                },
                onSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.GAME) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "single"
            val difficultyStr = backStackEntry.arguments?.getString("difficulty") ?: "medium"
            val difficulty = AiDifficulty.entries.find {
                it.name.equals(difficultyStr, ignoreCase = true)
            } ?: AiDifficulty.MEDIUM

            GameScreen(
                isTwoPlayer = mode == "two_player",
                aiDifficulty = difficulty,
                onGameOver = { winner, p1Score, p2Score ->
                    navController.navigate(Routes.gameOver(winner, p1Score, p2Score)) {
                        popUpTo(Routes.MAIN_MENU)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.GAME_OVER) { backStackEntry ->
            val winner = backStackEntry.arguments?.getString("winner")?.toIntOrNull() ?: 1
            val p1Score = backStackEntry.arguments?.getString("p1Score")?.toIntOrNull() ?: 0
            val p2Score = backStackEntry.arguments?.getString("p2Score")?.toIntOrNull() ?: 0

            GameOverScreen(
                winner = winner,
                p1Score = p1Score,
                p2Score = p2Score,
                onRematch = {
                    navController.popBackStack()
                },
                onMainMenu = {
                    navController.popBackStack(Routes.MAIN_MENU, inclusive = false)
                }
            )
        }
    }
}
