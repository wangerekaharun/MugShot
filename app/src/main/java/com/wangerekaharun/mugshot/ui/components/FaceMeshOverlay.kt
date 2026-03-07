package com.wangerekaharun.mugshot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

@Composable
fun FaceMeshOverlay(
    result: FaceLandmarkerResult?,
    viewWidth: Float,
    viewHeight: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        result?.faceLandmarks()?.firstOrNull()?.let { landmarks ->
            landmarks.forEach { landmark ->
                val x = landmark.x() * viewWidth
                val y = landmark.y() * viewHeight
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.9f),
                    radius = 4f,
                    center = Offset(x, y),
                )
            }
        }
    }
}
