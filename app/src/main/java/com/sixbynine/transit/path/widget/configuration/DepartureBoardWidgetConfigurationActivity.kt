package com.sixbynine.transit.path.widget.configuration

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.location.hasLocationPermission
import com.sixbynine.transit.path.station.StationLister
import com.sixbynine.transit.path.ui.theme.PathTheme
import com.sixbynine.transit.path.widget.DepartureBoardWidgetData
import com.sixbynine.transit.path.widget.StationByDisplayNameComparator

class DepartureBoardWidgetConfigurationActivity : AppCompatActivity() {

  private var wasPermissionRequestRejected = false
  private var previousData: DepartureBoardWidgetData? = null
  private var loadedFromPreviousData = false
  private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

  private val appWidgetId: Int
    get() = intent?.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: INVALID_APPWIDGET_ID


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Callback for requesting location permission when the user chooses the closest station option.
    locationPermissionRequest =
      registerForActivityResult(RequestMultiplePermissions()) { permissions ->
        val wasPermissionGranted =
          permissions[ACCESS_FINE_LOCATION] == true || permissions[ACCESS_COARSE_LOCATION] == true
        if (!wasPermissionGranted) {
          wasPermissionRequestRejected = true
          composeContent()
        }
      }

    // Load the previously configured data for the widget to cover the reconfiguration case.
    val viewModel: DepartureBoardWidgetConfigurationViewModel by viewModels()
    viewModel.previousData.observe(this) {
      previousData = it
      composeContent()
    }
    viewModel.loadPreviousData(appWidgetId)

    composeContent()
  }

  private fun composeContent() = setContent {
    PathTheme {
      Surface(color = MaterialTheme.colors.background) {
        Column(
          modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
        ) {
          Text(text = stringResource(R.string.choose_stations_configuration))

          var useClosestStation by remember { mutableStateOf(false) }
          if (useClosestStation && wasPermissionRequestRejected) {
            // If they clicked "use closest station" and then denied a permission request, uncheck
            // the box...
            useClosestStation = false
            wasPermissionRequestRejected = false
          }

          CheckBoxRow(
            isChecked = useClosestStation,
            text = stringResource(R.string.closest_station),
            onCheckedChange = {
              useClosestStation = !useClosestStation
              if (useClosestStation) checkLocationPermission()
            }
          )

          Text(
            modifier = Modifier
              .padding(vertical = 8.dp)
              .padding(start = 48.dp),
            text = stringResource(R.string.and_or),
          )

          var selectedStations by remember { mutableStateOf(emptySet<String>()) }
          StationLister
            .getStations()
            .distinctBy { it.apiName }
            .sortedWith(StationByDisplayNameComparator)
            .forEach { station ->
              CheckBoxRow(
                isChecked = station.apiName in selectedStations,
                text = station.displayName,
                onCheckedChange = { isChecked ->
                  selectedStations = if (isChecked) {
                    selectedStations + station.apiName
                  } else {
                    selectedStations - station.apiName
                  }
                }
              )
            }

          if (previousData != null && !loadedFromPreviousData) {
            // Check the previously checked boxes when we're reconfiguring. This is async but loads
            // really quickly, so this should (hopefully) happen before the user can notice or start
            // clicking things.
            loadedFromPreviousData = true
            useClosestStation = previousData?.useClosestStation ?: false
            selectedStations = previousData?.fixedStations ?: emptySet()
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
              onClick = { confirmWidgetUpdate(selectedStations, useClosestStation) },
              enabled = selectedStations.isNotEmpty() || useClosestStation
            ) {
              Text(text = stringResource(R.string.confirm))
            }
          }

        }
      }
    }
  }

  @Composable
  private fun CheckBoxRow(isChecked: Boolean, text: String, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(checked = isChecked, onCheckedChange = { onCheckedChange(it) })
      Text(text = text, modifier = Modifier.clickable { onCheckedChange(!isChecked) })
    }
  }

  private fun confirmWidgetUpdate(stations: Set<String>, useClosestStation: Boolean) {
    val viewModel: DepartureBoardWidgetConfigurationViewModel by viewModels()
    viewModel.configurationComplete.observe(this) { isComplete ->
      if (!isComplete) return@observe
      // If the configuration completed, then we can confirm that the widget is set up.
      val resultValue = Intent().putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
      setResult(RESULT_OK, resultValue)
      finish()
    }

    viewModel.applyWidgetConfiguration(appWidgetId, stations, useClosestStation)
  }

  private fun checkLocationPermission() {
    if (VERSION.SDK_INT < 23) {
      return
    }
    if (hasLocationPermission()) return

    locationPermissionRequest.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
  }
}
