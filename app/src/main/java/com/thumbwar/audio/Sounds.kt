package com.thumbwar.audio

import androidx.annotation.RawRes
import com.thumbwar.R

/**
 * Every sound the game makes, with its file in res/raw. The files are from Kenney's CC0 packs
 * (Interface Sounds, RPG Sounds, Music Jingles); see README's credits.
 */
enum class GameSound(@param:RawRes val resId: Int) {
    COUNTDOWN_BEAT(R.raw.sfx_countdown_beat),
    COUNTDOWN_DECLARE(R.raw.sfx_countdown_declare),
    COLLISION_THUD(R.raw.sfx_pin_start),
    PIN_TICK(R.raw.sfx_pin_tick),
    PIN_ESCAPE(R.raw.sfx_pin_escape),
    PIN_COMPLETE(R.raw.sfx_round_won),
    VICTORY_FANFARE(R.raw.sfx_match_won),
    BUTTON_TAP(R.raw.sfx_button_tap)
}
