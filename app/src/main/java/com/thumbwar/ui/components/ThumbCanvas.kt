package com.thumbwar.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.thumbwar.engine.ThumbState
import com.thumbwar.ui.theme.*

object ThumbCanvas {

    data class ThumbColors(
        val skin: Color,
        val skinDark: Color,
        val outline: Color,
        val nail: Color
    )

    val player1Colors = ThumbColors(
        skin = Player1Skin,
        skinDark = Player1SkinDark,
        outline = Color(0xFF8B6914),
        nail = Color(0xFFFFE4C4)
    )

    val player2Colors = ThumbColors(
        skin = Player2Skin,
        skinDark = Player2SkinDark,
        outline = Color(0xFF4A2810),
        nail = Color(0xFFC4956A)
    )

    fun DrawScope.drawThumb(
        state: ThumbState,
        canvasSize: Size,
        colors: ThumbColors,
        isSquished: Boolean = false
    ) {
        val cx = state.position.x * canvasSize.width
        val cy = state.position.y * canvasSize.height
        val baseRadius = com.thumbwar.engine.GameConfig.THUMB_RADIUS * canvasSize.width.coerceAtMost(canvasSize.height)

        val scaleX = if (isSquished) 1.3f else 1f
        val scaleY = if (isSquished) 0.7f else 1f
        val radiusX = baseRadius * scaleX
        val radiusY = baseRadius * scaleY

        // Body (elongated oval for thumb shape)
        val bodyHeight = radiusY * 2.8f
        val bodyWidth = radiusX * 2f

        // Thumb body — rounded rectangle
        drawOval(
            color = colors.skin,
            topLeft = Offset(cx - bodyWidth / 2, cy - bodyHeight / 2),
            size = Size(bodyWidth, bodyHeight)
        )

        // Outline
        drawOval(
            color = colors.outline,
            topLeft = Offset(cx - bodyWidth / 2, cy - bodyHeight / 2),
            size = Size(bodyWidth, bodyHeight),
            style = Stroke(width = 3f)
        )

        // Nail (top portion)
        val nailWidth = bodyWidth * 0.6f
        val nailHeight = bodyHeight * 0.25f
        drawOval(
            color = colors.nail,
            topLeft = Offset(cx - nailWidth / 2, cy - bodyHeight / 2 + bodyHeight * 0.08f),
            size = Size(nailWidth, nailHeight)
        )

        // Nail shine
        val shineWidth = nailWidth * 0.3f
        val shineHeight = nailHeight * 0.4f
        drawOval(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(cx - shineWidth / 2 - nailWidth * 0.1f, cy - bodyHeight / 2 + bodyHeight * 0.1f),
            size = Size(shineWidth, shineHeight)
        )

        // Knuckle line
        val knuckleY = cy + bodyHeight * 0.15f
        drawLine(
            color = colors.skinDark,
            start = Offset(cx - bodyWidth * 0.3f, knuckleY),
            end = Offset(cx + bodyWidth * 0.3f, knuckleY),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Cartoon eyes
        val eyeY = cy - bodyHeight * 0.08f
        val eyeSpacing = bodyWidth * 0.2f
        val eyeRadius = baseRadius * 0.18f
        val pupilRadius = eyeRadius * 0.55f

        // Left eye white
        drawCircle(Color.White, radius = eyeRadius, center = Offset(cx - eyeSpacing, eyeY))
        // Right eye white
        drawCircle(Color.White, radius = eyeRadius, center = Offset(cx + eyeSpacing, eyeY))

        // Pupils
        drawCircle(Color.Black, radius = pupilRadius, center = Offset(cx - eyeSpacing + 1f, eyeY + 1f))
        drawCircle(Color.Black, radius = pupilRadius, center = Offset(cx + eyeSpacing + 1f, eyeY + 1f))

        // Eye shine
        val shineR = pupilRadius * 0.4f
        drawCircle(Color.White, radius = shineR, center = Offset(cx - eyeSpacing - 1f, eyeY - 1f))
        drawCircle(Color.White, radius = shineR, center = Offset(cx + eyeSpacing - 1f, eyeY - 1f))

        // Pinned indicator — X eyes
        if (state.isPinned) {
            val xSize = eyeRadius * 0.6f
            val xStroke = 2f
            // Left eye X
            drawLine(colors.outline, Offset(cx - eyeSpacing - xSize, eyeY - xSize), Offset(cx - eyeSpacing + xSize, eyeY + xSize), xStroke)
            drawLine(colors.outline, Offset(cx - eyeSpacing + xSize, eyeY - xSize), Offset(cx - eyeSpacing - xSize, eyeY + xSize), xStroke)
            // Right eye X
            drawLine(colors.outline, Offset(cx + eyeSpacing - xSize, eyeY - xSize), Offset(cx + eyeSpacing + xSize, eyeY + xSize), xStroke)
            drawLine(colors.outline, Offset(cx + eyeSpacing + xSize, eyeY - xSize), Offset(cx + eyeSpacing - xSize, eyeY + xSize), xStroke)
        }
    }
}
