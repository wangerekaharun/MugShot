package com.wangerekaharun.mugshot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wangerekaharun.mugshot.model.FaceQuality
import com.wangerekaharun.mugshot.model.RejectionMessage

@Composable
fun RejectionMessageDisplay(
    quality: FaceQuality?,
    modifier: Modifier = Modifier,
) {
    var displayedMessage by remember { mutableStateOf("") }
    var lastUpdateTime by remember { mutableLongStateOf(0L) }
    val debounceInterval = 1500L

    LaunchedEffect(quality) {
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime > debounceInterval) {
            val (_, message) = RejectionMessage.getActiveMessage(quality)
            displayedMessage = message
            lastUpdateTime = now
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = displayedMessage,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "rejection_message",
        ) { message ->
            Text(
                text = message,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}
