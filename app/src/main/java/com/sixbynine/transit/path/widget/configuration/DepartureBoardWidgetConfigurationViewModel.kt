package com.sixbynine.transit.path.widget.configuration

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sixbynine.transit.path.ktx.toAppWidgetId
import com.sixbynine.transit.path.util.logWarning
import com.sixbynine.transit.path.widget.DepartureBoardWidget
import com.sixbynine.transit.path.widget.DepartureBoardWidgetData
import com.sixbynine.transit.path.widget.DepartureBoardWidgetDataManager
import com.sixbynine.transit.path.widget.DepartureBoardWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepartureBoardWidgetConfigurationViewModel @Inject internal constructor(
  // Pretty sure this doesn't leak a context...
  @field:SuppressLint("StaticFieldLeak") @ApplicationContext private val context: Context,
  private val dataManager: DepartureBoardWidgetDataManager,
  private val glanceAppWidgetManager: GlanceAppWidgetManager
): ViewModel() {

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
    val previousData = dataManager.getWidgetData(glanceId) ?: return@launch
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
      dataManager.updateData()
      _configurationComplete.value = true
      return@launch
    }

    dataManager.updateWidget(glanceId, false) { previousData ->
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
    return glanceAppWidgetManager
      .getGlanceIds(DepartureBoardWidget::class.java)
      .firstOrNull { it.toAppWidgetId() == appWidgetId }
  }
}
