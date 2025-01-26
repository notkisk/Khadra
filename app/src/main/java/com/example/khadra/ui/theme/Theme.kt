package com.example.khadra.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

/*
 * Application theme configuration
 * Defines light and dark color schemes and handles dynamic theming
 */

// Light color scheme using custom colors
private val LightColorScheme = lightColorScheme(
    primary = MeadowGreen,
    secondary = SageGreen,
    tertiary = EarthBrown,
    background = WheatYellow,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = SoilGray,
    onSurface = SoilGray,
)

// Dark color scheme using custom colors
private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen,
    secondary = PineGreen,
    tertiary = OakBrown,
    background = Color.Black,
    surface = Color(0xFF1B1B1B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = WheatYellow,
    onSurface = WheatYellow,
)

/**
 * Application theme composable
 * @param darkTheme Boolean indicating if dark theme should be used
 * @param dynamicColor Boolean to enable dynamic theming on supported devices
 * @param content Composable content to apply the theme to
 */
@Composable
fun KhadraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Choose color scheme based on theme mode
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    // Apply Material Theme with custom configurations
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}