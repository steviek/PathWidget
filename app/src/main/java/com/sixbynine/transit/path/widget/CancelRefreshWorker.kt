package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.sixbynine.transit.path.logging.Logging
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CancelRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val widgetUpdater: WidgetUpdater,
    private val logging: Logging
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        logging.debug("Cancelling widgets that are still refreshing")
        widgetUpdater.failRefreshingWidgets()
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return UpdateNotifications.createForegroundInfo(applicationContext)
    }
}