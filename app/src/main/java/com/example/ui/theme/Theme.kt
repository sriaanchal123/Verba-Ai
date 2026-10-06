package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DeepIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E3A8A),
    
    secondary = SlateNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    
    tertiary = SageGreen,
    onTertiary = Color.White,
    tertiaryContainer = SageGreenBg,
    onTertiaryContainer = Color(0xFF064E3B),
    
    background = PaperBg,
    onBackground = TextPrimary,
    
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = Slate100,
    onSurfaceVariant = TextSecondary,
    
    outline = CardBorder,
    outlineVariant = Slate300,
    
    error = RoseRed,
    onError = Color.White,
    errorContainer = RoseRedBg,
    onErrorContainer = Color(0xFF881337)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
