package com.thumbwar

import android.os.SystemClock
import com.thumbwar.audio.SoundManager
import com.thumbwar.ui.screens.game.FakeFrameClock
import com.thumbwar.ui.screens.game.GameViewModel

/**
 * The app as Robolectric runs it. Games made through GameViewModel.Factory get a 60 Hz fake
 * frame clock on Robolectric's own clock: the real vsync clock never lets a test go idle. The
 * shared SoundManager is dropped, since it would belong to the previous test's app.
 */
class TestThumbWarApplication : ThumbWarApplication() {
    override fun onCreate() {
        super.onCreate()
        GameViewModel.newFrameClock = { FakeFrameClock { SystemClock.uptimeMillis() } }
        SoundManager.resetShared()
    }
}
