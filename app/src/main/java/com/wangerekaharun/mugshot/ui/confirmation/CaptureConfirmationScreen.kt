package com.wangerekaharun.mugshot.ui.confirmation

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.wangerekaharun.mugshot.model.FaceQuality
import com.wangerekaharun.mugshot.model.QualityGrade
import com.wangerekaharun.mugshot.ui.components.QualityBadge
import com.wangerekaharun.mugshot.ui.theme.MugShotDark

@Composable
fun CaptureConfirmationScreen(
    imageUri: Uri,
    quality: FaceQuality,
    onAccept: () -> Unit,
    onRetake: () -> Unit,
) {
    val grade = QualityGrade.fromQuality(quality)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MugShotDark)
    ) {
        // Captured image
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Captured selfie",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        )

        // Quality badge
        QualityBadge(
            grade = grade,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .statusBarsPadding(),
        )

        // Action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retake")
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Looks Good!")
            }
        }
    }
}
