package com.thumbwar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thumbwar.ui.theme.Player1Skin
import com.thumbwar.ui.theme.Player2Skin

@Composable
fun ScoreDisplay(
    p1Score: Int,
    p2Score: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player 1 score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "P1",
                    style = MaterialTheme.typography.labelLarge,
                    color = Player1Skin
                )
                Text(
                    text = "$p1Score",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            // Player 2 score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "P2",
                    style = MaterialTheme.typography.labelLarge,
                    color = Player2Skin
                )
                Text(
                    text = "$p2Score",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }
    }
}
