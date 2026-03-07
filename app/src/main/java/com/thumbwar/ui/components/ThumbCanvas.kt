package com.thumbwar.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.thumbwar.engine.GameConfig
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
        val baseRadius = GameConfig.THUMB_RADIUS * canvasSize.width.coerceAtMost(canvasSize.height)

        val scaleX = if (isSquished) 1.35f else 1f
        val scaleY = if (isSquished) 0.65f else 1f
        val bodyWidth = baseRadius * 2.0f * scaleX
        val bodyHeight = baseRadius * 2.8f * scaleY

        val left = cx - bodyWidth / 2
        val top = cy - bodyHeight / 2
        val cornerRadius = CornerRadius(bodyWidth * 0.48f)

        // 1. Drop shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.20f),
            topLeft = Offset(left + 5f, top + 7f),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = cornerRadius
        )

        // 2. Body base fill
        drawRoundRect(
            color = colors.skin,
            topLeft = Offset(left, top),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = cornerRadius
        )

        // 3. Gradient shading — highlight top-left, shadow bottom-right
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.30f),
                    Color.Transparent,
                    colors.skinDark.copy(alpha = 0.28f)
                ),
                start = Offset(left, top),
                end = Offset(left + bodyWidth, top + bodyHeight)
            ),
            topLeft = Offset(left, top),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = cornerRadius
        )

        // 4. Outline
        drawRoundRect(
            color = colors.outline,
            topLeft = Offset(left, top),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = cornerRadius,
            style = Stroke(width = 3.5f)
        )

        // 5. Fingernail
        val nailWidth = bodyWidth * 0.62f
        val nailHeight = bodyHeight * 0.22f
        val nailLeft = cx - nailWidth / 2
        val nailTop = top + bodyHeight * 0.07f
        val nailCorner = CornerRadius(nailWidth * 0.45f)

        drawRoundRect(
            color = colors.nail,
            topLeft = Offset(nailLeft, nailTop),
            size = Size(nailWidth, nailHeight),
            cornerRadius = nailCorner
        )
        // Nail gradient sheen
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                start = Offset(nailLeft, nailTop),
                end = Offset(nailLeft + nailWidth, nailTop + nailHeight)
            ),
            topLeft = Offset(nailLeft, nailTop),
            size = Size(nailWidth, nailHeight),
            cornerRadius = nailCorner
        )
        // Nail outline
        drawRoundRect(
            color = colors.outline.copy(alpha = 0.50f),
            topLeft = Offset(nailLeft, nailTop),
            size = Size(nailWidth, nailHeight),
            cornerRadius = nailCorner,
            style = Stroke(width = 1.5f)
        )

        // 6. Knuckle creases (two arched lines)
        val knuckle1Y = cy + bodyHeight * 0.13f
        val knuckle2Y = cy + bodyHeight * 0.28f
        drawLine(
            color = colors.skinDark.copy(alpha = 0.65f),
            start = Offset(cx - bodyWidth * 0.28f, knuckle1Y),
            end = Offset(cx + bodyWidth * 0.28f, knuckle1Y),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = colors.skinDark.copy(alpha = 0.42f),
            start = Offset(cx - bodyWidth * 0.22f, knuckle2Y),
            end = Offset(cx + bodyWidth * 0.22f, knuckle2Y),
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )

        // 7. Eyes
        val eyeY = cy - bodyHeight * 0.09f
        val eyeSpacing = bodyWidth * 0.21f
        val eyeRadius = baseRadius * 0.17f
        val pupilRadius = eyeRadius * 0.58f

        for (sign in listOf(-1f, 1f)) {
            val ex = cx + sign * eyeSpacing
            // White
            drawCircle(Color.White, radius = eyeRadius, center = Offset(ex, eyeY))
            // Subtle outline
            drawCircle(
                color = colors.outline.copy(alpha = 0.30f),
                radius = eyeRadius,
                center = Offset(ex, eyeY),
                style = Stroke(width = 1.5f)
            )
            if (!state.isPinned) {
                // Pupils looking slightly inward
                val pupilX = ex + sign * eyeRadius * 0.08f
                drawCircle(Color(0xFF1A1A1A), radius = pupilRadius, center = Offset(pupilX, eyeY + 1f))
                // Shine
                drawCircle(
                    Color.White,
                    radius = pupilRadius * 0.38f,
                    center = Offset(pupilX - pupilRadius * 0.30f, eyeY - pupilRadius * 0.30f)
                )
            } else {
                // X eyes when pinned
                val xSize = eyeRadius * 0.62f
                drawLine(colors.outline, Offset(ex - xSize, eyeY - xSize), Offset(ex + xSize, eyeY + xSize), 2.5f, cap = StrokeCap.Round)
                drawLine(colors.outline, Offset(ex + xSize, eyeY - xSize), Offset(ex - xSize, eyeY + xSize), 2.5f, cap = StrokeCap.Round)
            }
        }
    }
}
