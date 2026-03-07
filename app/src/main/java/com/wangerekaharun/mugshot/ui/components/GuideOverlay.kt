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

/**
 * Draws an oval face guide in the center of the screen.
 * Currently shows a static white oval.
 * TODO: Workshop Step 2 -- Make the color change based on capture state
 */
@Composable
fun GuideOverlay(
    captureState: CaptureState,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val ovalWidth = size.width * 0.6f
        val ovalHeight = size.height * 0.45f
        val centerX = size.width / 2
        val centerY = size.height * 0.4f

        drawOval(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(centerX - ovalWidth / 2, centerY - ovalHeight / 2),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 8f),
        )
    }
}
