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
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresPermission
import com.sixbynine.transit.path.application
import com.sixbynine.transit.path.ktx.minutes
import com.sixbynine.transit.path.ktx.seconds
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.util.LogTag
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.time.Duration

fun hasLocationPermission(): Boolean {
  if (VERSION.SDK_INT < 23) {
    return true
  }
  return application.checkSelfPermission(ACCESS_FINE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED ||
      application.checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

suspend fun tryToGetLocation(): LocationCheckResult {
  if (
    VERSION.SDK_INT >= 23 &&
    application.checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
    application.checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
  ) {
    return NoPermission
  }

  val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  val criteria = Criteria().apply {
    accuracy = Criteria.ACCURACY_FINE
    isCostAllowed = false
  }
  val provider =
    locationManager.getBestProvider(criteria, /* enabledOnly= */ true) ?: return NoProvider
  val lastKnownLocation = locationManager.getLastKnownLocation(provider)
  if (lastKnownLocation != null && lastKnownLocation.age < 15.minutes) {
    return Success(lastKnownLocation)
  }

  return try {
    withTimeout(1.seconds.toMillis()) {
      Success(locationManager.getCurrentLocation(provider))
    }
  } catch (e: TimeoutCancellationException) {
    Log.w(LogTag, "Timed out trying to get the user's location")
    lastKnownLocation?.let { Success(it) } ?: Failure(e)
  } catch (t: Throwable) {
    Log.w(LogTag, "Unexpected error getting the user's location")
    lastKnownLocation?.let { Success(it) } ?: Failure(t)
  }
}

@RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
suspend fun LocationManager.getCurrentLocation(
  provider: String
) = suspendCancellableCoroutine<Location> { continuation ->
  if (VERSION.SDK_INT >= 30) {
    val cancellationSignal = CancellationSignal()
    getCurrentLocation(
      provider,
      cancellationSignal,
      application.mainExecutor
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

val Location.age: Duration
  get() = Duration.ofNanos(SystemClock.elapsedRealtimeNanos()) -
      Duration.ofNanos(elapsedRealtimeNanos)

sealed interface LocationCheckResult {
  object NoPermission : LocationCheckResult
  object NoProvider : LocationCheckResult
  data class Failure(val throwable: Throwable) : LocationCheckResult
  data class Success(val location: Location) : LocationCheckResult
}