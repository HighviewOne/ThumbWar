package com.thumbwar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.thumbwar.engine.GameState
import com.thumbwar.ui.theme.*

@Composable
fun ArenaCanvas(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasSize = Size(size.width, size.height)

        // Background mat
        drawRect(color = ArenaGreen)

        // Darker mat border
        drawRect(
            color = ArenaGreenDark,
            style = Stroke(width = 8f)
        )

        // Ring border (oval)
        val ringPadding = size.width.coerceAtMost(size.height) * 0.08f
        drawOval(
            color = RingGold,
            topLeft = Offset(ringPadding, ringPadding),
            size = Size(size.width - ringPadding * 2, size.height - ringPadding * 2),
            style = Stroke(width = 6f)
        )

        // Inner ring
        val innerPadding = ringPadding + 10f
        drawOval(
            color = RingGoldDark.copy(alpha = 0.5f),
            topLeft = Offset(innerPadding, innerPadding),
            size = Size(size.width - innerPadding * 2, size.height - innerPadding * 2),
            style = Stroke(width = 2f)
        )

        // Center divider line (dashed)
        drawLine(
            color = RingGold.copy(alpha = 0.3f),
            start = Offset(size.width / 2, ringPadding),
            end = Offset(size.width / 2, size.height - ringPadding),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        // Draw thumbs — pinning thumb on top
        val thumb1OnTop = gameState.thumb1.isPinning
        if (thumb1OnTop) {
            drawThumb2(gameState, canvasSize)
            drawThumb1(gameState, canvasSize)
        } else {
            drawThumb1(gameState, canvasSize)
            drawThumb2(gameState, canvasSize)
        }
    }
}

private fun DrawScope.drawThumb1(state: GameState, canvasSize: Size) {
    with(ThumbCanvas) {
        drawThumb(
            state = state.thumb1,
            canvasSize = canvasSize,
            colors = ThumbCanvas.player1Colors,
            isSquished = state.thumb1.isPinned
        )
    }
}

private fun DrawScope.drawThumb2(state: GameState, canvasSize: Size) {
    with(ThumbCanvas) {
        drawThumb(
            state = state.thumb2,
            canvasSize = canvasSize,
            colors = ThumbCanvas.player2Colors,
            isSquished = state.thumb2.isPinned
        )
    }
}
