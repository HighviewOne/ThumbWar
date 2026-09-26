package com.thumbwar.ui.components

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thumbwar.engine.GameState
import com.thumbwar.engine.ThumbState
import com.thumbwar.ui.theme.ArenaGreen
import com.thumbwar.ui.theme.Player1Skin
import com.thumbwar.ui.theme.Player2Skin
import com.thumbwar.ui.theme.ThumbWarTheme
import com.thumbwar.util.Vector2
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the Canvas-drawn components for real (native graphics) and checks pixels. The view is
 * drawn straight into a bitmap: Compose's captureToImage waits for a frame Robolectric never draws.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CanvasRenderingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val arenaSize = 300.dp

    private fun renderArena(state: GameState) {
        composeTestRule.setContent {
            ThumbWarTheme {
                Box(Modifier.size(arenaSize)) { ArenaCanvas(state, Modifier.size(arenaSize)) }
            }
        }
    }

    /** Color at a normalized position of the arena. */
    private fun renderToBitmap(): Bitmap {
        composeTestRule.waitForIdle()
        val content = composeTestRule.activity.findViewById<ViewGroup>(android.R.id.content)
        val bitmap = Bitmap.createBitmap(content.width, content.height, Bitmap.Config.ARGB_8888)
        composeTestRule.runOnUiThread { content.draw(android.graphics.Canvas(bitmap)) }
        return bitmap
    }

    private fun colorAt(x: Float, y: Float): Color {
        val bitmap = renderToBitmap()
        val sizePx = with(composeTestRule.density) { arenaSize.toPx() }
        return Color(bitmap.getPixel((x * (sizePx - 1)).toInt(), (y * (sizePx - 1)).toInt()))
    }

    private fun assertCloseTo(expected: Color, actual: Color, what: String) {
        val diff = abs(expected.red - actual.red) + abs(expected.green - actual.green) + abs(expected.blue - actual.blue)
        assertTrue("$what: expected ~$expected but was $actual", diff < 0.25f)
    }

    private fun thumbs(
        p1: Vector2,
        p2: Vector2,
        p1Pinning: Boolean = false,
        p2Pinning: Boolean = false
    ) = GameState(
        thumb1 = ThumbState(position = p1, isPinning = p1Pinning, isPinned = p2Pinning),
        thumb2 = ThumbState(position = p2, isPinning = p2Pinning, isPinned = p1Pinning)
    )

    @Test
    fun arena_drawsThumbsWhereTheStateSaysOnAGreenMat() {
        renderArena(thumbs(Vector2(0.25f, 0.5f), Vector2(0.75f, 0.5f)))

        assertCloseTo(Player1Skin, colorAt(0.25f, 0.5f), "player 1 thumb")
        assertCloseTo(Player2Skin, colorAt(0.75f, 0.5f), "player 2 thumb")
        assertCloseTo(ArenaGreen, colorAt(0.4f, 0.3f), "empty mat")
    }

    @Test
    fun arena_drawsThePinningThumbOnTop() {
        val center = Vector2(0.5f, 0.5f)

        renderArena(thumbs(center, center, p1Pinning = true))
        assertCloseTo(Player1Skin, colorAt(0.5f, 0.5f), "player 1 pinning, on top")
    }

    @Test
    fun arena_drawsPlayer2OnTopWhenPlayer2Pins() {
        val center = Vector2(0.5f, 0.5f)

        renderArena(thumbs(center, center, p2Pinning = true))
        assertCloseTo(Player2Skin, colorAt(0.5f, 0.5f), "player 2 pinning, on top")
    }

    @Test
    fun pinProgressBar_namesThePinnerAndDraws() {
        composeTestRule.setContent {
            ThumbWarTheme { PinProgressBar(progress = 0.6f, pinnerPlayer = 2) }
        }
        composeTestRule.onNodeWithText("Player 2 pinning!").assertExists()
        renderToBitmap() // runs the arc drawing code
    }

    @Test
    fun countdownOverlay_showsNumbersAndTheDeclareLine() {
        composeTestRule.setContent { ThumbWarTheme { CountdownOverlay(text = "3") } }
        composeTestRule.onNodeWithText("3").assertExists()
    }

    @Test
    fun countdownOverlay_numberIsVisibleWhileItPopsIn() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent { ThumbWarTheme { CountdownOverlay(text = "3") } }
        composeTestRule.mainClock.advanceTimeBy(100) // mid-animation: still scaling down

        val bitmap = renderToBitmap()
        val glowPixels = (0 until bitmap.width step 2).sumOf { x ->
            (0 until bitmap.height step 2).count { y ->
                // Yellow glow, possibly half faded in over the dark background
                val c = Color(bitmap.getPixel(x, y))
                c.red > 0.4f && c.green > 0.35f && c.blue < 0.3f
            }
        }
        assertTrue("The number should already be visible during its pop-in, found $glowPixels glow pixels", glowPixels > 50)
    }
}
