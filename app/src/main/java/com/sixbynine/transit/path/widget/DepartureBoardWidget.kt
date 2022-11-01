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
import com.sixbynine.transit.path.ktx.drawableBackground
import com.sixbynine.transit.path.ktx.toAppWidgetId
import com.sixbynine.transit.path.serialization.JsonFormat
import com.sixbynine.transit.path.time.DateTimeFormatter
import com.sixbynine.transit.path.widget.configuration.DepartureBoardWidgetConfigurationActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject

/** Our widget! */
class DepartureBoardWidget @Inject internal constructor(
  @ApplicationContext private val context: Context,
  private val dateTimeFormatter: DateTimeFormatter
) : GlanceAppWidget() {

  override val sizeMode = SizeMode.Responsive(setOf(SmallWidgetSize, getMediumWidgetSize()))

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
        .clickable(startConfigurationActivityAction())
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

  private fun getMediumWidgetSize(): DpSize {
    val widestUpdatedAtText =
      context.getString(
        R.string.updated_at_x,
        dateTimeFormatter.formatLocalTime(LocalTime.of(22, 20))
      )
    val updatedAtWidth = estimateTextWidth(context, widestUpdatedAtText, 12.sp)
    val requiredWidth = 16.dp * 2 + 8.dp + 32.dp + updatedAtWidth
    return DpSize(requiredWidth, 1.dp)
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

@AndroidEntryPoint
class DepartureBoardWidgetReceiver : GlanceAppWidgetReceiver() {
  @Inject
  lateinit var dataManager: WidgetUpdater

  @Inject
  lateinit var widget: DepartureBoardWidget

  @Inject
  lateinit var workerScheduler: WidgetRefreshWorkerScheduler

  override val glanceAppWidget get() = widget

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    runBlocking {
      dataManager.updateData()
    }
  }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    runBlocking {
      workerScheduler.schedulePeriodicRefresh()
    }
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    runBlocking {
      workerScheduler.cancelPeriodicRefresh()
    }
  }
}

val DEPARTURE_WIDGET_PREFS_KEY = stringPreferencesKey("departure_widget_data")

@Composable
fun startConfigurationActivityAction(): Action {
  val appWidgetId = LocalGlanceId.current.toAppWidgetId()
  val configurationIntent =
    Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
      .setClass(LocalContext.current, DepartureBoardWidgetConfigurationActivity::class.java)
      .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  return actionStartActivity(configurationIntent)
}

val SmallWidgetSize = DpSize(1.dp, 1.dp)

@Composable
fun getString(@StringRes resId: Int): String {
  return LocalContext.current.getString(resId)
}

@Composable
fun getString(@StringRes resId: Int, vararg args: Any): String {
  return LocalContext.current.getString(resId, *args)
}

@Composable
fun formatLocalTime(time: Instant): String {
  return DateTimeFormatter.from(LocalContext.current).formatLocalTime(time)
}
