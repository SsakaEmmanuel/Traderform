package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArtisticColorScheme = lightColorScheme(
    primary = ArtisticPrimary,
    onPrimary = ArtisticOnPrimary,
    primaryContainer = ArtisticPrimaryContainer,
    onPrimaryContainer = ArtisticOnPrimaryContainer,
    secondary = ArtisticPillText,
    onSecondary = Color.White,
    tertiary = MA25Color,
    background = ArtisticBg,
    onBackground = ArtisticTextPrimary,
    surface = ArtisticSurface,
    onSurface = ArtisticTextPrimary,
    surfaceVariant = ArtisticSurfaceElevated,
    onSurfaceVariant = ArtisticTextSecondary,
    outline = ArtisticBorder,
    outlineVariant = ArtisticBorderSubtle,
    error = ArtisticRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ArtisticColorScheme,
        typography = Typography,
        content = content
    )
}


