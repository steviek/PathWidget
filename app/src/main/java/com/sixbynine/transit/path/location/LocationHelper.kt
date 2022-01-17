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
import com.sixbynine.transit.path.ktx.minutes
import com.sixbynine.transit.path.location.LocationCheckResult.Failure
import com.sixbynine.transit.path.location.LocationCheckResult.NoPermission
import com.sixbynine.transit.path.location.LocationCheckResult.NoProvider
import com.sixbynine.transit.path.location.LocationCheckResult.Success
import com.sixbynine.transit.path.util.LogTag
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
  private val locationManager: LocationManager
): LocationProvider {
  override suspend fun tryToGetLocation(timeout: Duration): LocationCheckResult {
    // This is duplicated code, but it prevents Android Studio lint complaining.
    if (
      VERSION.SDK_INT >= 23 &&
      context.checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
      context.checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
      return NoPermission
    }

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
      withTimeout(timeout.toMillis()) {
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
}

@InstallIn(SingletonComponent::class)
@Module
interface LocationProviderModule {
  @Binds
  fun bindLocationProvider(provider: NonGmsLocationProvider): LocationProvider
}

val Location.age: Duration
  get() = Duration.ofNanos(SystemClock.elapsedRealtimeNanos()) -
      Duration.ofNanos(elapsedRealtimeNanos)
