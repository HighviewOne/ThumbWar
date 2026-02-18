package com.thumbwar.ui.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thumbwar.ai.AiDifficulty
import com.thumbwar.engine.GamePhase
import com.thumbwar.ui.components.ArenaCanvas
import com.thumbwar.ui.components.CountdownOverlay
import com.thumbwar.ui.components.PinProgressBar
import com.thumbwar.ui.components.ScoreDisplay
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun GameScreen(
    isTwoPlayer: Boolean,
    aiDifficulty: AiDifficulty,
    winsNeeded: Int = 1,
    onGameOver: (winner: Int, p1Score: Int, p2Score: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()

    // Force landscape for two-player mode
    val context = LocalContext.current
    DisposableEffect(isTwoPlayer) {
        val activity = context as? Activity
        if (isTwoPlayer) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Back button handling
    BackHandler { onBack() }

    // Lifecycle — pause on background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pause()
                Lifecycle.Event.ON_RESUME -> viewModel.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.initialize(isTwoPlayer, aiDifficulty, winsNeeded)
    }

    // Handle round end vs match end
    LaunchedEffect(gameState.phase, gameState.roundNumber) {
        if (gameState.phase == GamePhase.GAME_OVER) {
            if (gameState.isMatchOver) {
                kotlinx.coroutines.delay(1500)
                onGameOver(gameState.winner, gameState.p1RoundWins, gameState.p2RoundWins)
            } else {
                kotlinx.coroutines.delay(com.thumbwar.engine.GameConfig.ROUND_TRANSITION_DELAY_MS)
                viewModel.startNextRound()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Arena with touch handling
        ArenaCanvas(
            gameState = gameState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()

                            event.changes.forEach { change ->
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        viewModel.processPointerDown(
                                            change.id.value,
                                            change.position.x,
                                            change.position.y,
                                            canvasWidth,
                                            canvasHeight
                                        )
                                        change.consume()
                                    }
                                    PointerEventType.Move -> {
                                        viewModel.processPointerMove(
                                            change.id.value,
                                            change.position.x,
                                            change.position.y,
                                            canvasWidth,
                                            canvasHeight
                                        )
                                        change.consume()
                                    }
                                    PointerEventType.Release -> {
                                        viewModel.processPointerUp(change.id.value)
                                        change.consume()
                                    }
                                }
                            }
                        }
                    }
                }
        )

        // Score display
        ScoreDisplay(
            p1Score = gameState.p1RoundWins,
            p2Score = gameState.p2RoundWins,
            roundNumber = gameState.roundNumber,
            winsNeeded = gameState.winsNeeded
        )

        // Countdown overlay
        if (gameState.phase == GamePhase.COUNTDOWN) {
            CountdownOverlay(
                text = gameState.countdownText,
                beat = gameState.countdownBeat
            )
        }

        // Pin progress bar
        if (gameState.phase == GamePhase.PIN_IN_PROGRESS) {
            PinProgressBar(
                progress = gameState.pinProgress,
                pinnerPlayer = gameState.pinnerPlayer
            )
        }

        // Round transition overlay
        if (gameState.phase == GamePhase.GAME_OVER && !gameState.isMatchOver && gameState.winsNeeded > 1) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Player ${gameState.winner} wins the round!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
