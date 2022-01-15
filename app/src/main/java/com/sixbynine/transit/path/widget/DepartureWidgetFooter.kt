package com.sixbynine.transit.path.widget

import android.content.Context
import android.os.Build.VERSION
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.Visibility.Gone
import androidx.glance.Visibility.Invisible
import androidx.glance.Visibility.Visible
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.visibility
import com.sixbynine.transit.path.R.drawable
import com.sixbynine.transit.path.R.string
import com.sixbynine.transit.path.ktx.drawableBackground
import com.sixbynine.transit.path.time.formatLocalTime

@Composable
fun UpdatedFooter(data: DepartureBoardWidgetData) {
  require(data.loadedData != null)
  require(data.lastRefresh != null)

  Row(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
    modifier = GlanceModifier.fillMaxWidth().height(48.dp)
  ) {
    ImageButton(
      modifier =
      GlanceModifier
        .visibility(if (VERSION.SDK_INT >= 31) Invisible else Visible)
        .clickable(startConfigurationActivityAction(LocalGlanceId.current)),
      srcResId = drawable.ic_edit_inset,
      contentDescResId = string.edit
    )
    Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))
    val updatedAtText = when {
      !data.lastRefresh.wasSuccess && !data.lastRefresh.hadInternet -> {
        getString(string.no_internet)
      }
      !data.lastRefresh.wasSuccess -> getString(string.failed_to_update)
      LocalSize.current == SmallWidgetSize -> formatLocalTime(data.loadedData.updateTime)
      else -> getString(string.updated_at_x, formatLocalTime(data.loadedData.updateTime))
    }
    SecondaryText(
      text = updatedAtText,
      fontSize = 12.sp,
      textStyle = TextStyle(textAlign = TextAlign.Center)
    )
    Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))
    Box(modifier = GlanceModifier.size(48.dp), contentAlignment = Alignment.Center) {
      val isLoading = data.isLoading
      ImageButton(
        modifier = GlanceModifier
          .clickable(actionRunCallback<ClickFooterAction>())
          .visibility(if (isLoading) Invisible else Visible),
        srcResId = drawable.ic_refresh_inset,
        contentDescResId = string.refresh
      )
      // The arrow makes the refresh circle relatively smaller than the progress bar, but it looks
      // really good if I can get them to align.
      ProgressBar(
        modifier = GlanceModifier.size(20.dp).visibility(if (isLoading) Visible else Gone)
      )
    }

    Spacer(modifier = GlanceModifier.width(8.dp).height(48.dp))
  }
}

@Composable
fun ImageButton(
  modifier: GlanceModifier = GlanceModifier,
  @DrawableRes srcResId: Int,
  @StringRes contentDescResId: Int
) {
  Image(
    modifier = modifier.size(48.dp).drawableBackground(drawable.ripple_circle),
    provider = ImageProvider(srcResId),
    contentDescription = getString(contentDescResId),
  )
}

class ClickFooterAction : ActionCallback {
  override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    DepartureBoardWidgetDataManager.updateData()
  }
}