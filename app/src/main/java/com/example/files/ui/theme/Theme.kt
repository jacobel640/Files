package com.example.files.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

tailrec fun android.content.Context.findActivity(): android.app.Activity? {
    if (this is android.app.Activity) return this
    if (this is android.content.ContextWrapper) return baseContext.findActivity()
    return null
}

@Composable
fun FilesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to true if the user is using the default app color
    dynamicColor: Boolean = com.example.files.utils.ThemeManager.isUsingDefaultThemeColor(androidx.compose.ui.platform.LocalContext.current),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeColorInt = com.example.files.utils.ThemeManager.getThemeColor(context)
    val customPrimary = androidx.compose.ui.graphics.Color(themeColorInt)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme.copy(
            primary = customPrimary,
            primaryContainer = customPrimary.copy(alpha = 0.3f)
        )
        else -> LightColorScheme.copy(
            primary = customPrimary,
            primaryContainer = customPrimary.copy(alpha = 0.3f)
        )
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val activity = context.findActivity()
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = colorScheme.background.toArgb()
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}