package com.shopirend.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF315C49),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E8DB),
    onPrimaryContainer = Color(0xFF143B2C),
    secondary = Color(0xFFE36C3A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCC),
    background = Color(0xFFF7F6F2),
    surface = Color(0xFFFFFEFA),
    surfaceVariant = Color(0xFFE9ECE7),
    outline = Color(0xFF7A817C),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9D0B9),
    onPrimary = Color(0xFF143B2C),
    primaryContainer = Color(0xFF294F3D),
    secondary = Color(0xFFFFB59A),
    background = Color(0xFF121411),
    surface = Color(0xFF1B1D1A),
    surfaceVariant = Color(0xFF3F4943),
)

@Composable
fun ShopirendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
        ),
        content = content,
    )
}

