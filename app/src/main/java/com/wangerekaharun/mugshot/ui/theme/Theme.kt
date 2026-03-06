package com.wangerekaharun.mugshot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MugShotBlue,
    secondary = MugShotAccent,
    background = MugShotDark,
    surface = MugShotSurface,
    onPrimary = MugShotWhite,
    onSecondary = MugShotDark,
    onBackground = MugShotWhite,
    onSurface = MugShotWhite,
    error = MugShotRed,
)

@Composable
fun MugShotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
