package com.wangerekaharun.mugshot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wangerekaharun.mugshot.model.QualityGrade

@Composable
fun QualityBadge(
    grade: QualityGrade,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(grade.color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = grade.label,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )
    }
}
