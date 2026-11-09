package com.worthour.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WorthourPrimary,
    onPrimary = WorthourOnPrimary,
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    onBackground = Color(0xFFF9FAFB),
    onSurface = Color(0xFFF9FAFB)
)

private val LightColorScheme = lightColorScheme(
    primary = WorthourPrimary,
    onPrimary = WorthourOnPrimary,
    primaryContainer = WorthourPrimaryContainer,
    onPrimaryContainer = WorthourOnPrimary,
    background = WorthourBackground,
    surface = WorthourSurface,
    onBackground = WorthourOnSurface,
    onSurface = WorthourOnSurface,
    onSurfaceVariant = WorthourOnSurfaceVariant
)

@Composable
fun WorthourTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}