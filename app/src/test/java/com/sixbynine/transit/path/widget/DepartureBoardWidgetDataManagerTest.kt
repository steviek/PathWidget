package com.sixbynine.transit.path.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.View.FIND_VIEWS_WITH_TEXT
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.core.app.ApplicationProvider
import com.sixbynine.transit.path.api.DATE_TIME_FORMATTER
import com.sixbynine.transit.path.api.UpcomingTrain
import com.sixbynine.transit.path.fake.FakeTimeSource
import com.sixbynine.transit.path.fake.FakeTrainDataManager
import com.sixbynine.transit.path.ktx.getGlanceId
import com.sixbynine.transit.path.ktx.minutes
import com.sixbynine.transit.path.util.sendBroadcastBlocking
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowApplication
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.test.assertNotNull

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class DepartureBoardWidgetDataManagerTest {
  @get:Rule
  val hiltRule = HiltAndroidRule(this)

  @Inject
  lateinit var manager: DepartureBoardWidgetDataManager

  @Inject
  lateinit var appWidgetManager: AppWidgetManager

  @Inject
  lateinit var glanceAppWidgetManager: GlanceAppWidgetManager

  @Inject
  lateinit var trainDataManager: FakeTrainDataManager

  @Inject
  lateinit var timeSource: FakeTimeSource

  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val componentName = ComponentName(context, DepartureBoardWidgetReceiver::class.java)
  private val appWidgetId = 1

  @Before
  fun setUp() {
    hiltRule.inject()

    shadowOf(appWidgetManager).bindAppWidgetId(appWidgetId, componentName)
    sendBroadcastBlocking<DepartureBoardWidgetReceiver>(
      Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
    )
    /*
    runBlocking {
      glanceAppWidgetManager.getGlanceIds(DepartureBoardWidget::class.java)
    }*/
  }

  @Test
  fun foo() {
    /*timeSource.setNow(JAN_17_5_PM)
    configureWidget(EXCHANGE_PLACE)
    trainDataManager.setUpcomingTrains(
      EXCHANGE_PLACE,
      listOf(wtcBoundTrain(JAN_17_5_PM + 2.minutes))
    )

    manager.updateDataBlocking()

    val view = shadowOf(appWidgetManager).getViewFor(appWidgetId)
    assertNotNull(view.findViewWithText("Exchange Place"))
    assertNotNull(view.findViewWithText("World Trade Center"))
    assertNotNull(view.findViewWithText("5:02 PM"))*/
  }

  private fun configureWidget(vararg stations: String, useClosestStation: Boolean = false) {
    manager.setWidgetDataBlocking(
      DepartureBoardWidgetData(
        fixedStations = stations.toSet(),
        useClosestStation = useClosestStation
      )
    )
  }

  private fun wtcBoundTrain(arrivalTime: LocalDateTime): UpcomingTrain {
    return UpcomingTrain(
      lineName = "World Trade Center",
      headsign = "World Trade Center",
      route = "NWK_WTC",
      routeDisplayName = "Newark - World Trade Center",
      direction = "TO_NY",
      rawLineColors = listOf("#D93A30"),
      rawProjectedArrival = DATE_TIME_FORMATTER.format(
        arrivalTime.atZone(ZoneId.systemDefault()).toInstant().atZone(ZoneOffset.UTC)
      )
    )
  }

  private fun DepartureBoardWidgetDataManager.updateDataBlocking() = runBlocking { updateData() }

  private fun DepartureBoardWidgetDataManager.setWidgetDataBlocking(
    data: DepartureBoardWidgetData
  ) = runBlocking {
    val glanceId = glanceAppWidgetManager.getGlanceId<DepartureBoardWidget>(appWidgetId)
    assertNotNull(glanceId)
    updateWidget(glanceId) { data }
  }

  fun View.findViewWithText(text: String): View? {
    val outArrayList = arrayListOf<View?>(null)
    findViewsWithText(outArrayList, text, FIND_VIEWS_WITH_TEXT)
    return outArrayList.single()
  }

  companion object {
    const val EXCHANGE_PLACE = "EXCHANGE_PLACE"
    val JAN_17_5_PM = LocalDateTime.of(2022, 1, 17, 17, 0)!!
  }
}