package com.thumbwar.input

import com.thumbwar.util.Vector2

sealed class InputEvent {
    data class Move(val player: Int, val normalizedPosition: Vector2) : InputEvent()
    data class Release(val player: Int) : InputEvent()
}
