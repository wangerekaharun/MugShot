package com.wangerekaharun.mugshot.capture

import com.wangerekaharun.mugshot.model.CaptureState
import com.wangerekaharun.mugshot.model.FaceQuality

class AutoCaptureStateMachine(
    private val stabilizationDurationMs: Long = 1500L,
    private val requiredConsecutiveFrames: Int = 3,
    private val enableSmileGate: Boolean = true,
) {
    private var currentState: CaptureState = CaptureState.NoFace
    private var consecutiveGoodFrames: Int = 0
    private var stabilizationStartTime: Long = 0L

    fun getCurrentState(): CaptureState = currentState

    fun onNewFrame(quality: FaceQuality?): CaptureState {
        if (currentState is CaptureState.Captured) {
            return currentState
        }

        if (quality == null) {
            consecutiveGoodFrames = 0
            stabilizationStartTime = 0L
            currentState = CaptureState.NoFace
            return currentState
        }

        val allGood = if (enableSmileGate) quality.isAllGood else quality.isAllGoodExceptSmile

        currentState = when {
            allGood -> handleAllGood(quality)
            quality.passingSignalCount >= 3 -> handleAdjusting(quality)
            else -> handlePoor(quality)
        }

        return currentState
    }

    private fun handleAllGood(quality: FaceQuality): CaptureState {
        consecutiveGoodFrames++

        if (consecutiveGoodFrames >= requiredConsecutiveFrames) {
            if (stabilizationStartTime == 0L) {
                stabilizationStartTime = System.currentTimeMillis()
            }

            val elapsed = System.currentTimeMillis() - stabilizationStartTime
            return if (elapsed >= stabilizationDurationMs) {
                CaptureState.Captured(quality)
            } else {
                CaptureState.Stabilizing(
                    quality = quality,
                    startTimeMillis = stabilizationStartTime,
                    consecutiveGoodFrames = consecutiveGoodFrames,
                )
            }
        }

        return CaptureState.Adjusting(quality)
    }

    private fun handleAdjusting(quality: FaceQuality): CaptureState {
        consecutiveGoodFrames = 0
        stabilizationStartTime = 0L
        return CaptureState.Adjusting(quality)
    }

    private fun handlePoor(quality: FaceQuality): CaptureState {
        consecutiveGoodFrames = 0
        stabilizationStartTime = 0L
        return CaptureState.Poor(quality)
    }

    fun reset() {
        currentState = CaptureState.NoFace
        consecutiveGoodFrames = 0
        stabilizationStartTime = 0L
    }
}
