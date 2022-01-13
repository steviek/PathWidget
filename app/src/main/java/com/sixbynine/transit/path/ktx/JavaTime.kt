package com.sixbynine.transit.path.ktx

import java.time.Duration

val Int.seconds: Duration
  get() = Duration.ofSeconds(toLong())

val Int.minutes: Duration
  get() = Duration.ofMinutes(toLong())

val Int.hours: Duration
  get() = Duration.ofHours(toLong())
