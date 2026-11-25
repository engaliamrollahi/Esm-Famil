package com.example.audio

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var isSoundEnabled: Boolean = true

    private var toneGenerator: ToneGenerator? = null

    private fun getOrCreateToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            toneGenerator = runCatching {
                ToneGenerator(AudioManager.STREAM_MUSIC, 65)
            }.getOrNull()
        }
        return toneGenerator
    }

    fun playTap() {
        if (!isSoundEnabled) return
        playTone(ToneGenerator.TONE_PROP_BEEP, 45)
    }

    fun playLetterStart() {
        if (!isSoundEnabled) return
        scope.launch {
            playToneImmediate(ToneGenerator.TONE_PROP_ACK, 90)
            delay(110)
            playToneImmediate(ToneGenerator.TONE_PROP_BEEP2, 140)
        }
    }

    fun playTickWarning() {
        if (!isSoundEnabled) return
        playTone(ToneGenerator.TONE_PROP_BEEP, 60)
    }

    fun playRoundStop() {
        if (!isSoundEnabled) return
        scope.launch {
            playToneImmediate(ToneGenerator.TONE_PROP_PROMPT, 180)
            delay(190)
            playToneImmediate(ToneGenerator.TONE_PROP_ACK, 200)
        }
    }

    fun playVictoryChime() {
        if (!isSoundEnabled) return
        scope.launch {
            playToneImmediate(ToneGenerator.TONE_DTMF_1, 90)
            delay(110)
            playToneImmediate(ToneGenerator.TONE_DTMF_5, 90)
            delay(110)
            playToneImmediate(ToneGenerator.TONE_DTMF_9, 180)
        }
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        scope.launch {
            playToneImmediate(toneType, durationMs)
        }
    }

    private fun playToneImmediate(toneType: Int, durationMs: Int) {
        runCatching {
            getOrCreateToneGenerator()?.startTone(toneType, durationMs)
        }
    }

    fun release() {
        runCatching {
            toneGenerator?.release()
            toneGenerator = null
        }
    }
}
