package com.wangerekaharun.mugshot.ui.camera

import android.Manifest
import android.net.Uri
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wangerekaharun.mugshot.model.CaptureState
import com.wangerekaharun.mugshot.model.FaceQuality
import com.wangerekaharun.mugshot.ui.components.CaptureButton
import com.wangerekaharun.mugshot.ui.components.GuideOverlay
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
            // TODO: Workshop Step 2 -- Add ImageAnalysis use case here
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Guide overlay (static white oval)
        GuideOverlay(
            captureState = CaptureState.NoFace,
        )

        // Capture button
        CaptureButton(
            onClick = {
                val outputFile = File(context.cacheDir, "mugshot_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val uri = Uri.fromFile(outputFile)
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onImageCaptured(uri, FaceQuality())
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        )
    }
}
