package com.thumbwar

import android.os.SystemClock
import com.thumbwar.ui.screens.game.FakeFrameClock
import com.thumbwar.ui.screens.game.GameViewModel

/**
 * The app as Robolectric runs it. Games made through GameViewModel.Factory get a 60 Hz fake
 * frame clock on Robolectric's own clock: the real vsync clock never lets a test go idle.
 */
class TestThumbWarApplication : ThumbWarApplication() {
    override fun onCreate() {
        super.onCreate()
        GameViewModel.newFrameClock = { FakeFrameClock { SystemClock.uptimeMillis() } }
    }
}
