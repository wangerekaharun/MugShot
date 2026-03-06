package com.wangerekaharun.mugshot.model

sealed class CaptureState {
    data object NoFace : CaptureState()
    data class Poor(val quality: FaceQuality) : CaptureState()
    data class Adjusting(val quality: FaceQuality) : CaptureState()
    data class Stabilizing(
        val quality: FaceQuality,
        val startTimeMillis: Long,
        val consecutiveGoodFrames: Int,
    ) : CaptureState()
    data class Captured(val quality: FaceQuality) : CaptureState()
}
