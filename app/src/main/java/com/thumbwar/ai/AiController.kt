package com.thumbwar.ai

import com.thumbwar.engine.ArenaScale
import com.thumbwar.engine.GameConfig
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

    // While pinned: the direction (world units) the computer is running to break free, kept
    // between decisions so it runs instead of zig-zagging in place
    private var escapeDir: Vector2? = null
    private var jukeTimer = 0L

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
        escapeDir = null
        jukeTimer = 0L
    }

    fun update(state: GameState, deltaMs: Long): Vector2? {
        timeSinceLastDecision += deltaMs

        if (timeSinceLastDecision < reactionDelay) {
            return currentTarget
        }
        val sinceLastDecision = timeSinceLastDecision
        timeSinceLastDecision = 0L
        if (!state.thumb2.isPinned) escapeDir = null

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
            AiDifficulty.HARD -> updateHard(myPos, opponentPos, distance, state, sinceLastDecision)
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
        if (state.thumb2.isPinned) {
            // Run sideways, toward whichever side has more room, and keep going that way
            currentTarget = escapeTarget(myPos, opponentPos, state.arenaScale, MEDIUM_ESCAPE_ANGLES)
            return
        }
        aiState = when {
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

    private fun updateHard(
        myPos: Vector2,
        opponentPos: Vector2,
        distance: Float,
        state: GameState,
        sinceLastDecision: Long
    ) {
        if (state.thumb2.isPinned) {
            hardEscape(myPos, opponentPos, state.arenaScale, sinceLastDecision)
            return
        }
        // Predict opponent movement
        val opponentVel = state.thumb1.velocity
        val predictedPos = opponentPos + opponentVel * 0.1f

        aiState = when {
            distance < 0.12f -> AiState.ATTACK
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

    /**
     * Hard runs for the roomiest of several directions, then every so often cuts sharply the
     * other way: a pinner whose finger trails behind overshoots, and the pinned thumb gets clear.
     */
    private fun hardEscape(myPos: Vector2, opponentPos: Vector2, scale: ArenaScale, sinceLastDecision: Long) {
        val dir = escapeDir
        if (dir != null) {
            jukeTimer -= sinceLastDecision
            if (jukeTimer <= 0) {
                val away = awayFrom(myPos, opponentPos, scale)
                // Mirror the run across the line away from the pinner: a sudden cut to the other side
                escapeDir = away * (2f * dir.dot(away)) - dir
                jukeTimer = random.nextLong(HARD_JUKE_MIN_MS, HARD_JUKE_MAX_MS + 1)
            }
        } else {
            jukeTimer = random.nextLong(HARD_JUKE_MIN_MS, HARD_JUKE_MAX_MS + 1)
        }
        currentTarget = escapeTarget(myPos, opponentPos, scale, HARD_ESCAPE_ANGLES)
    }

    /**
     * Where to head while pinned. Keeps the current escape direction while it still has room;
     * otherwise picks, from [anglesDeg] around "straight away from the pinner", the direction
     * with the most room before a wall.
     */
    private fun escapeTarget(myPos: Vector2, opponentPos: Vector2, scale: ArenaScale, anglesDeg: List<Float>): Vector2 {
        val world = scale.toWorld(myPos)
        val current = escapeDir
        val dir = if (current != null && roomAlong(world, current, scale) >= ESCAPE_MIN_ROOM) {
            current
        } else {
            val away = awayFrom(myPos, opponentPos, scale)
            // Ties (e.g. dead centre) go either way, so the computer isn't predictable
            anglesDeg.shuffled(random).map { rotate(away, Math.toRadians(it.toDouble()).toFloat()) }
                .maxBy { roomAlong(world, it, scale) }
        }
        escapeDir = dir
        return (myPos + scale.toNormalized(dir * ESCAPE_REACH)).clamp(
            GameConfig.ARENA_MIN_X,
            GameConfig.ARENA_MIN_Y,
            GameConfig.ARENA_MAX_X,
            GameConfig.ARENA_MAX_Y
        )
    }

    /** Unit direction (world units) from the pinner to the pinned thumb; random if they sit exactly on top. */
    private fun awayFrom(myPos: Vector2, opponentPos: Vector2, scale: ArenaScale): Vector2 {
        val away = scale.toWorld(myPos - opponentPos).normalized()
        if (away != Vector2.ZERO) return away
        val angle = random.nextFloat() * 2f * Math.PI.toFloat()
        return Vector2(cos(angle), sin(angle))
    }

    private fun randomNearby(pos: Vector2, range: Float): Vector2 {
        return Vector2(
            (pos.x + random.nextFloat() * range * 2 - range).coerceIn(0.05f, 0.95f),
            (pos.y + random.nextFloat() * range * 2 - range).coerceIn(0.05f, 0.95f)
        )
    }

    private companion object {
        // How far ahead (world units) the escape target sits: far enough to run at full speed
        const val ESCAPE_REACH = 0.3f

        // Below this much room (world units) before a wall, pick a new escape direction
        const val ESCAPE_MIN_ROOM = GameConfig.THUMB_RADIUS * 1.5f

        // Escape directions tried, in degrees from straight away from the pinner
        val MEDIUM_ESCAPE_ANGLES = listOf(-90f, 90f)
        val HARD_ESCAPE_ANGLES = listOf(-90f, -45f, 0f, 45f, 90f)

        // Hard cuts back the other way after running this long
        const val HARD_JUKE_MIN_MS = 300L
        const val HARD_JUKE_MAX_MS = 600L
    }
}

private fun rotate(v: Vector2, angle: Float): Vector2 =
    Vector2(v.x * cos(angle) - v.y * sin(angle), v.x * sin(angle) + v.y * cos(angle))

/** Distance (world units) from [world] along unit direction [dir] to the arena's padded edge. */
internal fun roomAlong(world: Vector2, dir: Vector2, scale: ArenaScale): Float {
    fun axis(pos: Float, d: Float, min: Float, max: Float): Float = when {
        d > 0.0001f -> (max - pos) / d
        d < -0.0001f -> (min - pos) / d
        else -> Float.MAX_VALUE
    }
    val rx = axis(world.x, dir.x, GameConfig.ARENA_MIN_X * scale.x, GameConfig.ARENA_MAX_X * scale.x)
    val ry = axis(world.y, dir.y, GameConfig.ARENA_MIN_Y * scale.y, GameConfig.ARENA_MAX_Y * scale.y)
    return minOf(rx, ry).coerceAtLeast(0f)
}
