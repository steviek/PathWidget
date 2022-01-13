package com.sixbynine.transit.path.ui.theme

import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.context
import com.sixbynine.transit.path.ktx.toColor

val PathBlue200 by lazy { getColor(R.color.path_blue_200) }
val PathBlue500 by lazy { getColor(R.color.path_blue_500) }
val PathBlue700 by lazy { getColor(R.color.path_blue_700) }

private fun getColor(@ColorRes resId: Int) = ContextCompat.getColor(context, resId).toColor()
