package com.sixbynine.transit.path.backend

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
class TrainDataManagerTest {
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Inject lateinit var trainDataManager: TrainDataManager

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Test
  fun testSomething() {

  }
}