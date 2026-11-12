package com.marsholat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class ExtendedColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val dividerColor: Color,
    val cardBackground: Color,
    val cardBorder: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        dividerColor = LightDivider,
        cardBackground = LightSurface,
        cardBorder = LightDivider
    )
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider
)

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

@Composable
fun MarsholatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            dividerColor = DarkDivider,
            cardBackground = DarkSurface,
            cardBorder = DarkDivider
        )
    } else {
        ExtendedColors(
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            dividerColor = LightDivider,
            cardBackground = LightSurface,
            cardBorder = LightDivider
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
