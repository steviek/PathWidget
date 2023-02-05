package com.sixbynine.transit.path.widget

import android.appwidget.AppWidgetManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.view.View
import android.view.View.FIND_VIEWS_WITH_TEXT
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.google.common.truth.Truth.assertThat
import com.sixbynine.transit.path.fake.FakeGlanceIdProvider
import com.sixbynine.transit.path.fake.FakeTimeSource
import com.sixbynine.transit.path.fake.FakeTrainDataManager
import com.sixbynine.transit.path.fake.TestGlanceId
import com.sixbynine.transit.path.fake.TestWidgetRefresher
import com.sixbynine.transit.path.ktx.minutes
import com.sixbynine.transit.path.model.DepartureBoardTrain
import com.sixbynine.transit.path.model.Station
import com.sixbynine.transit.path.station.StationLister
import com.sixbynine.transit.path.util.runBlockingTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.test.assertNotNull

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class WidgetUpdaterTest {
  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var updater: WidgetUpdater

  @Inject
  lateinit var appWidgetManager: AppWidgetManager

  @Inject
  lateinit var glanceAppWidgetManager: GlanceAppWidgetManager

  @Inject
  lateinit var trainDataManager: FakeTrainDataManager

  @Inject
  lateinit var savedWidgetDataManager: SavedWidgetDataManager

  @Inject
  lateinit var widgetRefresher: TestWidgetRefresher

  @Inject
  lateinit var glanceIdProvider: FakeGlanceIdProvider

  @Inject
  lateinit var timeSource: FakeTimeSource

  @Inject
  lateinit var connectivityManager: ConnectivityManager

  private val glanceId = TestGlanceId()

  @Before
  fun setUp() = runBlockingTest {
    hiltRule.inject()
    glanceIdProvider.addGlanceId(DepartureBoardWidget::class.java, glanceId)
  }

  @Test
  fun updateData_callSucceeds_shouldRefreshWidgetData() = runBlockingTest {
    timeSource.setNow(JAN_17_5_PM)
    configureWidget(EXCHANGE_PLACE)
    trainDataManager.setUpcomingTrains(
      EXCHANGE_PLACE,
      listOf(wtcBoundTrain(JAN_17_5_PM + 2.minutes))
    )

    updater.updateData()

    val lastRefreshData = widgetRefresher.getLastRefreshData(glanceId)
    assertNotNull(lastRefreshData)
    assertThat(lastRefreshData.lastRefresh?.wasSuccess).isTrue()
    assertThat(lastRefreshData.lastRefresh?.hadInternet).isTrue()
    assertThat(lastRefreshData.loadedData?.updateTime).isEqualTo(JAN_17_5_PM.toInstant())
    assertThat(lastRefreshData.isLoading).isFalse()
    assertThat(lastRefreshData.loadedData?.stationToTrains)
      .containsEntry(
        getStation(EXCHANGE_PLACE),
        listOf(wtcBoundTrain(JAN_17_5_PM + 2.minutes))
      )
  }

  @Test
  fun updateData_callFails_shouldRefreshWidgetData() = runBlockingTest {
    timeSource.setNow(JAN_17_5_PM)
    configureWidget(EXCHANGE_PLACE)
    trainDataManager.setUpcomingTrainsFailed()

    updater.updateData()

    val lastRefreshData = widgetRefresher.getLastRefreshData(glanceId)
    assertNotNull(lastRefreshData)
    assertThat(lastRefreshData.lastRefresh?.wasSuccess).isFalse()
    assertThat(lastRefreshData.lastRefresh?.hadInternet).isTrue()
    assertThat(lastRefreshData.isLoading).isFalse()
    assertThat(lastRefreshData.loadedData).isNull()
  }

  @Test
  fun updateData_noInternet_shouldRefreshWidgetData() = runBlockingTest {
    timeSource.setNow(JAN_17_5_PM)
    configureWidget(EXCHANGE_PLACE)
    shadowOf(connectivityManager).setActiveNetworkInfo(null)
    trainDataManager.setUpcomingTrainsFailed()

    updater.updateData()

    val lastRefreshData = widgetRefresher.getLastRefreshData(glanceId)
    assertNotNull(lastRefreshData)
    assertThat(lastRefreshData.lastRefresh?.wasSuccess).isFalse()
    assertThat(lastRefreshData.lastRefresh?.hadInternet).isFalse()
    assertThat(lastRefreshData.isLoading).isFalse()
    assertThat(lastRefreshData.loadedData).isNull()
  }

  private suspend fun configureWidget(vararg stations: String, useClosestStation: Boolean = false) {
    savedWidgetDataManager.updateWidgetData(glanceId) {
      DepartureBoardWidgetData(
        fixedStations = stations.toSet(),
        useClosestStation = useClosestStation
      )
    }
  }

  private fun wtcBoundTrain(arrivalTime: LocalDateTime): DepartureBoardTrain {
    return DepartureBoardTrain(
      headsign = "World Trade Center",
      projectedArrival = arrivalTime.atZone(ZoneId.systemDefault()).toInstant(),
      lineColors = listOf(Color.parseColor("#D93A30"))
    )
  }

  fun View.findViewWithText(text: String): View? {
    val outArrayList = arrayListOf<View?>(null)
    findViewsWithText(outArrayList, text, FIND_VIEWS_WITH_TEXT)
    return outArrayList.single()
  }

  private fun getStation(apiName: String): Station {
    return StationLister.getStations().first { it.mRazzaApiName == apiName }
  }

  private fun LocalDateTime.toInstant() = atZone(ZoneId.systemDefault()).toInstant()

  companion object {
    const val EXCHANGE_PLACE = "EXCHANGE_PLACE"
    val JAN_17_5_PM = LocalDateTime.of(2022, 1, 17, 17, 0)!!
  }
}
