package com.sixbynine.transit.path.fake

import android.location.Location
import com.sixbynine.transit.path.location.LocationCheckResult
import com.sixbynine.transit.path.location.LocationProvider
import com.sixbynine.transit.path.location.LocationProviderModule
import com.sixbynine.transit.path.location.elapsedRealtime
import com.sixbynine.transit.path.permission.PermissionHelper
import com.sixbynine.transit.path.time.ElapsedRealtimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.bouncycastle.its.asn1.Latitude
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLocationProvider @Inject constructor(
  private val elapsedRealtimeProvider: ElapsedRealtimeProvider,
  private val permissionHelper: PermissionHelper
) : LocationProvider {
  private var result: LocationCheckResult? = null
  private var noProvider = false

  override suspend fun tryToGetLocation(timeout: Duration): LocationCheckResult {
    if (!permissionHelper.hasLocationPermission()) return LocationCheckResult.NoPermission
    return result ?: LocationCheckResult.Failure(RuntimeException())
  }

  fun setLocation(latitude: Double, longitude: Double) {
    val location = Location(/* provider= */ "fake")
    location.latitude = latitude
    location.longitude = longitude
    location.elapsedRealtime = elapsedRealtimeProvider.elapsedRealtime()
    result = LocationCheckResult.Success(location)
  }

  fun setFailed(cause: Throwable = RuntimeException()) {
    result = LocationCheckResult.Failure(cause)
  }

  fun setNoProvider() {
    result = LocationCheckResult.NoProvider
  }
}

@TestInstallIn(
  components = [SingletonComponent::class],
  replaces = [LocationProviderModule::class]
)
@Module
interface FakeLocationProviderModule {
  @Binds
  fun bindLocationProvider(fake: FakeLocationProvider): LocationProvider
}
