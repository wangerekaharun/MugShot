package com.wangerekaharun.mugshot.analysis

import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * TODO: Workshop Step 2 -- Implement coordinate transformation
 *
 * This transformer will convert coordinates from:
 * - ImageProxy coordinate space -> Compose UI coordinate space
 *
 * It must handle:
 * - Scale differences between image and view
 * - Front camera horizontal mirroring
 */
class CoordinateTransformer(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val viewWidth: Float,
    private val viewHeight: Float,
    private val isFrontCamera: Boolean = true,
) {
    fun transformRect(imageRect: RectF): Rect {
        // TODO: Implement coordinate transformation
        return Rect.Zero
    }

    fun transformPoint(x: Float, y: Float): Offset {
        // TODO: Implement point transformation
        return Offset.Zero
    }
}
