package com.sixbynine.transit.path.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.sixbynine.transit.path.PathWidgetApplication
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.R.color
import com.sixbynine.transit.path.application
import com.sixbynine.transit.path.serialization.JsonFormat
import com.sixbynine.transit.path.time.formatLocalTime
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import java.time.LocalTime

/** Our widget! */
class DepartureBoardWidget : GlanceAppWidget() {

  override val sizeMode = SizeMode.Responsive(setOf(SmallWidgetSize, MediumWidgetSize))

  override val stateDefinition = PreferencesGlanceStateDefinition

  @Composable
  override fun Content() {
    val prefs = currentState<Preferences>()
    val widgetData: DepartureBoardWidgetData? =
      prefs[DEPARTURE_WIDGET_PREFS_KEY]?.let { JsonFormat.decodeFromString(it) }

    Column(
      modifier = GlanceModifier.background(color.widget_background).fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (widgetData?.loadedData == null || widgetData.lastRefresh == null) {
        LoadingView()
        return@Column
      }

      DepartureList(widgetData, modifier = GlanceModifier.defaultWeight())
      UpdatedFooter(widgetData)
    }
  }

  @Composable
  fun LoadingView() {
    ProgressBar(modifier = GlanceModifier.width(48.dp).height(48.dp))
    Text(text = getString(R.string.loading), modifier = GlanceModifier.padding(top = 8.dp))
  }
}

@Composable
fun ProgressBar(modifier: GlanceModifier) {
  val context = LocalContext.current
  val remoteViews = RemoteViews(context.packageName, R.layout.progress_bar)
  Box(modifier) {
    AndroidRemoteViews(remoteViews)
  }
}

class DepartureBoardWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget = DepartureBoardWidget()

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    runBlocking {
      DepartureBoardWidgetDataManager.updateData()
    }
  }
}

val DEPARTURE_WIDGET_PREFS_KEY = stringPreferencesKey("departure_widget_data")

fun getString(@StringRes resId: Int): String {
  return application.getString(resId)
}

fun getString(@StringRes resId: Int, vararg args: Any): String {
  return application.getString(resId, *args)
}

val SmallWidgetSize = DpSize(1.dp, 1.dp)
val MediumWidgetSize: DpSize
  get() {
    val widestUpdatedAtText = getString(R.string.updated_at_x, formatLocalTime(LocalTime.of(22, 20)))
    val updatedAtWidth = estimateTextWidth(widestUpdatedAtText, 12.sp)
    val requiredWidth = 8.dp * 3 + 32.dp + updatedAtWidth
    return DpSize(requiredWidth, 1.dp)
  }
