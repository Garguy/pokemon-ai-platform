package com.pokemonai.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Color
import com.composables.ui.theme.ComposablesTheme

/** Runtime-switchable color tokens (light/dark) for the app chrome. */
data class AppColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val accent: Color,
    val secondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val muted: Color,
    val border: Color,
    val onPrimary: Color,
)

val LightColors = AppColors(
    background = Color(0xFFF4F6F8),
    surface = Color(0xFFFFFFFF),
    primary = Color(0xFF2D6A4F),
    accent = Color(0xFFF9C74F),
    secondary = Color(0xFF4D96FF),
    onBackground = Color(0xFF1E2723),
    onSurface = Color(0xFF1E2723),
    muted = Color(0xFF6B7B73),
    border = Color(0xFFE2E8E5),
    onPrimary = Color(0xFFFFFFFF),
)

val DarkColors = AppColors(
    background = Color(0xFF121714),
    surface = Color(0xFF1E2723),
    primary = Color(0xFF74C69D),
    accent = Color(0xFFFFD166),
    secondary = Color(0xFF8ECAE6),
    onBackground = Color(0xFFECF3EE),
    onSurface = Color(0xFFECF3EE),
    muted = Color(0xFF9AA8A0),
    border = Color(0xFF2C3631),
    onPrimary = Color(0xFF0E1311),
)

val LocalAppColors = staticCompositionLocalOf { LightColors }
val LocalDarkTheme = staticCompositionLocalOf { false }
val LocalToggleTheme = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun AppTheme(dark: Boolean, toggleTheme: () -> Unit, content: @Composable () -> Unit) {
    val colors = if (dark) DarkColors else LightColors
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalDarkTheme provides dark,
        LocalToggleTheme provides toggleTheme,
    ) {
        ComposablesTheme {
            CompositionLocalProvider(LocalContentColor provides colors.onBackground, content = content)
        }
    }
}
