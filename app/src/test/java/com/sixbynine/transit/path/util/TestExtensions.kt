package com.sixbynine.transit.path.util

import kotlinx.coroutines.runBlocking

fun runBlockingTest(test: suspend () -> Unit) {
  runBlocking {
    test()
  }
}
