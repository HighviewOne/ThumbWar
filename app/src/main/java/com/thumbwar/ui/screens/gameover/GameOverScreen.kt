package com.thumbwar.ui.screens.gameover

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thumbwar.ui.theme.MenuAccent
import com.thumbwar.ui.theme.RingGold
import com.thumbwar.ui.theme.VictoryGold

@Composable
fun GameOverScreen(
    winner: Int,
    p1Score: Int,
    p2Score: Int,
    onRematch: () -> Unit,
    onMainMenu: () -> Unit
) {
    // Victory bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "victory")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Entry animation
    val entryScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entryScale.animateTo(
            1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .scale(entryScale.value),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Trophy/crown emoji placeholder
        Text(
            text = "WINNER!",
            fontSize = 20.sp,
            color = VictoryGold,
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Player $winner",
            style = MaterialTheme.typography.displayLarge,
            color = VictoryGold,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(bounceScale)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Score
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("P1", style = MaterialTheme.typography.labelLarge)
                Text(
                    "$p1Score",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (winner == 1) VictoryGold else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                "-",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("P2", style = MaterialTheme.typography.labelLarge)
                Text(
                    "$p2Score",
                    style = MaterialTheme.typography.displayMedium,
                    color = if (winner == 2) VictoryGold else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRematch,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MenuAccent)
        ) {
            Text("REMATCH", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onMainMenu,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Text("MAIN MENU", style = MaterialTheme.typography.labelLarge)
        }
    }
}
