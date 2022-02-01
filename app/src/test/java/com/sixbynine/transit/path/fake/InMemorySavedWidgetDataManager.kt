package com.sixbynine.transit.path.fake

import androidx.glance.GlanceId
import com.sixbynine.transit.path.widget.DepartureBoardWidgetData
import com.sixbynine.transit.path.widget.SavedWidgetDataManager
import com.sixbynine.transit.path.widget.SavedWidgetDataManagerModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemorySavedWidgetDataManager @Inject constructor() : SavedWidgetDataManager {

  private val idToData = mutableMapOf<TestGlanceId, DepartureBoardWidgetData>()

  override suspend fun getWidgetData(id: GlanceId): DepartureBoardWidgetData? {
    require(id is TestGlanceId)
    return idToData[id]
  }

  override suspend fun updateWidgetData(
    id: GlanceId,
    function: (DepartureBoardWidgetData?) -> DepartureBoardWidgetData
  ) {
    require(id is TestGlanceId)
    idToData[id] = function(idToData[id])
  }
}

@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [SavedWidgetDataManagerModule::class]
)
@Module
interface TestSavedWidgetManagerModule {
  @Binds
  fun bindSavedWidgetDataManager(manager: InMemorySavedWidgetDataManager): SavedWidgetDataManager
}
