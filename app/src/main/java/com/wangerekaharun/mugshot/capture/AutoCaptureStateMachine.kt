package com.wangerekaharun.mugshot.capture

import com.wangerekaharun.mugshot.model.CaptureState
import com.wangerekaharun.mugshot.model.FaceQuality

/**
 * TODO: Workshop Step 3 -- Implement the auto-capture state machine
 *
 * States: NoFace -> Poor -> Adjusting -> Stabilizing -> Captured
 *
 * Transition rules:
 * - NoFace -> Poor: face detected
 * - Poor -> Adjusting: >= 3 of 5 quality signals pass
 * - Adjusting -> Stabilizing: all signals pass for N consecutive frames
 * - Stabilizing -> Captured: all signals hold for stabilizationDurationMs
 * - Any -> NoFace: face lost
 */
class AutoCaptureStateMachine(
    private val stabilizationDurationMs: Long = 1500L,
    private val requiredConsecutiveFrames: Int = 3,
    private val enableSmileGate: Boolean = true,
) {
    fun getCurrentState(): CaptureState = CaptureState.NoFace

    fun onNewFrame(quality: FaceQuality?): CaptureState {
        // TODO: Implement state transitions
        return if (quality == null) CaptureState.NoFace else CaptureState.Poor(quality)
    }

    fun reset() {
        // TODO: Reset state
    }
}
