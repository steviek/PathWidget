package com.sixbynine.transit.path.time

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings
import java.time.LocalTime
import java.util.Locale
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class DateTimeFormatterTest {
  @get:Rule val hiltRule = HiltAndroidRule(this)

  @Inject lateinit var dateTimeFormatter: DateTimeFormatter

  @Before
  fun setUp() {
    hiltRule.inject()
  }

  @Config(sdk = [23])
  @Test
  fun formatLocalTime_12hr_23() {
    ShadowSettings.set24HourTimeFormat(false)

    assertThat(formatLocalTime(LocalTime.of(0, 15))).isEqualTo("12:15")
    assertThat(formatLocalTime(LocalTime.of(3, 4))).isEqualTo("3:04")
    assertThat(formatLocalTime(LocalTime.of(12, 10))).isEqualTo("12:10")
    assertThat(formatLocalTime(LocalTime.of(16, 3))).isEqualTo("4:03")
  }

  @Config(sdk = [23])
  @Test
  fun formatLocalTime_24hr_23() {
    ShadowSettings.set24HourTimeFormat(true)

    assertThat(formatLocalTime(LocalTime.of(0, 15))).isEqualTo("00:15")
    assertThat(formatLocalTime(LocalTime.of(3, 4))).isEqualTo("03:04")
    assertThat(formatLocalTime(LocalTime.of(12, 10))).isEqualTo("12:10")
    assertThat(formatLocalTime(LocalTime.of(16, 3))).isEqualTo("16:03")
  }

  @Config(sdk = [29])
  @Test
  fun formatLocalTime_12hr_29() {
    ShadowSettings.set24HourTimeFormat(false)

    assertThat(formatLocalTime(LocalTime.of(0, 15))).isEqualTo("12:15")
    assertThat(formatLocalTime(LocalTime.of(3, 4))).isEqualTo("3:04")
    assertThat(formatLocalTime(LocalTime.of(12, 10))).isEqualTo("12:10")
    assertThat(formatLocalTime(LocalTime.of(16, 3))).isEqualTo("4:03")
  }

  @Config(sdk = [29])
  @Test
  fun formatLocalTime_24hr_29() {
    ShadowSettings.set24HourTimeFormat(true)

    assertThat(formatLocalTime(LocalTime.of(0, 15))).isEqualTo("00:15")
    assertThat(formatLocalTime(LocalTime.of(3, 4))).isEqualTo("03:04")
    assertThat(formatLocalTime(LocalTime.of(12, 10))).isEqualTo("12:10")
    assertThat(formatLocalTime(LocalTime.of(16, 3))).isEqualTo("16:03")
  }

  @Config(sdk = [29])
  @Test
  fun formatLocalTime_12hr_29_es() {
    Locale.setDefault(Locale("es", "US"))
    ShadowSettings.set24HourTimeFormat(false)

    assertThat(formatLocalTime(LocalTime.of(0, 15))).isEqualTo("12:15")
    assertThat(formatLocalTime(LocalTime.of(3, 4))).isEqualTo("3:04")
    assertThat(formatLocalTime(LocalTime.of(12, 10))).isEqualTo("12:10")
    assertThat(formatLocalTime(LocalTime.of(16, 3))).isEqualTo("4:03")
  }

  @Config(sdk = [29])
  @Test
  fun formatLocalTime_24hr_29_es() {
    Locale.setDefault(Locale("es", "US"))
    ShadowSettings.set24HourTimeFormat(true)

    assertThat(formatLocalTime(LocalTime.of(0, 15))).isEqualTo("00:15")
    assertThat(formatLocalTime(LocalTime.of(3, 4))).isEqualTo("03:04")
    assertThat(formatLocalTime(LocalTime.of(12, 10))).isEqualTo("12:10")
    assertThat(formatLocalTime(LocalTime.of(16, 3))).isEqualTo("16:03")
  }

  private fun formatLocalTime(time: LocalTime) = dateTimeFormatter.formatLocalTime(time)
}