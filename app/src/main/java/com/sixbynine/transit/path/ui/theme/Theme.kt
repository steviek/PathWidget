package com.sixbynine.transit.path.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import javax.inject.Inject

@Composable
private fun DarkColorPalette() = with(AppColors()) {
    darkColors(
        primary = PathBlue200,
        primaryVariant = PathBlue700,
        secondary = PathBlue200
    )
}

@Composable
private fun LightColorPalette() = with(AppColors()) {
    lightColors(
        primary = PathBlue500,
        primaryVariant = PathBlue700,
        secondary = PathBlue200

        /* Other default colors to override
          background = Color.White,
          surface = Color.White,
          onPrimary = Color.White,
          onSecondary = Color.Black,
          onBackground = Color.Black,
          onSurface = Color.Black,
          */
    )
}

@Composable
fun PathTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette()
    } else {
        LightColorPalette()
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
