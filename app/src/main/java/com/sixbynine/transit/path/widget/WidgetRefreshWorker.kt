package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy.EXPONENTIAL
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType.UNMETERED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import com.sixbynine.transit.path.ktx.minutes
import com.sixbynine.transit.path.logging.Logging
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val widgetUpdater: WidgetUpdater,
    private val widgetRefresher: WidgetRefreshWorkerScheduler,
    private val logging: Logging
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        logging.debug("Start widget refresh worker")
        widgetRefresher.scheduleFailRefreshWorker()
        val updateSucceeded = widgetUpdater.updateData()
        return if (updateSucceeded) {
            logging.debug("Widget refresh succeeded")
            widgetRefresher.cancelFailRefreshWorker()
            Result.success()
        } else {
            logging.debug("Widget refresh failed, retrying eventually")
            Result.retry()
        }
    }
}

class WidgetRefreshWorkerScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    private val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .setRequiredNetworkType(UNMETERED)
        .build()

    suspend fun schedulePeriodicRefresh() {
        // Start off conservative, and only update periodically if on WiFi and the battery isn't low
        // to save the user's data and battery life.
        val workRequest = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15.minutes)
            .setConstraints(constraints)
            .setBackoffCriteria(EXPONENTIAL, 10, SECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_TAG, KEEP, workRequest).result.await()
    }

    suspend fun cancelPeriodicRefresh() {
        workManager.cancelUniqueWork(WORK_TAG).result.await()
    }

    suspend fun performOneTimeRefresh() {
        val workRequest = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setBackoffCriteria(EXPONENTIAL, 10, SECONDS)
            .setExpedited(/* policy = */ RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.enqueueUniqueWork(ONE_TIME_WORK_TAG, REPLACE, workRequest)
            .result
            .await()
    }

    suspend fun scheduleFailRefreshWorker() {
        val workRequest = OneTimeWorkRequestBuilder<CancelRefreshWorker>()
            .setInitialDelay(30, SECONDS)
            .build()
        workManager.enqueueUniqueWork(ONE_TIME_FAIL_WORK_TAG, REPLACE, workRequest)
            .result
            .await()
    }

    suspend fun cancelFailRefreshWorker() {
        workManager.cancelUniqueWork(ONE_TIME_FAIL_WORK_TAG).result.await()
    }

    private companion object {
        const val WORK_TAG = "path_widget_refresh"
        const val ONE_TIME_WORK_TAG = "path_widget_refresh_one_time"
        const val ONE_TIME_FAIL_WORK_TAG = "path_widget_fail_refresh_one_time"
    }
}

