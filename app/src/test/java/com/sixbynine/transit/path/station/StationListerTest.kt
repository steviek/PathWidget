package com.sixbynine.transit.path.station

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class StationListerTest {

  @Test
  fun getClosestStation_exchangePlace() {
    val closestStation = StationLister.getClosestStation(40.71930410130449, -74.03263924822242)

    assertThat(closestStation.displayName).isEqualTo("Exchange Place")
  }

  @Test
  fun getClosestStation_newport() {
    val closestStation = StationLister.getClosestStation(40.72505482470874, -74.0337956911837)

    assertThat(closestStation.displayName).isEqualTo("Newport")
  }

  @Test
  fun getClosestStation_33rdStreet() {
    val closestStation = StationLister.getClosestStation(40.753270517632465, -73.9844736106006)

    assertThat(closestStation.displayName).isEqualTo("33rd Street")
  }
}
