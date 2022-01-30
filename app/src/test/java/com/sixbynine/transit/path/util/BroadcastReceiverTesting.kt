package com.sixbynine.transit.path.util

import android.content.BroadcastReceiver
import android.content.BroadcastReceiver.PendingResult
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.robolectric.Shadows.shadowOf
import org.robolectric.util.ReflectionHelpers
import kotlin.reflect.KClass

inline fun <reified T : BroadcastReceiver> sendBroadcastBlocking(intent: Intent) {
  sendBroadcastBlocking(T::class, intent)
}

fun sendBroadcastBlocking(receiverClass: KClass<out BroadcastReceiver>, intent: Intent) {
  val context = ApplicationProvider.getApplicationContext<Context>()
  val receiver = receiverClass.java.newInstance()
  receiver.onReceive(context, intent)
  if (shadowOf(receiver).wentAsync()) {
    val pendingResult = shadowOf(receiver).originalPendingResult
    shadowOf(pendingResult).future.get()
  }
}