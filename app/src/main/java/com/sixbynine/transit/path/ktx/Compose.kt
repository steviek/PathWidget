package com.sixbynine.transit.path.ktx

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color

fun @receiver:ColorInt Int.toColor(): Color {
    return Color(
        red = android.graphics.Color.red(this),
        green = android.graphics.Color.green(this),
        blue = android.graphics.Color.blue(this),
        alpha = android.graphics.Color.alpha(this)
    )
}
