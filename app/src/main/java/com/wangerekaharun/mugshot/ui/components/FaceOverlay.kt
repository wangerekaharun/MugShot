package com.wangerekaharun.mugshot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.wangerekaharun.mugshot.model.CaptureState
import com.wangerekaharun.mugshot.ui.theme.MugShotGreen
import com.wangerekaharun.mugshot.ui.theme.MugShotRed
import com.wangerekaharun.mugshot.ui.theme.MugShotYellow

@Composable
fun FaceOverlay(
    boundingBox: Rect?,
    captureState: CaptureState,
    modifier: Modifier = Modifier,
) {
    val boxColor = when (captureState) {
        is CaptureState.NoFace -> Color.Transparent
        is CaptureState.Poor -> MugShotRed
        is CaptureState.Adjusting -> MugShotYellow
        is CaptureState.Stabilizing -> MugShotGreen
        is CaptureState.Captured -> MugShotGreen
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        boundingBox?.let { box ->
            drawRoundRect(
                color = boxColor,
                topLeft = Offset(box.left, box.top),
                size = Size(box.width, box.height),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 4f),
            )
        }
    }
}
