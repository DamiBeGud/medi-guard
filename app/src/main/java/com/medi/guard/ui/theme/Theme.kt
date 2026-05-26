package com.medi.guard.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = ClinicalPrimary,
    onPrimary = ClinicalSurface,
    primaryContainer = ClinicalPrimaryContainer,
    onPrimaryContainer = ColorTokens.OnPrimaryContainer,
    secondary = ClinicalSuccess,
    onSecondary = ClinicalSurface,
    secondaryContainer = ClinicalSuccessContainer,
    onSecondaryContainer = ClinicalSuccess,
    tertiary = ClinicalWarning,
    onTertiary = ClinicalSurface,
    tertiaryContainer = ClinicalWarningContainer,
    onTertiaryContainer = ClinicalWarning,
    error = ClinicalError,
    onError = ClinicalSurface,
    errorContainer = ClinicalErrorContainer,
    onErrorContainer = ColorTokens.OnErrorContainer,
    background = ClinicalBackground,
    onBackground = ClinicalText,
    surface = ClinicalBackground,
    onSurface = ClinicalText,
    surfaceVariant = ClinicalSurfaceContainer,
    onSurfaceVariant = ClinicalTextVariant,
    outline = ClinicalOutline,
    outlineVariant = ClinicalOutlineVariant
)

@Composable
fun MediGuardTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ClinicalBackground.toArgb()
            window.navigationBarColor = ClinicalSurface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = LightColors,
        typography = MediGuardTypography,
        content = content
    )
}

private object ColorTokens {
    val OnPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFF3F7FF)
    val OnErrorContainer = androidx.compose.ui.graphics.Color(0xFF93000A)
}
