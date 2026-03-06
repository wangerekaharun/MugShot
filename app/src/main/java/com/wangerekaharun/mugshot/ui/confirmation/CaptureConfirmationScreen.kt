package com.wangerekaharun.mugshot.ui.confirmation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wangerekaharun.mugshot.model.FaceQuality

/**
 * TODO: Workshop Step 3 -- Build the capture confirmation screen
 *
 * This screen should:
 * 1. Display the captured image using Coil
 * 2. Show a QualityBadge with the grade
 * 3. Offer Retake and Accept buttons
 */
@Composable
fun CaptureConfirmationScreen(
    imageUri: Uri,
    quality: FaceQuality,
    onAccept: () -> Unit,
    onRetake: () -> Unit,
) {
    // TODO: Implement confirmation screen
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Capture confirmation coming in Step 3!")
    }
}
