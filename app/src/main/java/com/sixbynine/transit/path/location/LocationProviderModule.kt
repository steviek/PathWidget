package com.sixbynine.transit.path.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build.VERSION
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.logging.Logging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.time.Duration
import javax.inject.Inject

class NonGmsLocationProvider @Inject internal constructor(
  @ApplicationContext private val context: Context,
  private val locationManager: LocationManager?,
  private val logging: Logging
): LocationProvider {

  override val isLocationSupportedByDevice: Boolean
    get() = locationManager != null

  override suspend fun tryToGetLocation(timeout: Duration): LocationCheckResult {
    if (locationManager == null) {
      return NoProvider
    }

    // This is duplicated code, but it prevents Android Studio lint complaining.
    if (
      VERSION.SDK_INT >= 23 &&
      context.checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
      context.checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
      logging.debug("Don't have permission to get the user's location")
      return NoPermission
    }

    val criteria = Criteria().apply {
      accuracy = Criteria.ACCURACY_FINE
      isCostAllowed = false
    }
    val provider =
      locationManager.getBestProvider(criteria, /* enabledOnly= */ true) ?: return NoProvider
    val lastKnownLocation = locationManager.getLastKnownLocation(provider)
    logging.debug("Last known location is ${lastKnownLocation?.toLatLngStringWithGoogleApiLink()}")

    return try {
      withTimeout(timeout.toMillis()) {
        val currentLocation = locationManager.getCurrentLocation(provider)
        logging.debug(
          "Retrieved current location as ${currentLocation.toLatLngStringWithGoogleApiLink()} from $provider"
        )
        Success(currentLocation)
      }
    } catch (e: TimeoutCancellationException) {
      logging.warn("Timed out trying to get the user's location")
      lastKnownLocation?.let { Success(it) } ?: Failure(e)
    } catch (t: Throwable) {
      logging.warn("Unexpected error getting the user's location", t)
      lastKnownLocation?.let { Success(it) } ?: Failure(t)
    }
  }

  @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
  private suspend fun LocationManager.getCurrentLocation(
    provider: String
  ) = suspendCancellableCoroutine<Location> { continuation ->
    if (VERSION.SDK_INT >= 30) {
      val cancellationSignal = CancellationSignal()
      getCurrentLocation(
        provider,
        cancellationSignal,
        context.mainExecutor
      ) { location ->
        continuation.resumeWith(Result.success(location))
      }
      continuation.invokeOnCancellation { cancellationSignal.cancel() }
    } else {
      @Suppress("DEPRECATION") // I know...
      requestSingleUpdate(
        provider,
        { continuation.resumeWith(Result.success(it)) },
        /* looper= */ null
      )
    }
  }

  private fun Location.toLatLngStringWithGoogleApiLink(): String {
    return "($latitude, $longitude) [https://www.google.com/maps/search/" +
        "?api=1&query=$latitude%2C$longitude]"
  }
}

@InstallIn(SingletonComponent::class)
@Module
interface LocationProviderModule {
  @Binds
  fun bindLocationProvider(provider: NonGmsLocationProvider): LocationProvider
}

var Location.elapsedRealtime: Duration
  get() = Duration.ofNanos(elapsedRealtimeNanos)
  set(value) {
    elapsedRealtimeNanos = value.toNanos()
  }

