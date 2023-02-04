package com.sixbynine.transit.path.ktx

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.LocalContext

fun @receiver:ColorInt Int.toColor(): Color {
    return Color(
        red = android.graphics.Color.red(this),
        green = android.graphics.Color.green(this),
        blue = android.graphics.Color.blue(this),
        alpha = android.graphics.Color.alpha(this)
    )
}

@Composable
fun stringResource(@StringRes id: Int) : String {
    return LocalContext.current.getString(id)
}

@Composable
fun stringResource(@StringRes id: Int, vararg args: Any) : String {
    return LocalContext.current.getString(id, *args)
}
