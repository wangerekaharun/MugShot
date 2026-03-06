package com.wangerekaharun.mugshot.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wangerekaharun.mugshot.model.FaceQuality
import com.wangerekaharun.mugshot.ui.camera.CameraScreen
import com.wangerekaharun.mugshot.ui.confirmation.CaptureConfirmationScreen

@Composable
fun MugShotNavigation() {
    val navController = rememberNavController()
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedQuality by remember { mutableStateOf(FaceQuality()) }

    NavHost(
        navController = navController,
        startDestination = "camera"
    ) {
        composable("camera") {
            CameraScreen(
                onImageCaptured = { uri, quality ->
                    capturedImageUri = uri
                    capturedQuality = quality
                    navController.navigate("confirmation") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("confirmation") {
            capturedImageUri?.let { uri ->
                CaptureConfirmationScreen(
                    imageUri = uri,
                    quality = capturedQuality,
                    onAccept = {
                        navController.popBackStack("camera", inclusive = false)
                    },
                    onRetake = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
