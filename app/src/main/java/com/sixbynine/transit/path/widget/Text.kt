package com.sixbynine.transit.path.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.glance.GlanceModifier
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextAlign.Companion
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sixbynine.transit.path.R.color

@Composable
fun PrimaryText(
  text: String,
  fontSize: TextUnit,
  maxLines: Int = Int.MAX_VALUE,
  modifier: GlanceModifier = GlanceModifier,
  textAlign: TextAlign? = null,
) {
  Text(
    text = text,
    style = TextStyle(
      fontSize = fontSize,
      fontWeight = FontWeight.Bold,
      color = ColorProvider(color.widget_text_primary),
      textAlign = textAlign
    ),
    maxLines = maxLines,
    modifier = modifier,
  )
}

@Composable
fun SecondaryText(
  text: String,
  fontSize: TextUnit,
  modifier: GlanceModifier = GlanceModifier,
  textStyle: TextStyle = TextStyle()
) {
  Text(
    text = text,
    style = TextStyle(
      fontSize = fontSize,
      color = ColorProvider(color.widget_text_secondary),
      textAlign = textStyle.textAlign
    ),
    modifier = modifier
  )
}
