package com.wangerekaharun.mugshot.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * TODO: Workshop Step 2 -- Implement MLKit face detection
 *
 * This analyzer will:
 * 1. Extract the image from ImageProxy
 * 2. Run MLKit FaceDetection
 * 3. Map the detected face to a FaceQuality object
 * 4. Call onFaceDetected callback with the result
 */
class FaceAnalyzer(
    private val onFaceDetected: (com.wangerekaharun.mugshot.model.FaceQuality?, Int, Int) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        // TODO: Implement face detection here
        imageProxy.close() // Always close the imageProxy!
    }
}
