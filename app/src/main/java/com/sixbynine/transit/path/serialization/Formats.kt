package com.sixbynine.transit.path.serialization

import kotlinx.serialization.json.Json

val JsonFormat = Json {
  ignoreUnknownKeys = true
}
