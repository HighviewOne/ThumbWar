package com.thumbwar.ai

import com.thumbwar.engine.GameState
import com.thumbwar.util.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class AiController(
    private val difficulty: AiDifficulty,
    private val random: Random = Random.Default
) {

    private var timeSinceLastDecision = 0L
    private var currentTarget: Vector2? = null
    private var aiState: AiState = AiState.IDLE
    private var restTimer = 0L

    private enum class AiState { IDLE, APPROACH, DODGE, ATTACK, REST }

    private val reactionDelay: Long
    private val speed: Float
    private val mistakeChance: Float

    init {
        when (difficulty) {
            AiDifficulty.EASY -> {
                reactionDelay = 400L
                speed = 0.3f
                mistakeChance = 0.3f
            }
            AiDifficulty.MEDIUM -> {
                reactionDelay = 200L
                speed = 0.6f
                mistakeChance = 0.15f
            }
            AiDifficulty.HARD -> {
                reactionDelay = 80L
                speed = 0.9f
                mistakeChance = 0.1f
            }
        }
    }

    /** Forgets targets and timers from the previous round. */
    fun reset() {
        timeSinceLastDecision = 0L
        currentTarget = null
        aiState = AiState.IDLE
        restTimer = 0L
    }

    fun update(state: GameState, deltaMs: Long): Vector2? {
        timeSinceLastDecision += deltaMs

        if (timeSinceLastDecision < reactionDelay) {
            return currentTarget
        }
        timeSinceLastDecision = 0L

        val myPos = state.thumb2.position
        val opponentPos = state.thumb1.position
        val distance = state.arenaScale.distance(myPos, opponentPos)

        // Rest behavior for easy AI
        if (aiState == AiState.REST) {
            restTimer -= reactionDelay
            if (restTimer <= 0) {
                aiState = AiState.IDLE
            }
            return currentTarget
        }

        // Deliberate mistakes
        if (random.nextFloat() < mistakeChance) {
            currentTarget = randomNearby(myPos, 0.1f)
            return currentTarget
        }

        when (difficulty) {
            AiDifficulty.EASY -> updateEasy(myPos, opponentPos, distance)
            AiDifficulty.MEDIUM -> updateMedium(myPos, opponentPos, distance, state)
            AiDifficulty.HARD -> updateHard(myPos, opponentPos, distance, state)
        }

        return currentTarget
    }

    private fun updateEasy(myPos: Vector2, opponentPos: Vector2, distance: Float) {
        // Slow, random movement with frequent rests
        if (random.nextFloat() < 0.15f) {
            aiState = AiState.REST
            restTimer = random.nextLong(500, 1501)
            return
        }

        currentTarget = if (distance > 0.3f) {
            // Wander randomly
            randomNearby(myPos, 0.15f)
        } else {
            // Slowly approach
            myPos.lerp(opponentPos, speed * 0.3f)
        }
    }

    private fun updateMedium(myPos: Vector2, opponentPos: Vector2, distance: Float, state: GameState) {
        aiState = when {
            state.thumb2.isPinned -> AiState.DODGE
            distance < 0.15f -> AiState.ATTACK
            distance < 0.3f -> if (random.nextFloat() < 0.5f) AiState.APPROACH else AiState.DODGE
            else -> AiState.APPROACH
        }

        currentTarget = when (aiState) {
            AiState.APPROACH -> myPos.lerp(opponentPos, speed * 0.5f)
            AiState.DODGE -> {
                val away = (myPos - opponentPos).normalized()
                val perpendicular = Vector2(-away.y, away.x)
                val dodgeDir = if (random.nextBoolean()) perpendicular else perpendicular * -1f
                myPos + dodgeDir * 0.1f
            }
            AiState.ATTACK -> opponentPos
            else -> myPos
        }
    }

    private fun updateHard(myPos: Vector2, opponentPos: Vector2, distance: Float, state: GameState) {
        // Predict opponent movement
        val opponentVel = state.thumb1.velocity
        val predictedPos = opponentPos + opponentVel * 0.1f

        aiState = when {
            state.thumb2.isPinned -> AiState.DODGE
            distance < 0.12f && !state.thumb2.isPinned -> AiState.ATTACK
            distance < 0.25f -> {
                // Feint behavior
                if (random.nextFloat() < 0.3f) AiState.DODGE else AiState.ATTACK
            }
            else -> AiState.APPROACH
        }

        currentTarget = when (aiState) {
            AiState.APPROACH -> myPos.lerp(predictedPos, speed * 0.7f)
            AiState.DODGE -> {
                val away = (myPos - opponentPos).normalized()
                val angle = random.nextFloat() * Math.PI.toFloat() * 0.5f - Math.PI.toFloat() * 0.25f
                val dodgeDir = Vector2(
                    away.x * cos(angle) - away.y * sin(angle),
                    away.x * sin(angle) + away.y * cos(angle)
                )
                myPos + dodgeDir * 0.15f
            }
            AiState.ATTACK -> predictedPos
            else -> myPos
        }
    }

    private fun randomNearby(pos: Vector2, range: Float): Vector2 {
        return Vector2(
            (pos.x + random.nextFloat() * range * 2 - range).coerceIn(0.05f, 0.95f),
            (pos.y + random.nextFloat() * range * 2 - range).coerceIn(0.05f, 0.95f)
        )
    }
}
