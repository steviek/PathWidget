package com.sixbynine.transit.path.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.RemoteViews
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.R.color
import com.sixbynine.transit.path.application
import com.sixbynine.transit.path.context
import com.sixbynine.transit.path.ktx.drawableBackground
import com.sixbynine.transit.path.ktx.toAppWidgetId
import com.sixbynine.transit.path.serialization.JsonFormat
import com.sixbynine.transit.path.time.formatLocalTime
import com.sixbynine.transit.path.widget.configuration.DepartureBoardWidgetConfigurationActivity
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
      modifier = GlanceModifier.drawableBackground(R.drawable.widget_background).fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (widgetData?.loadedData == null || widgetData.lastRefresh == null) {
        LoadingView()
        return@Column
      }

      if (!widgetData.useClosestStation && widgetData.fixedStations.isNullOrEmpty()) {
        SetupView()
        return@Column
      }

      if (VERSION.SDK_INT < 31) {
        Spacer(modifier = GlanceModifier.height(16.dp))
      }

      DepartureList(widgetData, modifier = GlanceModifier.defaultWeight())
      UpdatedFooter(widgetData)
    }
  }

  @Composable
  fun SetupView() {
    Text(
      modifier = GlanceModifier
        .clickable(startConfigurationActivityAction(LocalGlanceId.current))
        .drawableBackground(R.drawable.ripple_rect)
        .padding(16.dp),
      text = getString(R.string.complete_widget_setup),
      style = TextStyle(
        color = ColorProvider(color.widget_accent_color),
        fontSize = 18.sp,
        textAlign = TextAlign.Center
      )

    )
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

fun startConfigurationActivityAction(glanceId: GlanceId): Action {
  val appWidgetId = glanceId.toAppWidgetId()
  val configurationIntent =
    Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
      .setClass(context, DepartureBoardWidgetConfigurationActivity::class.java)
      .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  return actionStartActivity(configurationIntent)
}

val SmallWidgetSize = DpSize(1.dp, 1.dp)
val MediumWidgetSize: DpSize
  get() {
    val widestUpdatedAtText =
      getString(R.string.updated_at_x, formatLocalTime(LocalTime.of(22, 20)))
    val updatedAtWidth = estimateTextWidth(widestUpdatedAtText, 12.sp)
    val requiredWidth = 16.dp * 2 + 8.dp + 32.dp + updatedAtWidth
    return DpSize(requiredWidth, 1.dp)
  }
