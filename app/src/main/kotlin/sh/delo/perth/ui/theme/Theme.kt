package sh.delo.perth.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PerthGreen,
    onPrimary = PerthSurface,
    primaryContainer = PerthGreenContainer,
    onPrimaryContainer = PerthGreen,
    secondary = PerthCyan,
    onSecondary = PerthSurface,
    tertiary = PerthAmber,
    onTertiary = PerthSurface,
    error = PerthRed,
    errorContainer = PerthRedContainer,
    onError = PerthSurface,
    background = PerthSurface,
    onBackground = PerthOnSurface,
    surface = PerthSurface,
    onSurface = PerthOnSurface,
    surfaceVariant = PerthSurfaceVariant,
    onSurfaceVariant = PerthOnSurfaceVariant,
    outline = PerthOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = PerthLightPrimary,
    onPrimary = PerthLightOnPrimary,
    primaryContainer = PerthGreenContainer,
    onPrimaryContainer = PerthLightPrimary,
    secondary = PerthCyanDim,
    onSecondary = PerthLightOnPrimary,
    tertiary = PerthAmber,
    onTertiary = PerthSurface,
    error = PerthRed,
    background = PerthLightBackground,
    onBackground = PerthSurface,
    surface = PerthLightSurface,
    onSurface = PerthSurface,
    surfaceVariant = PerthLightBackground,
    onSurfaceVariant = PerthOnSurfaceVariant,
    outline = PerthOutline,
)

/**
 * Perth Material 3 theme.
 *
 * Defaults to dark mode (terminal aesthetic). Dynamic colour is supported on Android 12+
 * but falls back to the hand-crafted terminal palette on older devices.
 *
 * @param darkTheme   Whether to use the dark palette. Defaults to system setting.
 * @param dynamicColor Whether to use dynamic (wallpaper-derived) colour on Android 12+.
 *                    Disabled by default to preserve the terminal aesthetic.
 */
@Composable
fun PerthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PerthTypography,
        content = content,
    )
}
