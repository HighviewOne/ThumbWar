package com.thumbwar.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.data.DifficultyStats
import com.thumbwar.data.PreferencesRepository
import com.thumbwar.data.StatsRepository
import com.thumbwar.ui.theme.RingGold
import com.thumbwar.util.formatPinTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefsRepo = remember { PreferencesRepository(context) }
    val statsRepo = remember { StatsRepository(context) }
    val scope = rememberCoroutineScope()

    val soundEnabled by prefsRepo.soundEnabled.collectAsState(initial = true)
    val vibrationEnabled by prefsRepo.vibrationEnabled.collectAsState(initial = true)
    val defaultDifficulty by prefsRepo.defaultDifficulty.collectAsState(initial = "MEDIUM")

    val wins by statsRepo.wins.collectAsState(initial = 0)
    val losses by statsRepo.losses.collectAsState(initial = 0)
    val currentStreak by statsRepo.currentStreak.collectAsState(initial = 0)
    val bestStreak by statsRepo.bestStreak.collectAsState(initial = 0)
    val byDifficulty by statsRepo.byDifficulty.collectAsState(initial = emptyMap())

    // Scrolls when content doesn't fit (e.g. a phone in landscape)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onBack) {
                Text("< Back")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "SETTINGS",
                style = MaterialTheme.typography.headlineMedium,
                color = RingGold
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sound toggle
        SettingsRow(
            label = "Sound Effects",
            checked = soundEnabled,
            onToggle = { scope.launch { prefsRepo.setSoundEnabled(it) } }
        )

        // Vibration toggle
        SettingsRow(
            label = "Vibration",
            checked = vibrationEnabled,
            onToggle = { scope.launch { prefsRepo.setVibrationEnabled(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Default difficulty
        Text("Default Difficulty", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = defaultDifficulty == difficulty.name,
                    onClick = {
                        scope.launch { prefsRepo.setDefaultDifficulty(difficulty.name) }
                    },
                    label = { Text(difficulty.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        Text("STATISTICS", style = MaterialTheme.typography.headlineMedium, color = RingGold)
        Spacer(modifier = Modifier.height(16.dp))

        StatsRow("Total Wins", wins.toString())
        StatsRow("Total Losses", losses.toString())
        StatsRow("Current Streak", currentStreak.toString())
        StatsRow("Best Streak", bestStreak.toString())

        Spacer(modifier = Modifier.height(16.dp))
        DifficultyStatsTable(byDifficulty)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { scope.launch { statsRepo.resetStats() } }
        ) {
            Text("Reset Stats")
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun StatsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = RingGold)
    }
}

/** One row per difficulty: wins, losses and fastest pin against the computer. */
@Composable
private fun DifficultyStatsTable(stats: Map<AiDifficulty, DifficultyStats>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DifficultyStatsLine("", "Wins", "Losses", "Fastest pin", header = true)
        AiDifficulty.entries.forEach { d ->
            val s = stats[d] ?: DifficultyStats()
            DifficultyStatsLine(
                d.name.lowercase().replaceFirstChar { it.uppercase() },
                s.wins.toString(),
                s.losses.toString(),
                s.fastestPinMs?.let(::formatPinTime) ?: "–"
            )
        }
    }
}

@Composable
private fun DifficultyStatsLine(label: String, wins: String, losses: String, fastest: String, header: Boolean = false) {
    val style = if (header) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge
    val valueColor = if (header) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else RingGold
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = style, modifier = Modifier.weight(1.2f))
        Text(wins, style = style, color = valueColor, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(losses, style = style, color = valueColor, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(fastest, style = style, color = valueColor, textAlign = TextAlign.End, modifier = Modifier.weight(1.6f))
    }
}
