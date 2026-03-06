package com.wangerekaharun.mugshot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.wangerekaharun.mugshot.model.CaptureState
import com.wangerekaharun.mugshot.ui.theme.MugShotGreen
import com.wangerekaharun.mugshot.ui.theme.MugShotRed
import com.wangerekaharun.mugshot.ui.theme.MugShotYellow

@Composable
fun GuideOverlay(
    captureState: CaptureState,
    modifier: Modifier = Modifier,
) {
    val guideColor = when (captureState) {
        is CaptureState.NoFace -> Color.White.copy(alpha = 0.3f)
        is CaptureState.Poor -> MugShotRed.copy(alpha = 0.6f)
        is CaptureState.Adjusting -> MugShotYellow.copy(alpha = 0.6f)
        is CaptureState.Stabilizing -> MugShotGreen.copy(alpha = 0.8f)
        is CaptureState.Captured -> MugShotGreen
    }

    val stabilizingProgress = when (captureState) {
        is CaptureState.Stabilizing -> {
            val elapsed = System.currentTimeMillis() - captureState.startTimeMillis
            (elapsed / 1500f).coerceIn(0f, 1f)
        }
        else -> 0f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val ovalWidth = size.width * 0.6f
        val ovalHeight = size.height * 0.45f
        val centerX = size.width / 2
        val centerY = size.height * 0.4f

        drawOval(
            color = guideColor,
            topLeft = Offset(centerX - ovalWidth / 2, centerY - ovalHeight / 2),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 3f),
        )

        // Countdown ring during stabilizing
        if (stabilizingProgress > 0f) {
            drawArc(
                color = MugShotGreen,
                startAngle = -90f,
                sweepAngle = 360f * stabilizingProgress,
                useCenter = false,
                topLeft = Offset(centerX - ovalWidth / 2 - 4f, centerY - ovalHeight / 2 - 4f),
                size = Size(ovalWidth + 8f, ovalHeight + 8f),
                style = Stroke(width = 6f),
            )
        }
    }
}
