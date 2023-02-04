package com.sixbynine.transit.path.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build.VERSION
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.sixbynine.transit.path.R
import com.sixbynine.transit.path.R.string

object UpdateNotifications {
    private const val ChannelName = "updates"

    fun createForegroundInfo(context: Context): ForegroundInfo {
        createNotificationChannel(context)
        return ForegroundInfo(
            R.id.update_notification,
            NotificationCompat.Builder(context, ChannelName)
                .setContentText(context.getString(string.update_notification_text))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .build()
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                /* id = */ ChannelName,
                /* name = */ context.getString(string.channel_name_update),
                /* importance = */ NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}