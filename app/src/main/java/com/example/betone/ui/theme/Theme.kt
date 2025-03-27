package com.example.betone.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF388E3C), // Матовый изумрудный для кнопок
    secondary = Color(0xFFFFB300),
    tertiary = Color(0xFFB0BEC5),
    background = Color(0xFF1B5E20),
    surface = Color(0xFF1B5E20),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE8F5E9),
    onSurface = Color(0xFFE8F5E9)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF388E3C), // Матовый изумрудный для кнопок
    secondary = Color(0xFFFBC02D),
    tertiary = Color(0xFFF5F5F5),
    background = Color(0xFFE8F5E9), // Светло-зелёный фон
    surface = Color(0xFFE8F5E9),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20)
)

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun BetOneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Динамические цвета отключены
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Убедись, что Typography определён
        shapes = Shapes,
        content = content
    )
}