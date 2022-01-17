package com.sixbynine.transit.path.ui.theme

import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.ktx.toColor

data class AppColors(
  val PathBlue200: Color,
  val PathBlue500: Color,
  val PathBlue700: Color
)

@Composable
fun AppColors() = AppColors(
  PathBlue200 = getColor(R.color.path_blue_200),
  PathBlue500 = getColor(R.color.path_blue_500),
  PathBlue700 = getColor(R.color.path_blue_700)
)

@Composable
private fun getColor(@ColorRes resId: Int): Color {
  return ContextCompat.getColor(LocalContext.current, resId).toColor()
}

