package com.sixbynine.transit.path.widget.configuration

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.util.Log
import androidx.glance.GlanceId
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sixbynine.transit.path.context
import com.sixbynine.transit.path.ktx.GlanceAppWidgetManager
import com.sixbynine.transit.path.ktx.toAppWidgetId
import com.sixbynine.transit.path.util.logWarning
import com.sixbynine.transit.path.widget.DepartureBoardWidget
import com.sixbynine.transit.path.widget.DepartureBoardWidgetData
import com.sixbynine.transit.path.widget.DepartureBoardWidgetDataManager
import com.sixbynine.transit.path.widget.DepartureBoardWidgetReceiver
import kotlinx.coroutines.launch

class DepartureBoardWidgetConfigurationViewModel : ViewModel() {

  private val _previousData = MutableLiveData<DepartureBoardWidgetData?>(null)
  private val _configurationComplete = MutableLiveData(false)

  val previousData: LiveData<DepartureBoardWidgetData?>
    get() = _previousData

  val configurationComplete: LiveData<Boolean>
    get() = _configurationComplete

  /**
   * Loads the previous widget data if there was any, and publishes to [previousData] if any data
   * was loaded.
   */
  fun loadPreviousData(appWidgetId: Int) = viewModelScope.launch {
    val glanceId = getGlanceId(appWidgetId) ?: return@launch
    val previousData = DepartureBoardWidgetDataManager.getWidgetData(glanceId) ?: return@launch
    _previousData.value = previousData
  }

  /**
   * Updates the widget stations and whether to use the closest station for [appWidgetId]. Publishes
   * to [configurationComplete] when the commit is complete.
   */
  fun applyWidgetConfiguration(
    appWidgetId: Int,
    stations: Set<String>,
    useClosestStation: Boolean
  ) = viewModelScope.launch {
    val glanceId = getGlanceId(appWidgetId)
    if (glanceId == null) {
      // This shouldn't really ever happen.
      logWarning("Couldn't get glance id for $appWidgetId")
      DepartureBoardWidgetDataManager.updateData()
      _configurationComplete.value = true
      return@launch
    }

    DepartureBoardWidgetDataManager.updateWidget(glanceId) { previousData ->
      when (previousData) {
        null -> DepartureBoardWidgetData(
          fixedStations = stations,
          useClosestStation = useClosestStation
        )
        else -> previousData.copy(fixedStations = stations, useClosestStation = useClosestStation)
      }
    }

    // Send a broadcast to update the widget to avoid blocking the configuration activity on the
    // update. This would be better done via WorkManager to avoid the broadcast queue.
    context.sendBroadcast(
      Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        .setClass(context, DepartureBoardWidgetReceiver::class.java)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
    )
    _configurationComplete.value = true
  }

  private suspend fun getGlanceId(appWidgetId: Int): GlanceId? {
    return GlanceAppWidgetManager()
      .getGlanceIds(DepartureBoardWidget::class.java)
      .firstOrNull { it.toAppWidgetId() == appWidgetId }
  }
}
