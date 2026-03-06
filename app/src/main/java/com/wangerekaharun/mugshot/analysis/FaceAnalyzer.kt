package com.wangerekaharun.mugshot.analysis

import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.wangerekaharun.mugshot.model.FaceQuality

class FaceAnalyzer(
    private val onFaceDetected: (FaceQuality?, Int, Int) -> Unit,
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.25f)
            .build()
    )

    private var frameSkipCounter = 0
    private val analyzeEveryNFrames = 3

    override fun analyze(imageProxy: ImageProxy) {
        frameSkipCounter++
        if (frameSkipCounter % analyzeEveryNFrames != 0) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val imageWidth = imageProxy.width
        val imageHeight = imageProxy.height
        val rotation = imageProxy.imageInfo.rotationDegrees

        val frameWidth: Int
        val frameHeight: Int
        if (rotation == 90 || rotation == 270) {
            frameWidth = imageHeight
            frameHeight = imageWidth
        } else {
            frameWidth = imageWidth
            frameHeight = imageHeight
        }

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    onFaceDetected(null, frameWidth, frameHeight)
                } else {
                    val face = faces.maxByOrNull {
                        it.boundingBox.width() * it.boundingBox.height()
                    } ?: return@addOnSuccessListener

                    val quality = mapFaceToQuality(face, frameWidth, frameHeight)
                    onFaceDetected(quality, frameWidth, frameHeight)
                }
            }
            .addOnFailureListener {
                onFaceDetected(null, frameWidth, frameHeight)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun mapFaceToQuality(face: Face, frameWidth: Int, frameHeight: Int): FaceQuality {
        val box = face.boundingBox
        val faceArea = box.width().toFloat() * box.height().toFloat()
        val frameArea = frameWidth.toFloat() * frameHeight.toFloat()

        val faceCenterX = box.centerX().toFloat()
        val faceCenterY = box.centerY().toFloat()
        val frameCenterX = frameWidth / 2f
        val frameCenterY = frameHeight / 2f

        return FaceQuality(
            boundingBox = RectF(
                box.left.toFloat(),
                box.top.toFloat(),
                box.right.toFloat(),
                box.bottom.toFloat()
            ),
            eulerAngleX = face.headEulerAngleX,
            eulerAngleY = face.headEulerAngleY,
            eulerAngleZ = face.headEulerAngleZ,
            smilingProbability = face.smilingProbability ?: 0f,
            leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f,
            rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f,
            detectionConfidence = face.trackingId?.toFloat() ?: 1f,
            faceSizeRatio = faceArea / frameArea,
            centerOffsetX = (faceCenterX - frameCenterX) / frameCenterX,
            centerOffsetY = (faceCenterY - frameCenterY) / frameCenterY,
        )
    }
}
