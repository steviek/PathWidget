package com.sixbynine.transit.path.fake

import androidx.glance.GlanceId
import com.sixbynine.transit.path.widget.DepartureBoardWidgetData
import com.sixbynine.transit.path.widget.SavedWidgetDataManager
import com.sixbynine.transit.path.widget.WidgetRefresher
import com.sixbynine.transit.path.widget.WidgetRefresherModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestWidgetRefresher @Inject constructor(
  private val savedWidgetDataManager: SavedWidgetDataManager
): WidgetRefresher {

  private var idToLastRefreshData = mutableMapOf<TestGlanceId, DepartureBoardWidgetData?>()

  override suspend fun refreshWidget(id: GlanceId) {
    require(id is TestGlanceId)
    idToLastRefreshData[id] = savedWidgetDataManager.getWidgetData(id)
  }

  fun getLastRefreshData(id: TestGlanceId): DepartureBoardWidgetData? {
    return idToLastRefreshData[id]
  }
}

@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [WidgetRefresherModule::class]
)
@Module
interface TestWidgetRefresherModule {
  @Binds
  fun bindWidgetRefresher(refresher: TestWidgetRefresher): WidgetRefresher
}
