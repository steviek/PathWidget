package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType.UNMETERED
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.await
import com.sixbynine.transit.path.ktx.minutes
import com.sixbynine.transit.path.logging.Logging
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  private val widgetUpdater: WidgetUpdater,
  private val logging: Logging
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    logging.debug("Start widget refresh worker")
    widgetUpdater.updateData()
    logging.debug("Finish widget refresh worker")
    return Result.success()
  }
}

class WidgetRefreshWorkerScheduler @Inject constructor(
  private val workManager: WorkManager
) {
  suspend fun schedulePeriodicRefresh() {
    // Start off conservative, and only update periodically if on WiFi and the battery isn't low
    // to save the user's data and battery life.
    val constraints = Constraints.Builder()
      .setRequiresBatteryNotLow(true)
      .setRequiredNetworkType(UNMETERED)
      .build()
    val workRequest = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15.minutes)
      .setConstraints(constraints)
      .build()
    workManager.enqueueUniquePeriodicWork(WORK_TAG, KEEP, workRequest).result.await()
  }

  suspend fun cancelPeriodicRefresh() {
    workManager.cancelUniqueWork(WORK_TAG).result.await()
  }

  private companion object {
    const val WORK_TAG = "path_widget_refresh"
  }
}

