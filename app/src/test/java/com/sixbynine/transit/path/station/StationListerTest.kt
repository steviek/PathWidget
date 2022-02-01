package com.sixbynine.transit.path.station

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class StationListerTest {
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Inject lateinit var stationLister: StationLister

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Test
  fun getClosestStation_exchangePlace() {
    val closestStation = stationLister.getClosestStation(40.71930410130449, -74.03263924822242)

    assertThat(closestStation.displayName).isEqualTo("Exchange Place")
  }

  @Test
  fun getClosestStation_newport() {
    val closestStation = stationLister.getClosestStation(40.72505482470874, -74.0337956911837)

    assertThat(closestStation.displayName).isEqualTo("Newport")
  }

  @Test
  fun getClosestStation_33rdStreet() {
    val closestStation = stationLister.getClosestStation(40.753270517632465, -73.9844736106006)

    assertThat(closestStation.displayName).isEqualTo("33rd Street")
  }

}