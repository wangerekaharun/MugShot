package com.wangerekaharun.mugshot.analysis

import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

class CoordinateTransformer(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val viewWidth: Float,
    private val viewHeight: Float,
    private val isFrontCamera: Boolean = true,
) {
    private val scaleX: Float = viewWidth / imageWidth.toFloat()
    private val scaleY: Float = viewHeight / imageHeight.toFloat()

    fun transformRect(imageRect: RectF): Rect {
        val left: Float
        val right: Float

        if (isFrontCamera) {
            left = viewWidth - (imageRect.right * scaleX)
            right = viewWidth - (imageRect.left * scaleX)
        } else {
            left = imageRect.left * scaleX
            right = imageRect.right * scaleX
        }

        val top = imageRect.top * scaleY
        val bottom = imageRect.bottom * scaleY

        return Rect(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
        )
    }

    fun transformPoint(x: Float, y: Float): Offset {
        val transformedX = if (isFrontCamera) {
            viewWidth - (x * scaleX)
        } else {
            x * scaleX
        }
        val transformedY = y * scaleY
        return Offset(transformedX, transformedY)
    }
}
