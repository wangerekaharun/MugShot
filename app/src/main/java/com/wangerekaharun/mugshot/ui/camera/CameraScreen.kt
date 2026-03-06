package com.wangerekaharun.mugshot.ui.camera

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wangerekaharun.mugshot.analysis.CoordinateTransformer
import com.wangerekaharun.mugshot.analysis.FaceAnalyzer
import com.wangerekaharun.mugshot.capture.AutoCaptureStateMachine
import com.wangerekaharun.mugshot.model.CaptureState
import com.wangerekaharun.mugshot.model.FaceQuality
import com.wangerekaharun.mugshot.analysis.MediaPipeFaceMeshAnalyzer
import com.wangerekaharun.mugshot.ui.components.CaptureButton
import com.wangerekaharun.mugshot.ui.components.FaceOverlay
import com.wangerekaharun.mugshot.ui.components.FaceMeshOverlay
import com.wangerekaharun.mugshot.ui.components.GuideOverlay
import com.wangerekaharun.mugshot.ui.components.RejectionMessageDisplay
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onImageCaptured: (Uri, FaceQuality) -> Unit = { _, _ -> },
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraContent(onImageCaptured = onImageCaptured)
    } else {
        PermissionRequest(onRequestPermission = { cameraPermissionState.launchPermissionRequest() })
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MugShot needs camera access",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun CameraContent(
    onImageCaptured: (Uri, FaceQuality) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var faceQuality by remember { mutableStateOf<FaceQuality?>(null) }
    var captureState by remember { mutableStateOf<CaptureState>(CaptureState.NoFace) }
    var transformedBoundingBox by remember { mutableStateOf<Rect?>(null) }
    var viewWidth by remember { mutableStateOf(0f) }
    var viewHeight by remember { mutableStateOf(0f) }
    var frameWidth by remember { mutableIntStateOf(0) }
    var frameHeight by remember { mutableIntStateOf(0) }
    var showFlash by remember { mutableStateOf(false) }
    var hasAutoCaptured by remember { mutableStateOf(false) }
    var meshResult by remember { mutableStateOf<FaceLandmarkerResult?>(null) }

    val stateMachine = remember { AutoCaptureStateMachine() }
    val meshAnalyzer = remember { MediaPipeFaceMeshAnalyzer(context) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val preview = remember {
        Preview.Builder().build().apply {
            setSurfaceProvider { request ->
                surfaceRequest = request
            }
        }
    }

    val faceAnalyzer = remember {
        FaceAnalyzer { quality, fw, fh ->
            faceQuality = quality
            frameWidth = fw
            frameHeight = fh
            captureState = stateMachine.onNewFrame(quality)
            // Update mesh result from MediaPipe
            meshResult = meshAnalyzer.latestResult
        }
    }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    // Feed MediaPipe with bitmap
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        try {
                            val bitmap = imageProxy.toBitmap()
                            val rotation = imageProxy.imageInfo.rotationDegrees
                            val rotatedBitmap = if (rotation != 0) {
                                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                            } else {
                                bitmap
                            }
                            meshAnalyzer.detectAsync(rotatedBitmap, System.currentTimeMillis())
                        } catch (_: Exception) {
                            // MediaPipe processing is best-effort
                        }
                    }
                    // Then run MLKit face analysis
                    faceAnalyzer.analyze(imageProxy)
                }
            }
    }

    // Bind camera use cases
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis,
        )
    }

    // Transform bounding box coordinates
    LaunchedEffect(faceQuality, viewWidth, viewHeight, frameWidth, frameHeight) {
        if (viewWidth > 0 && viewHeight > 0 && frameWidth > 0 && frameHeight > 0 && faceQuality != null) {
            val transformer = CoordinateTransformer(
                imageWidth = frameWidth,
                imageHeight = frameHeight,
                viewWidth = viewWidth,
                viewHeight = viewHeight,
            )
            transformedBoundingBox = transformer.transformRect(faceQuality!!.boundingBox)
        } else {
            transformedBoundingBox = null
        }
    }

    // Handle auto-capture
    LaunchedEffect(captureState) {
        if (captureState is CaptureState.Captured && !hasAutoCaptured) {
            hasAutoCaptured = true
            val quality = (captureState as CaptureState.Captured).quality

            // Haptic feedback
            triggerHaptic(context)

            // Flash effect
            showFlash = true
            delay(200)
            showFlash = false

            // Take picture
            takePicture(context, imageCapture, cameraExecutor) { uri ->
                onImageCaptured(uri, quality)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            meshAnalyzer.close()
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                viewWidth = coordinates.size.width.toFloat()
                viewHeight = coordinates.size.height.toFloat()
            }
    ) {
        // Camera preview
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Guide overlay
        GuideOverlay(
            captureState = captureState,
        )

        // MediaPipe face mesh overlay
        FaceMeshOverlay(
            result = meshResult,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
        )

        // Face bounding box overlay
        FaceOverlay(
            boundingBox = transformedBoundingBox,
            captureState = captureState,
        )

        // Rejection messages
        RejectionMessageDisplay(
            quality = faceQuality,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp),
        )

        // Debug quality readout
        faceQuality?.let { q ->
            Text(
                text = "Yaw: %.1f  Pitch: %.1f  Smile: %.0f%%".format(
                    q.eulerAngleY, q.eulerAngleX, q.smilingProbability * 100
                ),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 80.dp),
            )
        }

        // Capture button (manual fallback)
        CaptureButton(
            onClick = {
                val quality = faceQuality ?: FaceQuality()
                triggerHaptic(context)
                takePicture(context, imageCapture, cameraExecutor) { uri ->
                    onImageCaptured(uri, quality)
                }
            },
            enabled = captureState !is CaptureState.Captured,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        )

        // Shutter flash effect
        AnimatedVisibility(
            visible = showFlash,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }
    }
}

private fun triggerHaptic(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = vibratorManager.defaultVibrator
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    executor: java.util.concurrent.ExecutorService,
    onSaved: (Uri) -> Unit,
) {
    val outputFile = File(context.cacheDir, "mugshot_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = Uri.fromFile(outputFile)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onSaved(uri)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}
