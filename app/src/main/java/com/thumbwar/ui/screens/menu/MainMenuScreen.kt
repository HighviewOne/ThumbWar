package com.thumbwar.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.data.StatsRepository
import com.thumbwar.ui.theme.RingGold
import com.thumbwar.ui.theme.MenuAccent
import com.thumbwar.ui.theme.VictoryGold

@Composable
fun MainMenuScreen(
    onStartSinglePlayer: (AiDifficulty) -> Unit,
    onStartTwoPlayer: () -> Unit,
    onSettings: () -> Unit
) {
    var showDifficultyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val statsRepo = remember { StatsRepository(context) }
    val currentStreak by statsRepo.currentStreak.collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "THUMB\nWAR",
            style = MaterialTheme.typography.displayLarge,
            color = RingGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "1, 2, 3, 4...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        // Win streak display
        if (currentStreak > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Win Streak: $currentStreak",
                style = MaterialTheme.typography.labelLarge,
                color = VictoryGold
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { showDifficultyDialog = true },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MenuAccent)
        ) {
            Text("VS COMPUTER", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartTwoPlayer,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MenuAccent)
        ) {
            Text("2 PLAYERS", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Text("SETTINGS", style = MaterialTheme.typography.labelLarge)
        }
    }

    if (showDifficultyDialog) {
        AlertDialog(
            onDismissRequest = { showDifficultyDialog = false },
            title = { Text("Select Difficulty") },
            text = {
                Column {
                    AiDifficulty.entries.forEach { difficulty ->
                        TextButton(
                            onClick = {
                                showDifficultyDialog = false
                                onStartSinglePlayer(difficulty)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                difficulty.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDifficultyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
