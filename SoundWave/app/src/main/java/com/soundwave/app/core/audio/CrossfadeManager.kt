package com.soundwave.app.core.audio

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrossfadeManager @Inject constructor() {
    private var crossfadeDurationMs: Int = 3000

    fun setCrossfadeDuration(ms: Int) {
        crossfadeDurationMs = ms.coerceIn(0, 12000)
    }

    fun getCrossfadeDuration() = crossfadeDurationMs
}
