package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.sixbynine.transit.path.logging.Logging
import com.sixbynine.transit.path.serialization.JsonFormat
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import javax.inject.Inject

interface SavedWidgetDataManager {
  suspend fun getWidgetData(id: GlanceId): DepartureBoardWidgetData?

  /** Update the stored data for the widget with [id], and update its UI. */
  suspend fun updateWidgetData(
    id: GlanceId,
    function: (DepartureBoardWidgetData?) -> DepartureBoardWidgetData
  )
}

class GlanceAppWidgetStateSavedWidgetDataManager @Inject constructor(
  @ApplicationContext private val context: Context,
  private val logging: Logging
) : SavedWidgetDataManager {
  override suspend fun getWidgetData(id: GlanceId): DepartureBoardWidgetData? {
    val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
    return state[DEPARTURE_WIDGET_PREFS_KEY]?.let {
      JsonFormat.decodeFromString<DepartureBoardWidgetData>(it)
    }
  }

  override suspend fun updateWidgetData(
    id: GlanceId,
    function: (DepartureBoardWidgetData?) -> DepartureBoardWidgetData
  ) {
    logging.debug("update widget: $id")
    val previousData = getWidgetData(id)
    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { newState ->
      newState.toMutablePreferences()
        .apply {
          this[DEPARTURE_WIDGET_PREFS_KEY] = JsonFormat.encodeToString(function(previousData))
        }
    }
  }
}

@InstallIn(SingletonComponent::class)
@Module
interface SavedWidgetDataManagerModule {
  @Binds
  fun bindSavedWidgetDataManager(
    manager: GlanceAppWidgetStateSavedWidgetDataManager
  ): SavedWidgetDataManager
}
