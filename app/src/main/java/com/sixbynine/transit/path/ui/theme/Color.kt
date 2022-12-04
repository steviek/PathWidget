package com.sixbynine.transit.path.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.sixbynine.transit.path.R

data class AppColors(
    val PathBlue200: Color,
    val PathBlue500: Color,
    val PathBlue700: Color
)

@Composable
fun AppColors() = AppColors(
    PathBlue200 = colorResource(R.color.path_blue_200),
    PathBlue500 = colorResource(R.color.path_blue_500),
    PathBlue700 = colorResource(R.color.path_blue_700)
)
