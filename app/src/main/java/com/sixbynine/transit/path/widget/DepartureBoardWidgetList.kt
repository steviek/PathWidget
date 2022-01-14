package com.sixbynine.transit.path.widget

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build.VERSION
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.Visibility.Gone
import androidx.glance.Visibility.Visible
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.*
import androidx.glance.layout.Alignment.Vertical
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.R.color
import com.sixbynine.transit.path.api.Station
import com.sixbynine.transit.path.ktx.toColor
import com.sixbynine.transit.path.time.formatLocalTime
import com.sixbynine.transit.path.util.logDebug

@Composable
fun DepartureList(data: DepartureBoardWidgetData, modifier: GlanceModifier) {
  LazyColumn(modifier = modifier) {
    item {
      Spacer(modifier = GlanceModifier.height(8.dp))
    }

    checkNotNull(data.loadedData)
    val stationsForWidget =
      (data.fixedStations ?: emptySet()) + setOfNotNull(data.loadedData.closestStation)
    data
      .loadedData
      .stationToTrains
      .keys
      .sortedWith(StationByDisplayNameComparator)
      .sortedWith { first, second ->
        // Make sure the closest station is always the first to appear in the list.
        when (data.loadedData.closestStation) {
          first.apiName -> -1
          second.apiName -> 1
          else -> 0
        }
      }
      .distinctBy { it.apiName }
      .filter { it.apiName in stationsForWidget }
      .also { logDebug("Display stations: ${it.map { it.displayName }}") }
      .forEach { station ->
        item {
          StationDepartures(data.loadedData, station)
        }
      }
  }
}

@Composable
fun StationDepartures(data: LoadedWidgetData, station: Station) {
  val allTrains =
    data.stationToTrains.filterKeys { it.apiName == station.apiName }.flatMap { it.value }
  Column(
    modifier = GlanceModifier
      .padding(horizontal = 8.dp)
      .background(color.widget_background)
      .fillMaxWidth()
  ) {
    PrimaryText(
      text = station.displayName,
      fontSize = 18.sp,
      modifier = GlanceModifier.padding(bottom = 8.dp),
      maxLines = 1
    )
    val trainsByHeadsign = allTrains.groupBy { it.headsign }
    trainsByHeadsign.values.sortedBy { it.map { train -> train.projectedArrival }.minOrNull()!! }
      .forEach { trains ->
        Row {
          val lineColors = trains.flatMap { it.lineColors }.distinct()
          ColorBox(
            lineColors.firstOrNull(),
            lineColors.takeIf { it.size >= 2 }?.get(1)
          )

          Spacer(modifier = GlanceModifier.width(8.dp))
          Column {
            Row(verticalAlignment = Vertical.CenterVertically) {
              PrimaryText(text = trains.first().headsign, fontSize = 14.sp)
              Spacer(modifier = GlanceModifier.height(18.dp).width(1.dp))
            }

            val times =
              trains
                .map { it.projectedArrival }
                .sorted()
                .joinToString(separator = ", ") { formatLocalTime(it) }
            SecondaryText(text = times, fontSize = 14.sp)
          }
        }
      }
    Spacer(modifier = GlanceModifier.height(8.dp))
  }
}

@Composable
fun ColorBox(@ColorInt firstColor: Int?, @ColorInt secondColor: Int? = null) {
  // We display a single color using a spacer background or two colors tinting two triangles if
  // possible. We use visibility to choose views rather than `if` because this allows Glance to
  // recycle views in the generated RemoteViews.
  Box(modifier = GlanceModifier.size(18.dp)) {
    val useSingleColor: Boolean
    val context = LocalContext.current
    val topImageProvider: ImageProvider
    val bottomImageProvider: ImageProvider
    if (VERSION.SDK_INT >= 23 && firstColor != null && secondColor != null) {
      useSingleColor = false
      topImageProvider =
        ImageProvider(
          Icon.createWithResource(context, R.drawable.half_triangle_top)
            .setTint(firstColor)
        )
      bottomImageProvider =
        ImageProvider(
          Icon.createWithResource(context, R.drawable.half_triangle_bottom)
            .setTint(secondColor)
        )
    } else {
      useSingleColor = true
      // These will never be seen, just choose a random drawable to satisfy the API.
      topImageProvider = ImageProvider(R.drawable.half_triangle_top)
      bottomImageProvider = ImageProvider(R.drawable.half_triangle_top)
    }
    Image(
      modifier = GlanceModifier.fillMaxSize().visibility(if (useSingleColor) Gone else Visible),
      provider = topImageProvider,
      contentDescription = null,
    )
    Image(
      modifier = GlanceModifier.fillMaxSize().visibility(if (useSingleColor) Gone else Visible),
      provider = bottomImageProvider,
      contentDescription = null,
    )
    Spacer(
      modifier = GlanceModifier
        .background(firstColor?.toColor() ?: Color.Transparent)
        .fillMaxSize()
        .visibility(if (useSingleColor) Visible else Gone)
    )
  }
}

@Composable
fun UpdatedFooter(data: DepartureBoardWidgetData) {
  require(data.loadedData != null)
  require(data.lastRefresh != null)

  Row(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
    modifier = GlanceModifier.fillMaxWidth().height(48.dp)
  ) {
    Spacer(modifier = GlanceModifier.height(48.dp).width(32.dp))
    Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))
    val updatedAtText = when {
      !data.lastRefresh.wasSuccess && !data.lastRefresh.hadInternet -> {
        getString(R.string.no_internet)
      }
      !data.lastRefresh.wasSuccess -> getString(R.string.failed_to_update)
      LocalSize.current == SmallWidgetSize -> formatLocalTime(data.loadedData.updateTime)
      else -> getString(R.string.updated_at_x, formatLocalTime(data.loadedData.updateTime))
    }
    SecondaryText(
      text = updatedAtText,
      fontSize = 12.sp,
      textStyle = TextStyle(textAlign = TextAlign.Center)
    )
    Spacer(modifier = GlanceModifier.defaultWeight().height(1.dp))
    Box(
      modifier = GlanceModifier.height(48.dp).width(32.dp),
      contentAlignment = Alignment.Center
    ) {
      val isLoading = data.isLoading
      Image(
        modifier = GlanceModifier
          .size(48.dp)
          .clickable(actionRunCallback<ClickFooterAction>())
          .visibility(if (isLoading) Gone else Visible),
        provider = ImageProvider(R.drawable.selectable_item_background_borderless),
        contentDescription = getString(R.string.refresh)
      )
      Image(
        modifier = GlanceModifier.size(24.dp).visibility(if (isLoading) Gone else Visible),
        provider = ImageProvider(R.drawable.ic_refresh),
        contentDescription = null
      )
      // The arrow makes the refresh circle relatively smaller than the progress bar, but it looks
      // really good if I can get them to align.
      ProgressBar(
        modifier = GlanceModifier.size(20.dp).visibility(if (isLoading) Visible else Gone)
      )
    }

    Spacer(modifier = GlanceModifier.width(8.dp).height(48.dp))
  }
}

class ClickFooterAction : ActionCallback {
  override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    DepartureBoardWidgetDataManager.updateData()
  }
}
