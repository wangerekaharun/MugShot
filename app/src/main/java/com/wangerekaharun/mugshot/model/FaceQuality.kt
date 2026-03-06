package com.wangerekaharun.mugshot.model

import android.graphics.RectF

data class FaceQuality(
    val boundingBox: RectF = RectF(),
    val eulerAngleX: Float = 0f,
    val eulerAngleY: Float = 0f,
    val eulerAngleZ: Float = 0f,
    val smilingProbability: Float = 0f,
    val leftEyeOpenProbability: Float = 0f,
    val rightEyeOpenProbability: Float = 0f,
    val detectionConfidence: Float = 0f,
    val faceSizeRatio: Float = 0f,
    val centerOffsetX: Float = 0f,
    val centerOffsetY: Float = 0f,
) {
    companion object Thresholds {
        const val MAX_YAW = 10f
        const val MAX_PITCH = 10f
        const val MAX_TILT = 8f
        const val MIN_SMILE = 0.7f
        const val MIN_EYE_OPEN = 0.5f
        const val MIN_FACE_SIZE = 0.15f
        const val MAX_FACE_SIZE = 0.45f
        const val MAX_CENTER_OFFSET = 0.10f
    }

    val isYawGood: Boolean get() = kotlin.math.abs(eulerAngleY) < MAX_YAW
    val isPitchGood: Boolean get() = kotlin.math.abs(eulerAngleX) < MAX_PITCH
    val isTiltGood: Boolean get() = kotlin.math.abs(eulerAngleZ) < MAX_TILT
    val isSmiling: Boolean get() = smilingProbability > MIN_SMILE
    val isEyesOpen: Boolean get() = leftEyeOpenProbability > MIN_EYE_OPEN && rightEyeOpenProbability > MIN_EYE_OPEN
    val isFaceSizeGood: Boolean get() = faceSizeRatio in MIN_FACE_SIZE..MAX_FACE_SIZE
    val isCentered: Boolean get() = kotlin.math.abs(centerOffsetX) < MAX_CENTER_OFFSET && kotlin.math.abs(centerOffsetY) < MAX_CENTER_OFFSET

    val isPoseGood: Boolean get() = isYawGood && isPitchGood && isTiltGood
    val isAllGood: Boolean get() = isPoseGood && isCentered && isFaceSizeGood && isSmiling
    val isAllGoodExceptSmile: Boolean get() = isPoseGood && isCentered && isFaceSizeGood

    val passingSignalCount: Int get() = listOf(
        isYawGood, isPitchGood, isTiltGood, isCentered, isFaceSizeGood
    ).count { it }

    val totalSignals: Int get() = 5
}
