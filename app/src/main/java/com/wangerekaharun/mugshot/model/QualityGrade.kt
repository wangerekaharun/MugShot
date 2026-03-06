package com.wangerekaharun.mugshot.model

import androidx.compose.ui.graphics.Color

enum class QualityGrade(val label: String, val color: Color) {
    A_PLUS("A+", Color(0xFF27AE60)),
    B("B", Color(0xFFF39C12)),
    C("C", Color(0xFFE67E22));

    companion object {
        fun fromQuality(quality: FaceQuality): QualityGrade {
            val score = quality.passingSignalCount.toFloat() / quality.totalSignals
            return when {
                score >= 1.0f && quality.isSmiling -> A_PLUS
                score >= 0.8f -> B
                else -> C
            }
        }
    }
}
