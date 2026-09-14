package com.kangla.auto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Maroon,
    onPrimary = Color.White,
    primaryContainer = Amber,
    onPrimaryContainer = MaroonDark,
    secondary = Amber,
    onSecondary = MaroonDark,
    secondaryContainer = Cream,
    onSecondaryContainer = MaroonDark,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = CreamDark,
    onSurfaceVariant = Muted,
    error = Danger,
    onError = Color.White,
)

@Composable
fun KanglaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content,
    )
}