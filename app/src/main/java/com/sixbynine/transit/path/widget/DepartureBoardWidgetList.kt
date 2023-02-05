package com.sixbynine.transit.path.widget

import android.graphics.drawable.Icon
import android.os.Build.VERSION
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.Visibility.Gone
import androidx.glance.Visibility.Visible
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.background
import androidx.glance.layout.Alignment.Vertical
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.visibility
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.R.color
import com.sixbynine.transit.path.ktx.toColor
import com.sixbynine.transit.path.model.Station
import com.sixbynine.transit.path.time.DateTimeFormatter

@Composable
fun DepartureList(data: DepartureBoardWidgetData, modifier: GlanceModifier) {
    LazyColumn(modifier = modifier) {
        if (VERSION.SDK_INT >= 31) {
            item {
                Spacer(modifier = GlanceModifier.height(16.dp))
            }
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
                    first.mRazzaApiName -> -1
                    second.mRazzaApiName -> 1
                    else -> 0
                }
            }
            .distinctBy { it.mRazzaApiName }
            .filter { it.mRazzaApiName in stationsForWidget }
            .forEach { station ->
                item {
                    StationDepartures(data.loadedData, station)
                }
            }
    }
}

@Composable
fun StationDepartures(data: LoadedWidgetData, station: Station) {
    val context = LocalContext.current
    val allTrains =
        data.stationToTrains.filterKeys { it.mRazzaApiName == station.mRazzaApiName }
            .flatMap { it.value }
    Column(
        modifier = GlanceModifier
            .padding(horizontal = 16.dp)
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
        trainsByHeadsign.values.sortedBy {
            it.map { train -> train.projectedArrival }.minOrNull()!!
        }
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

                        Spacer(modifier = GlanceModifier.height(4.dp))

                        val times =
                            trains
                                .map { it.projectedArrival }
                                .sorted()
                                .joinToString(separator = ", ") {
                                    DateTimeFormatter.from(context).formatLocalTime(it)
                                }
                        SecondaryText(text = times, fontSize = 14.sp)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
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
            modifier = GlanceModifier.fillMaxSize()
                .visibility(if (useSingleColor) Gone else Visible),
            provider = topImageProvider,
            contentDescription = null,
        )
        Image(
            modifier = GlanceModifier.fillMaxSize()
                .visibility(if (useSingleColor) Gone else Visible),
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


