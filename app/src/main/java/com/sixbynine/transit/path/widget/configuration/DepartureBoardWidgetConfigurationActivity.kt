package com.sixbynine.transit.path.widget.configuration

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.sixbynine.transit.path.logging.Logging
import com.sixbynine.transit.path.permission.PermissionHelper
import com.sixbynine.transit.path.station.StationLister
import com.sixbynine.transit.path.ui.theme.PathTheme
import com.sixbynine.transit.path.widget.DepartureBoardWidgetData
import com.sixbynine.transit.path.widget.StationByDisplayNameComparator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DepartureBoardWidgetConfigurationActivity : AppCompatActivity() {

  @Inject
  lateinit var stationLister: StationLister

  @Inject
  lateinit var permissionHelper: PermissionHelper

  @Inject
  lateinit var logging: Logging

  private val viewModel: DepartureBoardWidgetConfigurationViewModel by viewModels()

  private var wasPermissionRequestRejected = false
  private var previousData: DepartureBoardWidgetData? = null
  private var loadedFromPreviousData = false
  private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
  private lateinit var backgroundLocationPermissionRequest: ActivityResultLauncher<Array<String>>

  private val appWidgetId: Int
    get() = intent?.extras?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: INVALID_APPWIDGET_ID

  private val prefs: SharedPreferences
    get() = getSharedPreferences("configuration_settings", Context.MODE_PRIVATE)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Callback for requesting location permission when the user chooses the closest station option.
    locationPermissionRequest =
      registerForActivityResult(RequestMultiplePermissions()) { permissions ->
        val wasPermissionGranted =
          permissions[ACCESS_FINE_LOCATION] == true || permissions[ACCESS_COARSE_LOCATION] == true
        if (wasPermissionGranted) {
          // The user can use the widget without background location access, but they should be
          // notified about it.
          checkBackgroundLocationPermission()
        } else {
          wasPermissionRequestRejected = true
          composeContent()
        }
      }

    backgroundLocationPermissionRequest =
      registerForActivityResult(RequestMultiplePermissions()) { permissions ->
        val wasPermissionGranted = permissions[ACCESS_BACKGROUND_LOCATION] == true
        logging.debug("Background location permission granted: $wasPermissionGranted")
      }

    loadDataAndComposeContent()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    loadDataAndComposeContent()
  }

  private fun loadDataAndComposeContent() {
    // Load the previously configured data for the widget to cover the reconfiguration case.
    viewModel.previousData.removeObservers(this)
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
            .verticalScroll(rememberScrollState())
        ) {
          Spacer(modifier = Modifier.height(16.dp))

          Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.choose_stations_configuration)
          )

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
              if (useClosestStation) {
                val dialogShown = checkLocationPermission()
                if (!dialogShown) checkBackgroundLocationPermission()
              }
            }
          )

          Text(
            modifier = Modifier
              .padding(vertical = 8.dp)
              .padding(start = 64.dp),
            text = stringResource(R.string.and_or),
          )

          var selectedStations by remember { mutableStateOf(emptySet<String>()) }
          stationLister
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

          Row(
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = { confirmWidgetUpdate(selectedStations, useClosestStation) },
              enabled = selectedStations.isNotEmpty() || useClosestStation
            ) {
              Text(text = stringResource(R.string.confirm))
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }

  @Composable
  private fun CheckBoxRow(isChecked: Boolean, text: String, onCheckedChange: (Boolean) -> Unit) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(checked = isChecked, onCheckedChange = { onCheckedChange(it) })
      Text(text = text, modifier = Modifier.clickable { onCheckedChange(!isChecked) })
    }
  }

  private fun confirmWidgetUpdate(stations: Set<String>, useClosestStation: Boolean) {
    viewModel.configurationComplete.observe(this) { isComplete ->
      if (!isComplete) return@observe
      // If the configuration completed, then we can confirm that the widget is set up.
      val resultValue = Intent().putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
      setResult(RESULT_OK, resultValue)
      finish()
    }

    viewModel.applyWidgetConfiguration(appWidgetId, stations, useClosestStation)
  }

  private fun checkLocationPermission(): Boolean {
    if (VERSION.SDK_INT < 23) {
      return false
    }
    if (permissionHelper.hasLocationPermission()) return false

    locationPermissionRequest.launch(permissionHelper.locationPermissions)
    return true
  }

  private fun checkBackgroundLocationPermission(): Boolean {
    if (permissionHelper.hasBackgroundLocationPermission()) return false

    val backgroundPermissionLabel = if (VERSION.SDK_INT >= 30) {
      packageManager.backgroundPermissionOptionLabel
    } else {
      getString(R.string.background_location_permission_label_fallback)
    }

    val dialogMessage =
      getString(R.string.background_location_permission_message, backgroundPermissionLabel)

    AlertDialog.Builder(this)
      .setTitle(R.string.background_location_permission_title)
      .setMessage(dialogMessage)
      .setPositiveButton(android.R.string.ok) { _, _ ->
        if (prefs.getBoolean(KeyBackgroundLocationRequested, false)) {
          // If we've already asked, the API will just reject us immediately 🙄. Do the next
          // best thing and send them to our settings page.
          val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
          intent.data = Uri.fromParts("package", packageName, null)
          startActivity(intent)
        } else {
          // If we've never asked before, then use the background location permission request,
          // which goes directly to location settings.
          backgroundLocationPermissionRequest.launch(permissionHelper.backgroundLocationPermissions)
          prefs.edit().putBoolean(KeyBackgroundLocationRequested, true).apply()
        }
      }
      .setNegativeButton(android.R.string.cancel, null)
      .create()
      .show()
    return true
  }

  private companion object {
    const val KeyBackgroundLocationRequested = "background_location_requested"
  }
}
